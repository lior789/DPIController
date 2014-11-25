package Controller;

import Common.Protocol.MatchRule;

import java.util.List;

/**
 * Created by Lior on 24/11/2014.
 */
public interface ILoadBalanceStrategy {
    void instanceAdded(ServiceInstance instance);

    void instanceRemoved(ServiceInstance instance, List<MatchRule> removedRules);

    void removeRules(List<MatchRule> removedRules);

    void addRules(List<MatchRule> rules);

    void setForeman(DPIForeman foreman);
}
