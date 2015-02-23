package wrappers;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.LinkedList;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;

import Mocks.ListenerMockThread;
import Mocks.MockMiddleBox;

public class IDSWrapper {
	public static void main(String[] args) throws FileNotFoundException,
			IOException {
		IDSWrapperArgs params = new IDSWrapperArgs();
		JCommander argsParser = new JCommander(params);
		try {
			argsParser.parse(args);
		} catch (ParameterException e) {
			System.out.println(e.getMessage());
			argsParser.usage();
			return;
		}
		ExecutableWrapper processHandler = startIDS(params);
		try {
			InetAddress controllerIp = Inet4Address
					.getByName(params.controller);
			short controllerPort = params.controllerPort;

			MockMiddleBox middleBoxWrapper = new MockMiddleBox(controllerIp,
					controllerPort, params.id, params.getName());
			middleBoxWrapper.start();
			if (params.printPackets)
				ListenerMockThread.startPrintingIncomingPackets(params.bpf,
						params.getInInterface());

			if (params.rulesFile != null) {
				middleBoxWrapper.loadRulesFile(params.rulesFile,
						params.maxRules);
			}
			middleBoxWrapper.join();

		} catch (Exception e) {
			e.printStackTrace();
			argsParser.usage();
		}
		processHandler.stopProcess();

	}

	private static ExecutableWrapper startIDS(IDSWrapperArgs params)
			throws FileNotFoundException, IOException {
		ExecutableWrapper processHandler = new ExecutableWrapper("/moly_ids",
				"ids_middlebox.exe");
		LinkedList<String> idsArgs = new LinkedList<String>();
		idsArgs.add("in=" + params.getInInterface());
		idsArgs.add("out=" + params.getOutInterface());
		processHandler.runProcess(idsArgs);
		return processHandler;
	}
}
