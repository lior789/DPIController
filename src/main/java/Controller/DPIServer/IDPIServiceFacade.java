package Controller.DPIServer;

import Common.Protocol.MatchRule;
import Common.ServiceInstance;
import Controller.InternalMatchRule;

import java.util.List;

/**
 * Created by Lior on 26/11/2014.
 */
public interface IDPIServiceFacade {

	void deallocateRule(List<InternalMatchRule> instanceRules,
			ServiceInstance instance);

	void assignRules(List<InternalMatchRule> rules, ServiceInstance instance);
}
