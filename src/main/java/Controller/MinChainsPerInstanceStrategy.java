package Controller;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.apache.log4j.Logger;

import Common.IChainNode;
import Common.Middlebox;
import Common.ServiceInstance;
import Controller.DPIForeman.IDPIServiceFormen;
import Controller.DPIForeman.ILoadBalanceStrategy;
import Controller.MatchRuleRepository.IMatchRuleRepository;

public class MinChainsPerInstanceStrategy implements ILoadBalanceStrategy {

	private List<PolicyChain> _policyChains;
	private IDPIServiceFormen _foreman;
	private final Map<ServiceInstance, List<PolicyChain>> _instanceChains;
	private final Map<PolicyChain, ServiceInstance> _chainInstance;
	private static final Logger LOGGER = Logger
			.getLogger(MinChainsPerInstanceStrategy.class);
	private final IMatchRuleRepository _matchRules;

	public MinChainsPerInstanceStrategy(IMatchRuleRepository matchRules) {
		this._matchRules = matchRules;
		_instanceChains = new HashMap<ServiceInstance, List<PolicyChain>>();
		_chainInstance = new HashMap<PolicyChain, ServiceInstance>();
	}

	@Override
	public void instanceAdded(ServiceInstance instance) {
		if (_instanceChains.containsKey(instance)) {
			LOGGER.warn("instance exists: " + instance);
			return;
		}
		_instanceChains.put(instance, new LinkedList<PolicyChain>());
		balanceChains();

	}

	private void balanceChains() {
		if (_policyChains == null || _policyChains.size() == 0
				|| _instanceChains.isEmpty()) {
			return;
		}
		List<PolicyChain> allChains = new LinkedList<PolicyChain>(_policyChains);
		Stack<ServiceInstance> instancesStack = new Stack<ServiceInstance>();
		instancesStack.addAll(_instanceChains.keySet());
		int instancesNeeded = Math.min(allChains.size(), instancesStack.size());

		int chainsPerInstance = _policyChains.size() / instancesNeeded;

		List<ServiceInstance> usedInstances = new LinkedList<ServiceInstance>();
		for (int i = 0; i < instancesNeeded; i++) {
			ServiceInstance instance = instancesStack.pop();
			List<PolicyChain> instanceChains = new LinkedList<PolicyChain>(
					allChains.subList(0, chainsPerInstance));
			allChains.subList(0, chainsPerInstance).clear();
			_instanceChains.put(instance, instanceChains);
			_foreman.assignRules(getMatchRules(instanceChains), instance);
			usedInstances.add(instance);
		}
		// handle the reminder
		for (int i = 0; i < allChains.size(); i++) {
			_instanceChains.get(usedInstances.get(i)).add(allChains.get(i));
		}

	}

	private List<InternalMatchRule> getMatchRules(
			List<PolicyChain> instanceChains) {
		List<InternalMatchRule> result = new LinkedList<InternalMatchRule>();
		for (PolicyChain policyChain : instanceChains) {
			for (IChainNode node : policyChain.chain) {
				if (node instanceof Middlebox)
					result.addAll(_matchRules.getMatchRules((Middlebox) node));
			}
		}
		return result;
	}

	private boolean isNoInstances() {
		return _instanceChains.keySet().isEmpty();
	}

	@Override
	public void instanceRemoved(ServiceInstance instance,
			List<InternalMatchRule> instanceRules) {
		_instanceChains.remove(instance);
		if (isNoInstances()) {
			LOGGER.warn("no instances");
		}
		balanceChains();
	}

	@Override
	public boolean removeRules(List<InternalMatchRule> removedRules,
			Middlebox mb) {

		_foreman.deallocateRule(removedRules);
		return true;
	}

	@Override
	public boolean addRules(List<InternalMatchRule> rules, Middlebox mb) {
		if (_policyChains == null || _policyChains.isEmpty()) {
			LOGGER.error("cant handle rules if no policy chain exists");
			return false;
		}
		if (isNoInstances()) {
			LOGGER.error("cant handle rules there are no instances");
			return false;
		}
		ServiceInstance instnace = null;
		for (PolicyChain chain : _policyChains) {
			if (chain.chain.contains(mb)) {
				instnace = _chainInstance.get(chain);
				break;
			}
		}
		_foreman.assignRules(rules, instnace);
		return true;
	}

	@Override
	public void setForeman(IDPIServiceFormen foreman) {
		_foreman = foreman;
	}

	@Override
	public void setPolicyChains(List<PolicyChain> chains) {
		_policyChains = chains;
		balanceChains();
	}

}
