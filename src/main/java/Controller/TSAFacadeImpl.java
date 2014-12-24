package Controller;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import Common.DPILogger;

import com.google.gson.Gson;

public class TSAFacadeImpl implements ITSAFacade {

	private static final Logger LOGGER = DPILogger.LOGGER;
	private static final String IGNORED_ADDRESS = "10.0.0.0";
	private final short _identificationPort = 6666;

	// TODO: move to config file
	@Override
	public boolean applyPolicyChain(List<InetAddress> chain) {
		String msg = generateChainMessage(chain);
		try {
			sendMessage(msg);
			LOGGER.info("send tsa message " + msg);
		} catch (IOException e) {

			LOGGER.error("error while sending policyChain:");
			LOGGER.error(e.getMessage());
			e.printStackTrace();
			return false;
		}
		return true;
	}

	private void sendMessage(String msg) throws IOException {
		DatagramSocket socket = new DatagramSocket();
		byte[] buf = new byte[1000];
		buf = msg.getBytes();
		InetAddress address = InetAddress.getByName(IGNORED_ADDRESS);
		DatagramPacket packet = new DatagramPacket(buf, buf.length, address,
				_identificationPort);
		socket.send(packet);
		socket.close();

	}

	private String generateChainMessage(List<InetAddress> chain) {
		Gson gson = new Gson();
		String[] hosts = new String[chain.size()];
		for (int i = 0; i < chain.size(); i++) {
			InetAddress host = chain.get(i);
			hosts[i] = host.getHostAddress();
		}
		String json = gson.toJson(hosts);
		return json;
	}
}
