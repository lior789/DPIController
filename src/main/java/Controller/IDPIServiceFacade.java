package Controller;

import Common.Protocol.MatchRule;
import Common.ServiceInstance;

import java.util.List;

/**
 * Created by Lior on 26/11/2014.
 */
public interface IDPIServiceFacade {
    void deallocateRule(List<MatchRule> rules, ServiceInstance instance);

    void assignRules(List<MatchRule> rules, ServiceInstance instance);
}
