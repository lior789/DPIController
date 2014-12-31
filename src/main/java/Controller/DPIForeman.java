package Controller;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import Common.ServiceInstance;
import Common.Protocol.MatchRule;

/**
 * this class has a key job in the controller it got notified on all the match
 * rules and all the instances and divides the work (patterns) between the
 * instances(DPI-Services) it uses DPIInstancesStrategy to split the work
 * Created by Lior on 20/11/2014.
 */
public class DPIForeman {
	private static final Logger LOGGER = Logger.getLogger(DPIForeman.class);

	private final InstanceRepository _workers; // rules per instance

	private final ILoadBalanceStrategy _strategy; // rules per instance
	private final IDPIServiceFacade _controller;

	public DPIForeman(ILoadBalanceStrategy strategy,
			IDPIServiceFacade controller) {
		_controller = controller;
		_workers = new InstanceRepository();
		_strategy = strategy;
		_strategy.setForeman(this);
	}

	public boolean addWorker(ServiceInstance worker) {
		LOGGER.trace(String.format("Instance %s,%s is added", worker.id,
				worker.name));
		if (_workers.getInstances().contains(worker)) {
			return false;
		}
		_workers.addInstance(worker);
		_strategy.instanceAdded(worker);
		return true;
	}

	public void removeWorker(ServiceInstance removedWorker) {
		LOGGER.trace(String.format("Instance %s is going to be removed",
				removedWorker.id));

		List<MatchRule> rules = _workers.removeInstance(removedWorker);
		_strategy.instanceRemoved(removedWorker, rules);
		LOGGER.info(String.format("%s instance removed", removedWorker.id));
	}

	public void removeJobs(List<MatchRule> rules) {
		_strategy.removeRules(rules);
	}

	/**
	 * add match rules to one or more instances using its load balancing
	 * strategy
	 * 
	 * @param rules
	 *            list of matchrules to divide among workers
	 * @return false if no workers(instances) availble
	 */
	public boolean addJobs(List<MatchRule> rules) {
		if (_workers.getInstances().size() == 0) {
			return false;
		}
		_strategy.addRules(rules);
		return true;
	}

	public ServiceInstance getInstance(MatchRule rule) {
		return _workers.getInstance(rule);
	}

	public List<MatchRule> getRules(ServiceInstance worker) {
		return _workers.getRules(worker);
	}

	/**
	 * remove the rules from the instances, including "real" instances
	 * 
	 * @param rules
	 *            list of MatchRules to remove
	 */
	public void deallocateRule(List<MatchRule> rules) {
		HashMap<ServiceInstance, List<MatchRule>> tmp = new HashMap<>();
		for (MatchRule rule : rules) {
			ServiceInstance instance = _workers.getInstance(rule);
			if (instance == null) {
				LOGGER.error("rule not allocated: " + rule);
				continue;
			}
			if (!tmp.containsKey(instance)) {
				tmp.put(instance, new LinkedList<MatchRule>());
			}
			tmp.get(instance).add(rule);
		}
		for (ServiceInstance instance : tmp.keySet()) {
			List<MatchRule> instanceRules = tmp.get(instance);
			_controller.deallocateRule(instanceRules, instance);
		}
		_workers.removeRules(rules);
	}

	public void assignRules(List<MatchRule> rules, ServiceInstance worker) {
		_controller.assignRules(rules, worker);
		_workers.addRules(rules, worker);
	}

	@Override
	public String toString() {
		return _workers.toString();
	}

	public Collection<ServiceInstance> getAllInstnaces() {
		return this._workers.getInstances();
	}
}
