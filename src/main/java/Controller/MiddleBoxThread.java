package Controller;

import Common.DPILogger;
import Common.Protocol.DPIProtocolMessage;
import Common.Protocol.JsonUtils;
import Common.Protocol.Middlebox.*;
import Common.Protocol.Service.InstanceRegister;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;

/**
 * this thread class is running per client (middlebox or service) and handle all of the middlebox messages using the controller
 * Created by Lior on 12/11/2014.
 */
//TODO: split to two dedicated threads
public class MiddleBoxThread extends Thread {
    private Socket _socket;
    private DPIController _dpiController;
    private boolean keepRunning;

    public MiddleBoxThread(Socket middleboxSocket, DPIController dpiController) {
        super("ControllerThread");
        _socket = middleboxSocket;
        _dpiController = dpiController;
    }


    /**
     * wait for incoming messages
     */
    public void run() {
        DPILogger.LOGGER.info("thread started: " + this.getId());
        InetAddress clientIP = _socket.getInetAddress();
        DPILogger.LOGGER.info("Incoming connection from : " + clientIP);
        keepRunning = true;
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(_socket.getInputStream()));
            String inputLine;
            while (this.keepRunning && (inputLine = in.readLine()) != null) {
                DPILogger.LOGGER.info(String.format("Recevied %s from %s", inputLine, clientIP));
                handleMessage(inputLine);
            }
        } catch (SocketException e) {
            DPILogger.LOGGER.info(clientIP + " has closed the connection ");

        } catch (IOException e) {
            e.printStackTrace();
        }
        DPILogger.LOGGER.info("thread stopped: " + this.getId());
    }

    /**
     * decode the message and handle it using the dpi-controller api
     *
     * @param message json string representing a valid middlebox message
     */
    private void handleMessage(String message) {
        DPIProtocolMessage msgObj = JsonUtils.fromJson(message);
        if (msgObj == null) {
            DPILogger.LOGGER.error("Unknown Message: message");
            return;
        }
        String msgType = msgObj.getClass().getSimpleName();
        DPILogger.LOGGER.info("got: " + msgType);
        //todo: replace switch with just overload methods in controller
        switch (msgType) {
            case "MiddleboxRegister":
                _dpiController.registerMiddlebox((MiddleboxRegister) msgObj);
                break;
            case "MiddleboxDeregister":
                _dpiController.deregisterMiddlebox((MiddleboxDeregister) msgObj);
                this.keepRunning = false; //todo: maybe move to controller
                break;
            case "MiddleboxRulesetAdd":
                _dpiController.addRules((MiddleboxRulesetAdd) msgObj);
                break;
            case "MiddleboxRulesetRemove":
                _dpiController.removeRules((MiddleboxRulesetRemove) msgObj);
                break;
            case "InstanceRegister":
                _dpiController.registerInstance((InstanceRegister) msgObj);
                break;
            case "InstanceDeregister":
                _dpiController.deregisterInstance((InstanceRegister) msgObj);
                break;
            default:
                DPILogger.LOGGER.error("controller cant handle this operation: " + msgType);


        }
    }

}
