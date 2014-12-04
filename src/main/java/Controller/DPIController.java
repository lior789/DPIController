package Controller;

import Common.DPILogger;
import Common.Middlebox;
import Common.Protocol.Controller.RuleAdd;
import Common.Protocol.Controller.RuleRemove;
import Common.Protocol.MatchRule;
import Common.Protocol.Middlebox.MiddleboxDeregister;
import Common.Protocol.Middlebox.MiddleboxRegister;
import Common.Protocol.Middlebox.MiddleboxRulesetAdd;
import Common.Protocol.Middlebox.MiddleboxRulesetRemove;
import Common.Protocol.Service.InstanceDeregister;
import Common.Protocol.Service.InstanceRegister;
import Common.ServiceInstance;

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

    private final DPIForeman _foreman; //handles work between instances
    private final MiddleboxRepository _middleboxes; //rules per middlebox
    private final DPIServer _server; // handle the connections with middlebox and services

    /**
     * @param port on which port the controller is listening to messages
     */
    public DPIController(int port) {
        _middleboxes = new MiddleboxRepository();
        _server = new DPIServer(this, port);
        _foreman = new DPIForeman(new SimpleLoadBalanceStrategy(), _server);
    }

    public void registerMiddlebox(Middlebox mb) {
        if (!_middleboxes.addMiddlebox(mb)) {
            DPILogger.LOGGER.warn("middlebox already exists: " + mb.id);
            return;
        }
    }

    public void deregisterMiddlebox(Middlebox mb) {
        DPILogger.LOGGER.trace(String.format("Middlebox %s is going to be removed", mb.id));
        List<MatchRule> removedRules = _middleboxes.removeMiddlebox(mb);
        if (removedRules == null) {
            DPILogger.LOGGER.warn(String.format("no such middlebox %s", mb.id));
            return;
        }
        _foreman.removeJobs(removedRules);

    }

    public void removeRules(Middlebox mb, List<MatchRule> rules) {
        if (!_middleboxes.removeRules(mb, rules)) {
            DPILogger.LOGGER.warn("problem while removing rules for middlebox " + mb.id);
            return;
        }
        _foreman.removeJobs(rules);
    }

    public void addRules(Middlebox mb, List<MatchRule> rules) {
        if (!_middleboxes.addRules(mb, rules)) {
            DPILogger.LOGGER.warn(String.format("no such middlebox %s", mb.id));
            return;
        }
        _foreman.addJobs(rules);
    }

    public void registerInstance(ServiceInstance instance) {
        if (!_foreman.addWorker(instance)) {
            DPILogger.LOGGER.warn("instance already registered: " + instance);
        }
    }

    public void deregisterInstance(ServiceInstance instance) {
        _foreman.removeWorker(instance);
    }

    public void run() {
        _server.run();
    }


}
