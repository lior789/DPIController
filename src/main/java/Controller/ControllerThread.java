package Controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;

import org.apache.log4j.Logger;

import Common.Protocol.DPIProtocolMessage;
import Common.Protocol.JsonUtils;
import Common.Protocol.Controller.ControllerMessage;

/**
 * this thread class is running per client (middlebox or service) and handle all
 * of the middlebox messages using the controller Created by Lior on 12/11/2014.
 */

public class ControllerThread extends Thread {
	private static final Logger LOGGER = Logger
			.getLogger(ControllerThread.class);
	private final Socket _socket;
	private final DPIServer _dpiServer;
	private boolean keepRunning;

	public ControllerThread(Socket middleboxSocket,
			DPIController dpiController, DPIServer dpiServer) {
		super("ControllerThread");
		_socket = middleboxSocket;
		_dpiServer = dpiServer;
	}

	/**
	 * wait for incoming messages
	 */
	@Override
	public void run() {
		LOGGER.info("thread started: " + getId());
		InetAddress clientIP = _socket.getInetAddress();
		LOGGER.info("Incoming connection from : " + clientIP);
		keepRunning = true;
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(
					_socket.getInputStream()));
			String inputLine;
			while (this.keepRunning && (inputLine = in.readLine()) != null) {
				LOGGER.info(String.format("Recevied %s from %s", inputLine,
						clientIP));
				handleMessage(inputLine);
			}
		} catch (SocketException e) {
			LOGGER.info(clientIP + " has closed the connection ");

		} catch (IOException e) {
			e.printStackTrace();
		}
		LOGGER.info("thread stopped: " + getId());
	}

	/**
	 * decode the message and handle it using the dpi-controller api
	 * 
	 * @param message
	 *            json string representing a valid middlebox message
	 */
	private void handleMessage(String message) {
		DPIProtocolMessage msgObj = JsonUtils.fromJson(message);
		if (msgObj == null) {
			LOGGER.error("Unknown Message Type: message");
			return;
		}
		String msgType = msgObj.getClass().getSimpleName();
		LOGGER.info("got: " + msgType);
		_dpiServer.dispacthMessage(this, msgObj);
	}

	public void setKeepRunning(boolean keepRunning) {
		this.keepRunning = keepRunning;
	}

	public void sendMessage(ControllerMessage msg) throws IOException {
		PrintWriter sendOut = new PrintWriter(_socket.getOutputStream(), true);
		sendOut.println(JsonUtils.toJson(msg));
	}

	public InetAddress getClientAddress() {
		return _socket.getInetAddress();

	}
}
