package wrappers;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import Common.JsonUtils;
import Common.Protocol.DPIProtocolMessage;
import Common.Protocol.MatchRule;
import Common.Protocol.Controller.RuleAdd;
import Common.Protocol.Controller.RuleRemove;
import Common.Protocol.Service.InstanceDeregister;
import Common.Protocol.Service.InstanceMessage;
import Common.Protocol.Service.InstanceMessageFactory;
import Common.Protocol.Service.InstanceRegister;

public class DPIServiceWrapper {

	private final InetAddress _controllerIp;
	private final int _controllerPort;
	private final String _id;
	private final String _name;
	private final InstanceMessageFactory _messageFactory;
	private Socket _socket;
	private PrintWriter _sendOut;
	private final HashMap<Integer, MatchRule> _rules;
	private final String RULES_FILE = "./rules.json";
	private final String INTERFACE;
	private final ProcessHandler _processHandler;
	private final int MAX_RULES = 1000;

	public DPIServiceWrapper(InetAddress controllerIp, int controllerPort,
			String id, String name) throws FileNotFoundException, IOException {
		_controllerIp = controllerIp;
		_controllerPort = controllerPort;
		_id = id;
		_name = name;
		INTERFACE = name + "-eth0";
		_messageFactory = new InstanceMessageFactory(id, name);
		_rules = new HashMap<>();
		_processHandler = new ProcessHandler("/moly_service");
	}

	public void run() {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				InstanceDeregister msg = _messageFactory.createDeregistration();
				sendMessageToController(msg);
				_processHandler.stopProcess();
			}
		});

		try {
			_socket = new Socket(_controllerIp.getHostAddress(),
					_controllerPort);
			_sendOut = new PrintWriter(_socket.getOutputStream(), true);
			System.out.println(String.format("dpi service %s:%s is up!", _id,
					_name));
			InstanceRegister msg = _messageFactory.createRegistration();
			sendMessageToController(msg);
			waitForInstructions();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private void sendMessageToController(InstanceMessage msg) {
		String msgStr = JsonUtils.toJson(msg);
		System.out.println("Sending : " + msgStr);
		_sendOut.println(msgStr);
	}

	/**
	 * waits and prints every message received by controller
	 * 
	 * @throws IOException
	 */
	private void waitForInstructions() throws IOException {
		BufferedReader in = new BufferedReader(new InputStreamReader(
				_socket.getInputStream()));
		String inputLine;
		while ((inputLine = in.readLine()) != null) {
			System.out.println("got: " + inputLine);
			DPIProtocolMessage controllerMsg = JsonUtils.fromJson(inputLine);
			handleMessage(controllerMsg);

		}
	}

	private void handleMessage(DPIProtocolMessage controllerMsg) {
		if (controllerMsg instanceof RuleAdd) {
			addRules(((RuleAdd) controllerMsg).rules);
		} else if (controllerMsg instanceof RuleRemove) {
			removeRules(((RuleRemove) controllerMsg).rules);
		} else {
			System.out.println("Unknown message from controller:");
			System.out.println(controllerMsg);
			return;
		}
	}

	private void reloadService() {
		writeRulesToFile(_rules.values(), RULES_FILE);
		try {
			String.format("rules=%s in=%s out=%s max=%d", RULES_FILE,
					INTERFACE, INTERFACE, _rules.values().size());
			LinkedList<String> args = new LinkedList<String>();
			args.add("rules=" + RULES_FILE);
			args.add("in=" + INTERFACE);
			args.add("out=" + INTERFACE);
			args.add("max=" + MAX_RULES);
			_processHandler.runProcess(args);
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println(String.format(
				"reloading DPI service with %d rules!", _rules.size()));
	}

	private void writeRulesToFile(Collection<MatchRule> rules, String rulesFile) {
		try {
			PrintWriter outFile = new PrintWriter(new FileWriter(rulesFile));

			for (MatchRule matchRule : rules) {
				String matchStr = matchRule.toString();
				outFile.println(matchStr);
			}
			outFile.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private void removeRules(List<Integer> rules) {
		boolean isRulesChanged = false;
		for (Integer ruleId : rules) {
			MatchRule removed = _rules.remove(ruleId);
			if (removed == null) {
				System.out.println(String.format(
						"ruleID %s not exists, shouldnt happen!", ruleId));
			} else {
				isRulesChanged = true;
				System.out.println(String.format("rule %s been removed",
						removed));
			}
		}
		if (isRulesChanged) {
			reloadService();
		}
	}

	private void addRules(List<MatchRule> rules) {
		boolean isRulesChanged = false;
		for (MatchRule matchRule : rules) {
			if (_rules.containsKey(matchRule.rid)) {
				System.out.println(String.format("ruleid %s already exists",
						matchRule.rid));

			} else {
				_rules.put(matchRule.rid, matchRule);
				isRulesChanged = true;
			}
		}
		if (isRulesChanged) {
			reloadService();
		}
	}

	public static void main(String[] args) throws FileNotFoundException,
			IOException {
		String USAGE = "USAGE: controller_Ip controller_port name";
		if (args.length != 3) {
			System.out.println(USAGE);
			return;
		}
		String controllerIpStr = args[0];
		String controllerPortStr = args[1];
		String serviceName = args[2];
		try {
			InetAddress controllerIp = Inet4Address.getByName(controllerIpStr);
			int controllerPort = Integer.parseInt(controllerPortStr);
			new DPIServiceWrapper(controllerIp, controllerPort, serviceName,
					serviceName).run();
		} catch (UnknownHostException e) {
			System.out.println(controllerIpStr + " is an invalid Ip address");
			System.out.println(USAGE);
		} catch (NumberFormatException e) {
			System.out.println(controllerPortStr
					+ " is an invalid port address");
			System.out.println(USAGE);
		}

	}

}
