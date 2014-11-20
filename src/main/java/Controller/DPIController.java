package Controller;

import Common.DPILogger;
import Common.Protocol.*;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * This class is the DPIcontroller u main class, the rules of this class:
 * 1. handle input rules from the middlebox - using MiddleBoxThread
 * 2. keep track of all the match rules (patterns) - using the MatchRulesRepository
 * 3. updates the dpi-services on the Match Rules
 * 4. load balance the dpi-services
 * 5. altering the policy chain of the sdn controller,
 *    making each packet corresponding to one of the registered middleboxes
 *    to go through the relevant service
 * Created by Lior on 12/11/2014.
 */
public class DPIController {

    private final MatchRulesRepository _repository;
    private int _port;
    private boolean _listening;

    /**
     * @param port on which port the controller is listening to messages
     */
    public DPIController(int port) {
        _port = port;
        _listening = true;
        _repository = new MatchRulesRepository();
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
                new MiddleBoxThread(clientSocket,this).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void registerMiddlebox(MiddleboxRegister msg) {
        if(!_repository.addMiddlebox(msg.getMiddleboxId(),msg.getMiddleboxName())){
            DPILogger.LOGGER.warn("middlebox already exists: " + msg.getMiddleboxId());
        }
    }

    public void deregisterMiddlebox(MiddleboxDeregister msg) {
        if (!_repository.removeMiddlebox(msg.getMiddleboxId())){
            DPILogger.LOGGER.warn("unknown middlebox id" + msg.getMiddleboxId());
        }
    }

    public void removeRules(MiddleboxRulesetRemove msg) {
        if(!_repository.removeRules(msg.getMiddleboxId(),msg.rules)){
            DPILogger.LOGGER.warn("problem while removing rules for middlebox " + msg.getMiddleboxId());
        }
    }

    public void addRules(MiddleboxRulesetAdd msg) {
        _repository.addRules(msg.getMiddleboxId(), msg.rules);
    }
}
