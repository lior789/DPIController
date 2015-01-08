package Common;

import java.net.InetAddress;

public class GenericChainNode implements IChainNode {
	public InetAddress address;

	public GenericChainNode(InetAddress rawNode) {
		address = rawNode;
	}

	@Override
	public InetAddress GetAddress() {
		return address;
	}

}
