package Controller;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import Common.IChainNode;
import Common.Middlebox;
import Common.ServiceInstance;
import Common.Protocol.MatchRule;
import Controller.DPIForeman.DPIForeman;
import Controller.DPIForeman.IDPIServiceFormen;
import Controller.DPIForeman.ILoadBalanceStrategy;
import Controller.DPIForeman.MinChainsPerInstanceStrategy;
import Controller.DPIServer.IDPIServiceFacade;
import Controller.MatchRuleRepository.MatchRulesRepository;

public class InstancePerChainStrategyTest {

	@Test
	public void sanityTest() {
		IDPIServiceFacade mock = getUselessFacadeMock();
		IDPIServiceFormen foreman = new DPIForeman(mock);
		MatchRulesRepository matchRules = new MatchRulesRepository();

		List<ServiceInstance> instances = Arrays.asList(
				new ServiceInstance("1"), new ServiceInstance("2"),
				new ServiceInstance("3"));
		List<PolicyChain> chains = new LinkedList<PolicyChain>();
		Middlebox middlebox1a = new Middlebox("1a");
		Middlebox middlebox1b = new Middlebox("1b");
		Middlebox middlebox2a = new Middlebox("2a");
		Middlebox middlebox2b = new Middlebox("2b");
		matchRules.addMiddlebox(middlebox1a);
		matchRules.addMiddlebox(middlebox1b);
		matchRules.addMiddlebox(middlebox2a);
		matchRules.addMiddlebox(middlebox2b);
		chains.add(new PolicyChain(Arrays.asList((IChainNode) middlebox1a,
				(IChainNode) middlebox1b)));
		chains.add(new PolicyChain(Arrays.asList((IChainNode) middlebox2a,
				(IChainNode) middlebox2b)));
		ILoadBalanceStrategy strategy = new MinChainsPerInstanceStrategy(
				matchRules);
		foreman.setStrategy(strategy);
		MatchRule rule1a = new MatchRule("a", "1a");
		MatchRule rule1b = new MatchRule("a", "1b");
		MatchRule rule2a = new MatchRule("b", "2a");
		MatchRule rule2b = new MatchRule("b", "2b");
		List<InternalMatchRule> internal1a = matchRules.addRules(middlebox1a,
				Arrays.asList(rule1a));
		foreman.addJobs(internal1a, middlebox1a);
		List<InternalMatchRule> internal2a = matchRules.addRules(middlebox2a,
				Arrays.asList(rule2a));
		foreman.addJobs(internal2a, middlebox2a);
		List<InternalMatchRule> internal1b = matchRules.addRules(middlebox1b,
				Arrays.asList(rule1b));
		foreman.addJobs(internal1b, middlebox1b);
		List<InternalMatchRule> internal2b = matchRules.addRules(middlebox2b,
				Arrays.asList(rule2b));
		foreman.addJobs(internal2b, middlebox2b);

		foreman.setPolicyChains(chains);
		foreman.addWorker(instances.get(0));
		foreman.addWorker(instances.get(1));
		foreman.addWorker(instances.get(2));

		assertEquals(foreman.getNeededInstances(internal1a).get(0),
				instances.get(0));
		assertEquals(foreman.getNeededInstances(internal1b).get(0),
				instances.get(0));
		assertEquals(foreman.getNeededInstances(internal2a).get(0),
				instances.get(1));
		assertEquals(foreman.getNeededInstances(internal2b).get(0),
				instances.get(1));
		assertEquals(0, foreman.getRules(instances.get(2)).size());

	}

