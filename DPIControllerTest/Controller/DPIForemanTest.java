package Controller;

import Common.Protocol.MatchRule;
import Common.ServiceInstance;
import org.junit.Test;

import static org.mockito.Mockito.*;
import java.util.LinkedList;
import java.util.List;
import static org.junit.Assert.*;

public class DPIForemanTest {

    @org.junit.Test
    public void testLoadBalancing() throws Exception {
        IDPIServiceFacade mock = getUselessFacadeMock();
        DPIForeman foreman = new DPIForeman(new SimpleLoadBalanceStrategy(), mock);

        foreman.addWorker(new ServiceInstance("I1", "Instance1"));
        foreman.addWorker(new ServiceInstance("I2", "Instance2"));
        List<MatchRule> rules = new LinkedList<>();
        rules.add(new MatchRule("1", "a"));
        rules.add(new MatchRule("2", "b"));
        rules.add(new MatchRule("3", "c"));
        rules.add(new MatchRule("4", "d"));

        foreman.addJobs(rules.subList(0, 2));
        foreman.addJobs(rules.subList(2, 4));
        ServiceInstance instance2 = new ServiceInstance("I2");
        ServiceInstance instance1 = new ServiceInstance("I1");
        assertEquals(2, foreman.getRules(instance1).size());
        assertEquals(2, foreman.getRules(instance2).size());
    }

    private IDPIServiceFacade getUselessFacadeMock() {
        IDPIServiceFacade mock = mock(IDPIServiceFacade.class);
        doNothing().when(mock).assignRules(anyList(), (ServiceInstance) anyObject());
        return mock;
    }

    @Test
    public void testNoInstances() throws Exception {
        IDPIServiceFacade mock = getUselessFacadeMock();
        DPIForeman foreman = new DPIForeman(new SimpleLoadBalanceStrategy(), mock);
        List<MatchRule> rules = new LinkedList<>();
        rules.add(new MatchRule("1", "a"));
        rules.add(new MatchRule("2", "b"));
        assertFalse(foreman.addJobs(rules));

    }
}