package Controller;

import Common.DPILogger;
import Common.Protocol.Controller.RuleAdd;
import Common.Protocol.Controller.RuleRemove;
import Common.Protocol.MatchRule;
import Common.Protocol.Middlebox.MiddleboxDeregister;
import Common.Protocol.Middlebox.MiddleboxRegister;
import Common.Protocol.Middlebox.MiddleboxRulesetAdd;
import Common.Protocol.Middlebox.MiddleboxRulesetRemove;
import Common.Protocol.Service.InstanceRegister;

import java.util.LinkedList;
import java.util.List;

/**
 * This class is the DPIcontroller u main class, the rules of this class:
 * 1. handle input rules from the middlebox - using ControllerThread
 * 2. keep track of all the match rules (patterns) - using the MatchRulesRepository
 * 3. updates the dpi-services on the Match Rules
 * 4. load balance the dpi-services
 * 5. altering the policy chain of the sdn controller,
 *    making each packet corresponding to one of the registered middleboxes
 *    to go through the relevant service
 * Created by Lior on 12/11/2014.
 */
public class DPIController {
    //todo: use interfaces for foreman and repository

    private final DPIForeman _foreman; //handles work between instances
    private final MiddleboxRepository _middleboxes; //rules per middlebox
    private final DPIServer _server; // handle the connections with middlebox and services

    /**
     * @param port on which port the controller is listening to messages
     */
    public DPIController(int port) {
        _middleboxes = new MiddleboxRepository();
        _foreman = new DPIForeman(new SimpleLoadBalanceStrategy(), this);
        _server = new DPIServer(this, port);
    }

    public void registerMiddlebox(ControllerThread thread, MiddleboxRegister msg) {
        if (!_middleboxes.addMiddlebox(msg.id, msg.name)) {
            DPILogger.LOGGER.warn("middlebox already exists: " + msg.id);
            return;
        }
        _server.registerMiddlebox(thread, new Middlebox(msg.id, msg.name));
    }

    public void deregisterMiddlebox(ControllerThread thread, MiddleboxDeregister msg) {
        DPILogger.LOGGER.trace(String.format("Middlebox %s is going to be removed", msg.id));
        List<String> removedRules = _middleboxes.removeMiddlebox(msg.id);
        if (removedRules == null) {
            DPILogger.LOGGER.warn(String.format("no such middlebox %s", msg.id));
            return;
        }
        _foreman.removeJobs(MatchRule.create(removedRules));
        _server.deregisterMiddlebox(thread, new Middlebox(msg.id));
    }

    public void removeRules(MiddleboxRulesetRemove msg) {
        if (!_middleboxes.removeRules(msg.id, msg.rules)) {
            DPILogger.LOGGER.warn("problem while removing rules for middlebox " + msg.id);
            return;
        }
        _foreman.removeJobs(MatchRule.create(msg.rules));
    }

    public void addRules(MiddleboxRulesetAdd msg) {
        if (!_middleboxes.addRules(msg.id, msg.rules)) {
            DPILogger.LOGGER.warn(String.format("no such middlebox %s", msg.id));
            return;
        }
        _foreman.addJobs(msg.rules);
    }

    public void registerInstance(ControllerThread thread, InstanceRegister registerMsg) {
        //todo: make boolean
        ServiceInstance instance = new ServiceInstance(registerMsg.id, registerMsg.name);
        _foreman.addWorker(instance);
        _server.registerService(thread, instance);
    }

    public void deregisterInstance(ControllerThread thread, InstanceRegister deregisterMsg) {
        ServiceInstance instance = new ServiceInstance(deregisterMsg.id);
        _foreman.removeWorker(instance);
        _server.deregisterService(thread, instance);
    }

    public void run() {
        _server.run();
    }

    public void deallocateRule(List<MatchRule> rules, ServiceInstance instance) {
        RuleRemove ruleRemove = new RuleRemove();
        List<String> rids = new LinkedList<>();
        for (MatchRule rule : rules) {
            rids.add(rule.rid);
        }
        ruleRemove.rules = rids;
        _server.sendMessage(instance, ruleRemove);
    }

    public void assignRules(List<MatchRule> rules, ServiceInstance instance) {
        RuleAdd ruleAdd = new RuleAdd();
        ruleAdd.rules = rules;
        _server.sendMessage(instance, ruleAdd);
    }
}
