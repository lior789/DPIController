package Controller;

import Common.Protocol.MatchRule;

import java.util.List;

/**
 * Created by Lior on 24/11/2014.
 */
public interface ILoadBalanceStrategy {
    void instanceAdded(InstanceData instance);

    void instanceRemoved(InstanceData instance, List<MatchRule> removedRules);

    void removeRules(List<String> removedRules);

    void addRules(List<MatchRule> rules);
}
