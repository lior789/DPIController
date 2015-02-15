package wrappers;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.LinkedList;

import Mocks.MockMiddleBox;

public class IDSWrapper {
	public static void main(String[] args) throws FileNotFoundException,
			IOException {
		String USAGE = "USAGE: controller_Ip controller_port name (mininet host)";
		if (args.length != 3) {
			System.out.println(USAGE);
			return;
		}
		String controllerIpStr = args[0];
		String controllerPortStr = args[1];
		String mbName = args[2];
		try {
			InetAddress controllerIp = Inet4Address.getByName(controllerIpStr);
			int controllerPort = Integer.parseInt(controllerPortStr);

			MockMiddleBox middleBox = new MockMiddleBox(controllerIp,
					controllerPort, mbName, mbName);
			middleBox.start();
			ProcessHandler processHandler = startIDS(mbName);
			while (middleBox.isAlive()) {

			}
			processHandler.stopProcess();

		} catch (Exception e) {
			System.out.println(USAGE);
		}

	}

	private static ProcessHandler startIDS(String mbName)
			throws FileNotFoundException, IOException {
		ProcessHandler processHandler = new ProcessHandler("/moly_ids",
				"ids_middlebox.exe");
		LinkedList<String> idsArgs = new LinkedList<String>();
		String inter = mbName + "-eth0";
		idsArgs.add("in=" + inter);
		idsArgs.add("out=" + inter);
		processHandler.runProcess(idsArgs);
		return processHandler;
	}
}
