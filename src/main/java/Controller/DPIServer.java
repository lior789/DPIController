package Controller;

import Common.DPILogger;
import Common.Middlebox;
import Common.Protocol.Controller.ControllerMessage;
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

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * handle the communiction with the different instances, dispatch message and passes to controller
 * Created by Lior on 25/11/2014.
 */
public class DPIServer implements IDPIServiceFacade {
    private DPIController _controller;
    private int _port;
    private boolean _listening;
    private List<ControllerThread> _idleThreads;
    private Map<Middlebox, ControllerThread> _middleboxThreads;
    private Map<ServiceInstance, ControllerThread> _servicesThreads;

    public DPIServer(DPIController dpiController, int port) {
        _middleboxThreads = new HashMap<Middlebox, ControllerThread>();
        _servicesThreads = new HashMap<ServiceInstance, ControllerThread>();
        _controller = dpiController;
        _idleThreads = new LinkedList<>();
        _port = port;
        _listening = true;
    }

    /**
     * waits for incoming connections and rules changes
     *
     * @param dpiController
     */
    public void run() {
        try {
            ServerSocket serverSocket = new ServerSocket(_port);
            DPILogger.LOGGER.info("Dpi controller is up!");
            while (_listening) {
                Socket clientSocket = serverSocket.accept();
                ControllerThread serverThread = new ControllerThread(clientSocket, _controller, this);
                _idleThreads.add(serverThread);
                serverThread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean sendMessage(ServiceInstance instance, ControllerMessage msg) {
        ControllerThread thread = _servicesThreads.get(instance);
        try {
            thread.sendMessage(msg);
        } catch (IOException e) {
            DPILogger.LOGGER.error("cant send message to client: " + instance);
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public void dispacthMessage(ControllerThread thread, MiddleboxRegister msg) {
        Middlebox mb = new Middlebox(msg.id, msg.name);
        _middleboxThreads.put(mb, thread);
        _idleThreads.remove(thread);
        _controller.registerMiddlebox(mb);

    }

    public void dispacthMessage(ControllerThread thread, MiddleboxDeregister msg) {
        Middlebox mb = new Middlebox(msg.id, msg.name);
        thread.setKeepRunning(false);
        _middleboxThreads.remove(mb);
        _controller.deregisterMiddlebox(mb);
    }

    public void dispacthMessage(ControllerThread thread, MiddleboxRulesetAdd msg) {
        _controller.addRules(new Middlebox(msg.id), msg.rules);
    }

    public void dispacthMessage(ControllerThread thread, MiddleboxRulesetRemove msg) {
        _controller.removeRules(new Middlebox(msg.id), MatchRule.create(msg.rules));
    }

    public void dispacthMessage(ControllerThread thread, InstanceRegister msg) {
        ServiceInstance instance = new ServiceInstance(msg.id, msg.name);
        _servicesThreads.put(instance, thread);
        _idleThreads.remove(thread);
        _controller.registerInstance(instance);
    }

    public void dispacthMessage(ControllerThread thread, InstanceDeregister msg) {
        ServiceInstance instance = new ServiceInstance(msg.id);
        thread.setKeepRunning(false);
        _servicesThreads.remove(instance);
        _controller.deregisterInstance(instance);
    }

    @Override
    public void deallocateRule(List<MatchRule> rules, ServiceInstance instance) {
        RuleRemove ruleRemove = new RuleRemove();
        List<String> rids = new LinkedList<>();
        for (MatchRule rule : rules) {
            rids.add(rule.rid);
        }
        ruleRemove.rules = rids;
        sendMessage(instance, ruleRemove);
    }

    @Override
    public void assignRules(List<MatchRule> rules, ServiceInstance instance) {
        RuleAdd ruleAdd = new RuleAdd();
        ruleAdd.rules = rules;
        sendMessage(instance, ruleAdd);
    }

}

