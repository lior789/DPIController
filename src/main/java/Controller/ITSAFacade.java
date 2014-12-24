package Controller;

import java.net.InetAddress;
import java.util.List;

public interface ITSAFacade {

	/**
	 * notify the TSA on the new policy chain
	 * 
	 * @param ordered
	 *            list of the addresses of the chain instances
	 * @return
	 */
	boolean applyPolicyChain(List<InetAddress> chain);

}
