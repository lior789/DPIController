package Controller;

import Common.Protocol.MatchRule;
import Common.ServiceInstance;

import java.util.List;

/**
 * Created by Lior on 24/11/2014.
 */
public interface ILoadBalanceStrategy {
	void instanceAdded(ServiceInstance instance);

	void instanceRemoved(ServiceInstance instance, List<MatchRule> removedRules);

	boolean removeRules(List<MatchRule> removedRules);

	boolean addRules(List<MatchRule> rules);

	void setForeman(DPIForeman foreman);
}
