package Controller.TSA;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import Common.GenericChainNode;
import Common.IChainNode;
import Common.Middlebox;
import Common.ServiceInstance;
import Controller.DPIController;
import Controller.PolicyChain;

public class TSAFacadeImpl implements ITSAFacade {

	private static final Logger LOGGER = Logger.getLogger(TSAFacadeImpl.class);
	private static final String IGNORED_ADDRESS = "10.0.0.0";
	private final short _identificationPort = 6666;
	private List<PolicyChain> _currentChains;
	private final DPIController _dpiController;

	public TSAFacadeImpl(DPIController dpiController) {
		this._dpiController = dpiController;
	}

	@Override
	public boolean sendPolicyChains(List<PolicyChain> chains) {
		String msg = generateChainMessage(chains);
		try {
			sendMessage(msg);
			LOGGER.info("send tsa message " + msg);
		} catch (IOException e) {

			LOGGER.error("error while sending policyChain:");
			LOGGER.error(e.getMessage());
			e.printStackTrace();
			return false;
		}
		return true;
	}

	void setPolicyChains(List<RawPolicyChain> rawChains) {
		_currentChains = new LinkedList<PolicyChain>();
		for (RawPolicyChain rawChain : rawChains) {
			_currentChains.add(generatePolicyChain(rawChain));
		}
		_dpiController.updatePolicyChains(_currentChains);
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

	private void sendMessage(String msg) throws IOException {
		DatagramSocket socket = new DatagramSocket();
		byte[] buf = new byte[1000];
		buf = msg.getBytes();
		InetAddress address = InetAddress.getByName(IGNORED_ADDRESS);
		DatagramPacket packet = new DatagramPacket(buf, buf.length, address,
				_identificationPort);
		socket.send(packet);
		socket.close();

	}

	private String generateChainMessage(List<PolicyChain> chains) {
		String result = "";
		for (PolicyChain policyChain : chains) {
			result += policyChain.trafficClass + " -> ";
			for (IChainNode node : policyChain.chain) {
				result += node.GetAddress().getHostAddress() + ",";
			}
			result.substring(0, result.length() - 1);
			result += System.lineSeparator();
		}
		return result;
	}

	@Override
	public List<PolicyChain> getPolicyChains() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<PolicyChain> generateDPIPolicyChains(
			List<PolicyChain> currentChains) {

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
				if (!newChain.contains(serviceInstance))
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

	private List<InetAddress> removeInstancesFromChain(
			List<InetAddress> hostChain, Collection<ServiceInstance> instnaces) {
		List<InetAddress> result = new LinkedList<InetAddress>();
		Collection<ServiceInstance> instances = instnaces;
		List<InetAddress> instancesHost = new ArrayList<InetAddress>(
				instances.size());
		for (ServiceInstance instance : instances) {
			instancesHost.add(instance.address);
		}
		for (InetAddress host : hostChain) {
			if (!instancesHost.contains(host)) {
				result.add(host);
			}
		}
		return result;
	}
}