	@Test
	public void moreChainsThanInstances() {
		IDPIServiceFacade mock = getUselessFacadeMock();
		IDPIServiceFormen foreman = new DPIForeman(mock);
		MatchRulesRepository matchRules = new MatchRulesRepository();

		List<ServiceInstance> instances = Arrays.asList(
				new ServiceInstance("1"), new ServiceInstance("2"),
				new ServiceInstance("3"));
		List<PolicyChain> chains = new LinkedList<PolicyChain>();
		Middlebox[] middleboxes = getMiddleboxes(6);
		for (Middlebox middlebox : middleboxes) {
			matchRules.addMiddlebox(middlebox);
		}

		chains.add(new PolicyChain(Arrays.asList((IChainNode) middleboxes[0],
				(IChainNode) middleboxes[1])));
		chains.add(new PolicyChain(Arrays.asList((IChainNode) middleboxes[2],
				(IChainNode) middleboxes[3])));
		chains.add(new PolicyChain(Arrays.asList((IChainNode) middleboxes[4],
				(IChainNode) middleboxes[5])));

		ILoadBalanceStrategy strategy = new MinChainsPerInstanceStrategy(
				matchRules);
		foreman.setStrategy(strategy);
		MatchRule[] rules = getMatchRules(6);
		InternalMatchRule[] internalRules = new InternalMatchRule[6];
		for (int i = 0; i < rules.length; i++) {
			internalRules[i] = matchRules.addRules(middleboxes[i],
					Arrays.asList(rules[i])).get(0);
			foreman.addJobs(Arrays.asList(internalRules[i]), middleboxes[i]);
		}

		foreman.setPolicyChains(chains);
		foreman.addWorker(instances.get(0));
		foreman.addWorker(instances.get(1));
		Set<ServiceInstance> neededInstances = new HashSet<ServiceInstance>();
		neededInstances.addAll(foreman.getNeededInstances(Arrays
				.asList(internalRules[0])));
		neededInstances.addAll(foreman.getNeededInstances(Arrays
				.asList(internalRules[2])));
		neededInstances.addAll(foreman.getNeededInstances(Arrays
				.asList(internalRules[5])));
		assertEquals(2, neededInstances.size());
		for (int i = 0; i < internalRules.length; i++) {
			assertEquals(1,
					foreman.getNeededInstances(Arrays.asList(internalRules[i]))
							.size());
		}

	}

	@Test
	public void overlappingChains() {
		IDPIServiceFacade mock = getUselessFacadeMock();
		IDPIServiceFormen foreman = new DPIForeman(mock);
		MatchRulesRepository matchRules = new MatchRulesRepository();
		Middlebox[] middleboxes = getMiddleboxes(5);
		MatchRule[] rules = getMatchRules(5);
		InternalMatchRule[] internalRules = new InternalMatchRule[5];
		for (int i = 0; i < middleboxes.length; i++) {
			Middlebox middlebox = middleboxes[i];
			matchRules.addMiddlebox(middlebox);
			internalRules[i] = matchRules.addRules(middlebox,
					Arrays.asList(rules[i])).get(0);
		}

		ServiceInstance[] instances = getInstances(2);
		List<PolicyChain> chains = new LinkedList<PolicyChain>();

		// mb:1 is joint to both chains
		chains.add(new PolicyChain(Arrays.asList((IChainNode) middleboxes[0],
				(IChainNode) middleboxes[1], (IChainNode) middleboxes[2])));
		chains.add(new PolicyChain(Arrays.asList((IChainNode) middleboxes[3],
				(IChainNode) middleboxes[1], (IChainNode) middleboxes[4])));

		ILoadBalanceStrategy strategy = new MinChainsPerInstanceStrategy(
				matchRules);
		foreman.setStrategy(strategy);

		foreman.setPolicyChains(chains);
		foreman.addWorker(instances[0]);
		foreman.addWorker(instances[1]);

		assertEquals(1,
				foreman.getNeededInstances(Arrays.asList(internalRules[0]))
						.size());

		assertEquals(1,
				foreman.getNeededInstances(Arrays.asList(internalRules[2]))
						.size());
		assertEquals(1,
				foreman.getNeededInstances(Arrays.asList(internalRules[3]))
						.size());
		assertEquals(1,
				foreman.getNeededInstances(Arrays.asList(internalRules[4]))
						.size());
		assertEquals(2,
				foreman.getNeededInstances(Arrays.asList(internalRules[1]))
						.size());

	}

	private ServiceInstance[] getInstances(int count) {
		ServiceInstance[] result = new ServiceInstance[count];
		for (int i = 0; i < count; i++) {
			result[i] = new ServiceInstance(String.valueOf(i));
		}
		return result;

	}

	private static Middlebox[] getMiddleboxes(int count) {
		Middlebox[] result = new Middlebox[count];
		for (int i = 0; i < count; i++) {
			result[i] = new Middlebox(String.valueOf(i));
		}
		return result;
	}

	private static MatchRule[] getMatchRules(int count) {
		MatchRule[] result = new MatchRule[count];
		for (int i = 0; i < count; i++) {
			result[i] = new MatchRule(String.valueOf(i), String.valueOf(i));
		}
		return result;
	}

	private IDPIServiceFacade getUselessFacadeMock() {
		IDPIServiceFacade mock = mock(IDPIServiceFacade.class);
		doNothing().when(mock).assignRules(anyList(),
				(ServiceInstance) anyObject());
		return mock;
	}
}
