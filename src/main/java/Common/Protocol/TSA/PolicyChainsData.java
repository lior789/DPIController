package Common.Protocol.TSA;

import java.util.List;

import Common.Protocol.DPIProtocolMessage;

public class PolicyChainsData extends DPIProtocolMessage {
	public PolicyChainsData(List<RawPolicyChain> result) {
		chains = result;
	}

	public List<RawPolicyChain> chains;
}
