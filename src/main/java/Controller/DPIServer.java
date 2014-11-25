package Controller;

import Common.DPILogger;
import Common.Protocol.Controller.ControllerMessage;
import Common.Protocol.Controller.RuleAdd;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by Lior on 25/11/2014.
 */
public class DPIServer {
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

    public void registerService(ControllerThread thread, ServiceInstance instance) {
        _servicesThreads.put(instance, thread);
        _idleThreads.remove(thread);
    }

    public void registerMiddlebox(ControllerThread thread, Middlebox mb) {
        _middleboxThreads.put(mb, thread);
        _idleThreads.remove(thread);
    }

    public void deregisterService(ControllerThread thread, ServiceInstance instance) {
        thread.setKeepRunning(false);
        _servicesThreads.remove(instance);
    }

    public void deregisterMiddlebox(ControllerThread thread, Middlebox mb) {
        thread.setKeepRunning(false);
        _middleboxThreads.remove(mb);
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
}
