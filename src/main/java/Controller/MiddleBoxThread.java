package Controller;

import Common.DPILogger;
import Common.Protocol.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;

/**
 * this thread class is running per registered middlebox and handle all of the middlebox messages using the controller
 * Created by Lior on 12/11/2014.
 */
public class MiddleBoxThread extends Thread {
    private Socket _middleboxSocket;
    private DPIController _dpiController;
    private boolean keepRunning;

    public MiddleBoxThread(Socket middleboxSocket, DPIController dpiController) {
        super("MiddleBoxThread");
        _middleboxSocket = middleboxSocket;
        _dpiController = dpiController;
    }


    /**
     * wait for incoming messages
     */
    public void run(){
        DPILogger.LOGGER.info("thread started: " + this.getId());
        InetAddress clientIP = _middleboxSocket.getInetAddress();
        DPILogger.LOGGER.info("Incoming connection from : " + clientIP);
        keepRunning = true;
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(_middleboxSocket.getInputStream()));
            String inputLine;
            while (this.keepRunning && (inputLine =in.readLine()) != null){
                DPILogger.LOGGER.info(String.format("Recevied %s from %s", inputLine, clientIP));
                handleMessage(inputLine);
            }
        } catch (SocketException e){
            DPILogger.LOGGER.info(clientIP + " has closed the connection ");

        } catch (IOException e) {
            e.printStackTrace();
        }
        DPILogger.LOGGER.info("thread stopped: " + this.getId());
    }


    /**
     * decode the message and handle it using the dpi-controller api
     * @param message json string representing a valid middlebox message
     */
    private void handleMessage(String message) {
        MiddleboxMessage msgObj = MiddleboxMessageFactory.create(message);
        if (msgObj == null){
            DPILogger.LOGGER.error("Unknown Message: message");
            return;
        }
       String msgType = msgObj.getClass().getSimpleName();
       DPILogger.LOGGER.info("got: " + msgType);

        switch (msgType){
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
            default:
                DPILogger.LOGGER.error("unknown operation this shouldnt happen: " + msgType);


        }
    }

}
