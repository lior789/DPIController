package Controller.TSA;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class TSAOFClient extends TsaClientThread {

	static final String IGNORED_ADDRESS = "10.0.0.0";
	short _identificationPort;

	public TSAOFClient(TSAFacadeImpl tsaFacade) {
		super(tsaFacade);
		_identificationPort = Short.valueOf(_props
				.getProperty("TSAClient.PacketInPort"));
	}

	@Override
	void waitForInstructions() {
		DatagramSocket ds = null;
		try {
			ds = new DatagramSocket(_identificationPort);

			byte[] buf = new byte[256];
			DatagramPacket pkt = new DatagramPacket(buf, buf.length);
			while (true) {
				ds.receive(pkt);
				handleIncomingMessage(new String(pkt.getData()));
			}
		} catch (SocketException e) {

		} catch (IOException e) {
			ds.close();
		}

	}

	@Override
	void connectToTSA() throws Exception {
		// udp nothing to do here

	}

	@Override
	boolean sendMessage(String msg) {
		DatagramSocket socket = null;
		try {
			socket = new DatagramSocket();

			byte[] buf = new byte[1000];
			buf = msg.getBytes();
			InetAddress address = InetAddress.getByName(IGNORED_ADDRESS);
			DatagramPacket packet = new DatagramPacket(buf, buf.length,
					address, _identificationPort);
			socket.send(packet);
			socket.close();
			return true;
		} catch (Exception e) {
			LOGGER.error("cant send message: " + e.getMessage());
		}
		return false;

	}

}
