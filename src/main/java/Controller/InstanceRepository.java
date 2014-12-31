package Controller;

import Common.Protocol.MatchRule;
import Common.ServiceInstance;

import java.util.*;

/**
 * Created by Lior on 24/11/2014.
 */
public class InstanceRepository {
	@Override
	public String toString() {
		return "InstanceRepository [_instancesMap=" + _instancesMap + "]";
	}

	private final Map<ServiceInstance, List<MatchRule>> _instancesMap;
	private final Map<MatchRule, ServiceInstance> _rulesMap;

	public InstanceRepository() {
		_instancesMap = new HashMap<ServiceInstance, List<MatchRule>>();
		_rulesMap = new HashMap<MatchRule, ServiceInstance>();
	}

	public void addInstance(ServiceInstance worker) {
		_instancesMap.put(worker, new LinkedList<MatchRule>());
	}

	public List<MatchRule> removeInstance(ServiceInstance removedInstance) {
		List<MatchRule> matchRules = _instancesMap.get(removedInstance);
		for (MatchRule matchRule : matchRules) {
			_rulesMap.remove(matchRule);
		}
		_instancesMap.remove(removedInstance);
		return matchRules;
	}

	public ServiceInstance getInstance(MatchRule rule) {
		return _rulesMap.get(rule);
	}

	public List<MatchRule> getRules(ServiceInstance worker) {
		return _instancesMap.get(worker);
	}

	public void removeRule(MatchRule rule) {
		ServiceInstance instance = _rulesMap.get(rule);
		if (instance == null) {
			return;
		}
		List<MatchRule> rules = _instancesMap.get(instance);
		rules.remove(rule);
		_rulesMap.remove(rule);
	}

	public void addRule(MatchRule rule, ServiceInstance instnace) {
		_instancesMap.get(instnace).add(rule);
		_rulesMap.put(rule, instnace);
	}

	public void addRules(List<MatchRule> rules, ServiceInstance mostFreeWorker) {
		for (MatchRule rule : rules) {
			addRule(rule, mostFreeWorker);
		}
	}

	public void removeRules(List<MatchRule> rules) {
		for (MatchRule rule : rules) {
			removeRule(rule);
		}
	}

	public Collection<ServiceInstance> getInstances() {
		return _instancesMap.keySet();
	}
}
