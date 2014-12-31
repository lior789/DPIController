package Controller;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import Common.ServiceInstance;
import Common.Protocol.MatchRule;

/**
 * this load balance strategy always asigns new rules to the instance with the
 * least rules at the moment Created by Lior on 23/11/2014.
 */
public class SimpleLoadBalanceStrategy implements ILoadBalanceStrategy {

	private static final Logger LOGGER = Logger
			.getLogger(SimpleLoadBalanceStrategy.class);
	private DPIForeman _foreman;
	private final Map<ServiceInstance, Integer> _instancesLoad;

	public SimpleLoadBalanceStrategy() {
		_instancesLoad = new HashMap<ServiceInstance, Integer>();
	}

	@Override
	public void instanceAdded(ServiceInstance instance) {
		_instancesLoad.put(instance, 0);
		return;
	}

	@Override
	public void instanceRemoved(ServiceInstance instance,
			List<MatchRule> removedRules) {
		_instancesLoad.remove(instance);
		this.addRules(removedRules);
		LOGGER.trace("Instances state after change: \n " + _foreman.toString());
	}

	@Override
	public boolean removeRules(List<MatchRule> removedRules) {
		Set<MatchRule> distinctRules = new HashSet<MatchRule>(removedRules);
		for (MatchRule rule : distinctRules) {
			ServiceInstance worker = _foreman.getInstance(rule);
			if (worker != null) {
				Integer currentLoad = _instancesLoad.get(worker);
				_instancesLoad.put(worker, currentLoad - 1);
			} else {
				LOGGER.warn("rule doesnt exists: " + rule);
			}

		}
		_foreman.deallocateRule(removedRules);
		LOGGER.info(String.format("%s removed rules:\n %s",
				removedRules.size(), removedRules));
		LOGGER.trace("Instances state after change: \n " + _foreman.toString());
		return true;
	}

	@Override
	public boolean addRules(List<MatchRule> rules) {
		if (_instancesLoad.isEmpty()) {
			LOGGER.error("There is no available Workers");
			return false;
		}
		ServiceInstance mostFreeWorker = findMostFreeInstance();
		_foreman.assignRules(rules, mostFreeWorker);
		List<MatchRule> existingRules = _foreman.getRules(mostFreeWorker);
		_instancesLoad.put(mostFreeWorker, existingRules.size());

		LOGGER.info(String.format("added %s rules to worker %s", rules.size(),
				mostFreeWorker.id));
		LOGGER.trace("Instances state after change: \n " + _foreman.toString());
		return true;
	}

	@Override
	public void setForeman(DPIForeman foreman) {
		_foreman = foreman;
	}

	/**
	 * @return the worker with the minimum rules
	 */
	private ServiceInstance findMostFreeInstance() {
		int min_value = Integer.MAX_VALUE;
		ServiceInstance minWorker = null;
		for (Map.Entry<ServiceInstance, Integer> workerJobs : _instancesLoad
				.entrySet()) {
			if (workerJobs.getValue() == 0) { // free worker
				return workerJobs.getKey();
			}
			if (workerJobs.getValue() < min_value) {
				min_value = workerJobs.getValue();
				minWorker = workerJobs.getKey();
			}
		}
		return minWorker;
	}

}
