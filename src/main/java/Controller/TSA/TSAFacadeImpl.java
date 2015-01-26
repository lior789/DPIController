package Controller.TSA;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import Common.GenericChainNode;
import Common.IChainNode;
import Common.Middlebox;
import Common.ServiceInstance;
import Common.Protocol.TSA.RawPolicyChain;
import Controller.DPIController;
import Controller.PolicyChain;

public class TSAFacadeImpl implements ITSAFacade {

	private static final Logger LOGGER = Logger.getLogger(TSAFacadeImpl.class);

	private List<PolicyChain> _currentChains;
	private final DPIController _dpiController;
	private final TsaClientThread _tsaClient;

	private List<RawPolicyChain> _currentRawChains;

	public TSAFacadeImpl(DPIController dpiController) {
		this._dpiController = dpiController;
		_currentRawChains = new LinkedList<RawPolicyChain>();
		_tsaClient = new TSASocketClient(this);
		_tsaClient.start();
	}

	@Override
	public boolean updatePolicyChains(List<PolicyChain> chains) {
		List<RawPolicyChain> rawChains = generateRawChains(chains);
		_tsaClient.sendPolicyChains(rawChains);
		LOGGER.info("send tsa message " + rawChains);
		return true;
	}

	void setPolicyChains(List<RawPolicyChain> rawChains) {
		_currentRawChains = rawChains;
		this.refreshPolicyChains();
	}

	private PolicyChain generatePolicyChain(RawPolicyChain rawChain) {
		List<IChainNode> knownNodes = new LinkedList<IChainNode>();
		knownNodes.addAll(_dpiController.getAllMiddleBoxes());
		knownNodes.addAll(_dpiController.getAllInstances());
		Map<InetAddress, IChainNode> knownNodesMap = new HashMap<InetAddress, IChainNode>();
		for (IChainNode node : knownNodes) {
			knownNodesMap.put(node.GetAddress(), node);
		}
		List<IChainNode> resultChain = new LinkedList<IChainNode>();
		for (InetAddress rawNode : rawChain.chain) {
			resultChain.add(knownNodesMap.containsKey(rawNode) ? knownNodesMap
					.get(rawNode) : new GenericChainNode(rawNode));
		}
		return new PolicyChain(resultChain, rawChain.trafficClass);
	}

	private List<RawPolicyChain> generateRawChains(List<PolicyChain> chains) {
		List<RawPolicyChain> result = new LinkedList<RawPolicyChain>();
		for (PolicyChain policyChain : chains) {
			RawPolicyChain tmp = new RawPolicyChain();
			tmp.trafficClass = policyChain.trafficClass;
			tmp.chain = new LinkedList<InetAddress>();
			for (IChainNode node : policyChain.chain) {
				tmp.chain.add(node.GetAddress());
			}
			result.add(tmp);
		}
		return result;
	}

	@Override
	public List<PolicyChain> getPolicyChains() {
		return _currentChains;
	}

	@Override
	public List<PolicyChain> modifyPolicyChains(List<PolicyChain> currentChains) {

		List<PolicyChain> newChains = new LinkedList<PolicyChain>();

		for (PolicyChain currentChain : currentChains) {
			List<IChainNode> newChain = new LinkedList<IChainNode>();
			for (IChainNode host : currentChain.chain) {
				handlePolicyNode(newChain, host);
			}
			newChains.add(new PolicyChain(newChain, currentChain.trafficClass));
		}
		return newChains;
	}

	private void handlePolicyNode(List<IChainNode> newChain, IChainNode host) {
		if (host instanceof Middlebox) {
			List<ServiceInstance> instances = _dpiController
					.getNeededInstances((Middlebox) host);
			for (ServiceInstance serviceInstance : instances) {
				if (serviceInstance != null
						&& !newChain.contains(serviceInstance))
					newChain.add(serviceInstance);
			}
			newChain.add(host);
		} else if (host instanceof ServiceInstance) {
			return;
		} else if (host instanceof GenericChainNode) {
			newChain.add(host);
		} else {
			LOGGER.warn("unknown policy node : " + host);
		}
	}

	@Override
	public void refreshPolicyChains() {
		_currentChains = new LinkedList<PolicyChain>();
		for (RawPolicyChain rawChain : _currentRawChains) {
			_currentChains.add(generatePolicyChain(rawChain));
		}
		_dpiController.updatePolicyChains(_currentChains);
	}

}
