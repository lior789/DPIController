package Controller.DPIForeman;

import Common.Protocol.MatchRule;
import Common.ServiceInstance;
import Controller.InternalMatchRule;

import java.util.List;

/**
 * Created by Lior on 24/11/2014.
 */
public interface ILoadBalanceStrategy {
	void instanceAdded(ServiceInstance instance);

	void instanceRemoved(ServiceInstance instance,
			List<InternalMatchRule> removedRules);

	boolean removeRules(List<InternalMatchRule> removedRules);

	boolean addRules(List<InternalMatchRule> internalRules);

	void setForeman(DPIForeman foreman);
}
