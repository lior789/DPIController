package Controller;

import Common.DPILogger;
import Common.Protocol.Middlebox.MiddleboxDeregister;
import Common.Protocol.Middlebox.MiddleboxRegister;
import Common.Protocol.Middlebox.MiddleboxRulesetAdd;
import Common.Protocol.Middlebox.MiddleboxRulesetRemove;
import Common.Protocol.Service.InstanceRegister;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

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

    private DPIForeman _foreman;
    private int _port;
    private boolean _listening;

    /**
     * @param port on which port the controller is listening to messages
     */
    public DPIController(int port) {
        _port = port;
        _listening = true;
        _foreman = new DPIForeman();
    }

    /***
     * waits for incoming connections and rules changes
     */
    public void run() {
        try {
            ServerSocket serverSocket = new ServerSocket(_port);
            DPILogger.LOGGER.info("Dpi controller is up!");
            while(_listening) {
                Socket clientSocket = serverSocket.accept();
                new MiddleBoxThread(clientSocket, this).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void registerMiddlebox(MiddleboxRegister msg) {
        if (!_foreman.addMiddlebox(msg.id, msg.name)) {
            DPILogger.LOGGER.warn("middlebox already exists: " + msg.id);
        }
    }

    public void deregisterMiddlebox(MiddleboxDeregister msg) {
        if (!_foreman.removeMiddlebox(msg.id)) {
            DPILogger.LOGGER.warn("unknown middlebox id" + msg.id);
        }
    }

    public void removeRules(MiddleboxRulesetRemove msg) {
        if (!_foreman.removeRules(msg.id, msg.rules)) {
            DPILogger.LOGGER.warn("problem while removing rules for middlebox " + msg.id);
        }
    }

    public void addRules(MiddleboxRulesetAdd msg) {
        _foreman.addRules(msg.id, msg.rules);
    }

    public void registerInstance(InstanceRegister registerMsg) {
        _foreman.addWorker(registerMsg.id, registerMsg.name);
    }

    public void deregisterInstance(InstanceRegister deregisterMsg) {
        _foreman.removeWorker(deregisterMsg.id);
    }
}
