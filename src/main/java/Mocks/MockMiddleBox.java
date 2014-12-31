package Mocks;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import Common.Protocol.JsonUtils;
import Common.Protocol.MatchRule;
import Common.Protocol.Middlebox.MiddleboxDeregister;
import Common.Protocol.Middlebox.MiddleboxMessage;
import Common.Protocol.Middlebox.MiddleboxMessageFactory;
import Common.Protocol.Middlebox.MiddleboxRegister;
import Common.Protocol.Middlebox.MiddleboxRulesetAdd;
import Common.Protocol.Middlebox.MiddleboxRulesetRemove;

/**
 * Created by Lior on 12/11/2014.
 */
public class MockMiddleBox {

	private final MiddleboxMessageFactory _messageFactory;
	private final InetAddress _controllerIp;
	private final int _controllerPort;
	private final String _id;
	private final String _name;
	private Socket _socket;
	private boolean _waitForInput = true;
	private PrintWriter _sendOut = null;
	private final String USAGE = "exit/add-rules rid,pattern[,regex] .../remove-rules rid1,rid2,..";

	public MockMiddleBox(InetAddress controllerIp, int controllerPort,
			String id, String name) {

		_controllerIp = controllerIp;
		_controllerPort = controllerPort;
		_id = id;
		_name = name;
		_messageFactory = new MiddleboxMessageFactory(id, name);
	}

	/**
	 * wait for action from ui add/remove rules
	 */
	public void run() {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				MiddleboxDeregister msg = _messageFactory
						.createDeregistration();
				sendMessageToController(msg);
			}
		});
		try {
			_socket = new Socket(_controllerIp.getHostAddress(),
					_controllerPort);
			_sendOut = new PrintWriter(_socket.getOutputStream(), true);
			MiddleboxRegister msg = _messageFactory.createRegistration();
			sendMessageToController(msg);
			new LoopThread("icmp").run();
			waitForInput();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private void sendMessageToController(MiddleboxMessage msg) {
		String registerMsg = JsonUtils.toJson(msg);
		_sendOut.println(registerMsg);
	}

	private void waitForInput() throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		while (_waitForInput) {
			System.out.println("Enter command:");
			String command = br.readLine();
			String[] commandArgs = command.split(" ");
			boolean isValid;
			switch (commandArgs[0]) {
			case "exit":
				isValid = handleExitCommand(commandArgs);
				break;
			case "add-rules":
				isValid = handleAddRuleCommand(commandArgs);
				break;
			case "remove-rules":
				isValid = handleRemoveRulesCommand(commandArgs);
				break;
			default:
				isValid = false;
			}
			if (!isValid) {
				System.out.println("Unknown command");
				System.out.println(USAGE);
			}
		}
		System.out.println("Adios!");
	}

	private boolean handleRemoveRulesCommand(String[] commandArgs) {
		if (commandArgs.length != 2)
			return false;
		String rulesString = commandArgs[1];
		List<String> rules = Arrays.asList(rulesString.split(","));
		MiddleboxRulesetRemove msg = _messageFactory.createRulesetRemove(rules);
		this.sendMessageToController(msg);
		return true;
	}

	private boolean handleAddRuleCommand(String[] commandArgs) {
		if (commandArgs.length < 2)
			return false;
		List<MatchRule> rules = new ArrayList<MatchRule>();
		for (int i = 1; i < commandArgs.length; i++) {
			MatchRule rule = parseRule(commandArgs[i]);
			if (rule == null)
				return false;
			rules.add(rule);
		}
		MiddleboxRulesetAdd msg = _messageFactory.createRulesetAdd(rules);
		sendMessageToController(msg);
		return true;
	}

	private MatchRule parseRule(String ruleArg) {
		String[] ruleParams = ruleArg.split(",");
		if (ruleParams.length != 2 && ruleParams.length != 3)
			return null;
		MatchRule rule = new MatchRule(ruleParams[0]);
		rule.pattern = ruleParams[1];
		if (ruleParams.length == 3) {
			if (ruleParams[2].equals("regex")) {
				rule.is_regex = true;
			} else
				return null;
		} else {
			rule.is_regex = false;
		}
		return rule;
	}

	private boolean handleExitCommand(String[] commandArgs) {
		MiddleboxDeregister msg = _messageFactory.createDeregistration();
		sendMessageToController(msg);
		_waitForInput = false;
		return true;
	}
}
