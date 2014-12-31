package Controller;

import java.net.InetAddress;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import Common.Middlebox;
import Common.ServiceInstance;
import Common.Protocol.MatchRule;

/**
 * This class is the DPIcontroller u main class, the rules of this class: 1.
 * handle input rules from the middlebox - using ControllerThread 2. keep track
 * of all the match rules (patterns) - using the MatchRulesRepository 3. updates
 * the dpi-services on the Match Rules 4. load balance the dpi-services 5.
 * altering the policy chain of the sdn controller, making each packet
 * corresponding to one of the registered middleboxes to go through the relevant
 * service Created by Lior on 12/11/2014.
 */
public class DPIController {

	private static final Logger LOGGER = Logger.getLogger(DPIController.class);
	private final DPIForeman _foreman; // handles work between instances
	private final MiddleboxRepository _middleboxes; // rules per middlebox
	private final DPIServer _server; // handle the connections with middlebox
										// and services
	private final ITSAFacade _tsa;

	/**
	 * @param port
	 *            on which port the controller is listening to messages
	 */
	public DPIController(int port) {
		_middleboxes = new MiddleboxRepository();
		_server = new DPIServer(this, port);
		_foreman = new DPIForeman(new SimpleLoadBalanceStrategy(), _server);
		_tsa = new TSAFacadeImpl();
	}

	public void registerMiddlebox(Middlebox mb) {
		if (!_middleboxes.addMiddlebox(mb)) {
			LOGGER.warn("middlebox already exists: " + mb.id);
			return;
		}
		LOGGER.info("middlebox added: " + mb.name);
		this.updateTSA();
	}

	public void deregisterMiddlebox(Middlebox mb) {
		LOGGER.trace(String
				.format("Middlebox %s is going to be removed", mb.id));
		List<MatchRule> removedRules = _middleboxes.removeMiddlebox(mb);
		if (removedRules == null) {
			LOGGER.warn(String.format("no such middlebox %s", mb.id));
			return;
		}
		_foreman.removeJobs(removedRules);
		this.updateTSA();
	}

	public void removeRules(Middlebox mb, List<MatchRule> rules) {
		if (!_middleboxes.removeRules(mb, rules)) {
			LOGGER.warn("problem while removing rules for middlebox " + mb.id);
			return;
		}
		_foreman.removeJobs(rules);
	}

	public void addRules(Middlebox mb, List<MatchRule> rules) {
		if (!_middleboxes.addRules(mb, rules)) {
			LOGGER.warn(String.format("no such middlebox %s", mb.id));
			return;
		}
		_foreman.addJobs(rules);
	}

	public void registerInstance(ServiceInstance instance) {
		if (!_foreman.addWorker(instance)) {
			LOGGER.warn("instance already registered: " + instance);
		}
		LOGGER.info("instance added: " + instance.name);
		this.updateTSA();
	}

	public void deregisterInstance(ServiceInstance instance) {
		_foreman.removeWorker(instance);
		// TODO:check if instance exists
		this.updateTSA();
	}

	public void run() {
		_server.run();
	}

	/**
	 * updates the traffic steering application on the order all packets should
	 * traverse in the future: add order between middlebox , and traffic class
	 * (ie. dst port 80) -> (configuration)
	 */
	public void updateTSA() {
		Collection<ServiceInstance> instances = _foreman.getAllInstnaces();
		Collection<Middlebox> middleboxes = getPolicyChain();
		List<InetAddress> result = new LinkedList<InetAddress>();
		for (ServiceInstance instance : instances) {
			result.add(_server.getAddress(instance));
		}
		for (Middlebox mb : middleboxes) {
			result.add(_server.getAddress(mb));
		}
		LOGGER.info("going to update policy chain to: " + result.toString());
		_tsa.applyPolicyChain(result);

	}

	/**
	 * returns ordered collection of the registered middleboxes, currently
	 * random order
	 * 
	 * @return
	 */
	private Collection<Middlebox> getPolicyChain() {
		return this._middleboxes.getAllMiddleboxes();
	}

}
