package Controller.DPIForeman;

import Common.Protocol.MatchRule;
import Common.ServiceInstance;
import Controller.InternalMatchRule;

import java.util.*;

/**
 * Created by Lior on 24/11/2014.
 */
public class InstanceRepository {
	@Override
	public String toString() {
		return "InstanceRepository [_instancesMap=" + _instancesMap + "]";
	}

	private final Map<ServiceInstance, List<InternalMatchRule>> _instancesMap;
	private final Map<InternalMatchRule, ServiceInstance> _rulesMap;

	public InstanceRepository() {
		_instancesMap = new HashMap<ServiceInstance, List<InternalMatchRule>>();
		_rulesMap = new HashMap<InternalMatchRule, ServiceInstance>();
	}

	public void addInstance(ServiceInstance worker) {
		_instancesMap.put(worker, new LinkedList<InternalMatchRule>());
	}

	public List<InternalMatchRule> removeInstance(
			ServiceInstance removedInstance) {
		List<InternalMatchRule> matchRules = _instancesMap.get(removedInstance);
		for (MatchRule matchRule : matchRules) {
			_rulesMap.remove(matchRule);
		}
		_instancesMap.remove(removedInstance);
		return matchRules;
	}

	public ServiceInstance getInstance(MatchRule rule) {
		return _rulesMap.get(rule);
	}

	public List<InternalMatchRule> getRules(ServiceInstance worker) {
		return _instancesMap.get(worker);
	}

	public void removeRule(MatchRule rule) {
		ServiceInstance instance = _rulesMap.get(rule);
		if (instance == null) {
			return;
		}
		List<InternalMatchRule> rules = _instancesMap.get(instance);
		rules.remove(rule);
		_rulesMap.remove(rule);
	}

	public void addRule(InternalMatchRule rule, ServiceInstance instnace) {
		_instancesMap.get(instnace).add(rule);
		_rulesMap.put(rule, instnace);
	}

	public void addRules(List<InternalMatchRule> rules,
			ServiceInstance mostFreeWorker) {
		for (InternalMatchRule rule : rules) {
			addRule(rule, mostFreeWorker);
		}
	}

	public void removeRules(List<InternalMatchRule> rules) {
		for (MatchRule rule : rules) {
			removeRule(rule);
		}
	}

	public Collection<ServiceInstance> getInstances() {
		return _instancesMap.keySet();
	}
}
