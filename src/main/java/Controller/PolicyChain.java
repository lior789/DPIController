package Controller;

import java.util.List;

import Common.IChainNode;

public class PolicyChain {

	public String trafficClass;

	public PolicyChain(List<IChainNode> chain) {
		this.chain = chain;
	}

	public PolicyChain(List<IChainNode> resultChain, String trafficClass) {
		chain = resultChain;
		// TODO Auto-generated constructor stub
		this.trafficClass = trafficClass;
	}

	public List<IChainNode> chain;
}
