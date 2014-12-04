package Controller;

import Common.DPILogger;
import Common.Protocol.MatchRule;
import Common.ServiceInstance;

import java.util.*;

/**
 * this load balance strategy always asigns new rules to the instance with the least rules at the moment
 * Created by Lior on 23/11/2014.
 */
public class SimpleLoadBalanceStrategy implements ILoadBalanceStrategy {

    private DPIForeman _foreman;
    private final Map<ServiceInstance, Integer> _instancesLoad;

    public SimpleLoadBalanceStrategy() {
        _instancesLoad = new HashMap<ServiceInstance, Integer>();
    }

    @Override
    public void instanceAdded(ServiceInstance instance) {
        _instancesLoad.put(instance, 0);
        return;
    }

    @Override
    public void instanceRemoved(ServiceInstance instance, List<MatchRule> removedRules) {
        this.addRules(removedRules);
        DPILogger.LOGGER.trace("Instances state after change: \n " + _foreman.toString());
    }

    @Override
    public void removeRules(List<MatchRule> removedRules) {
        Set<MatchRule> distinctRules = new HashSet<MatchRule>(removedRules);
        for (MatchRule rule : distinctRules) {
            ServiceInstance worker = _foreman.getInstance(rule);
            Integer currentLoad = _instancesLoad.get(worker);
            _instancesLoad.put(worker, currentLoad - 1);

        }
        _foreman.deallocateRule(removedRules);
        DPILogger.LOGGER.info(String.format("%s removed rules:\n %s", removedRules.size(), removedRules));
        DPILogger.LOGGER.trace("Instances state after change: \n " + _foreman.toString());
    }

    @Override
    public void addRules(List<MatchRule> rules) {
        ServiceInstance mostFreeWorker = findMostFreeInstance();
        _foreman.assignRules(rules, mostFreeWorker);
        List<MatchRule> existingRules = _foreman.getRules(mostFreeWorker);
        _instancesLoad.put(mostFreeWorker, existingRules.size());

        DPILogger.LOGGER.info(String.format("added %s rules to worker %s", rules.size(), mostFreeWorker.id));
        DPILogger.LOGGER.trace("Instances state after change: \n " + _foreman.toString());
    }

    @Override
    public void setForeman(DPIForeman foreman) {
        _foreman = foreman;
    }

    /**
     * @return the worker with the minimum rules
     */
    private ServiceInstance findMostFreeInstance() {
        int min_value = Integer.MAX_VALUE;
        ServiceInstance minWorker = null;
        for (Map.Entry<ServiceInstance, Integer> workerJobs : _instancesLoad.entrySet()) {
            if (workerJobs.getValue() == 0) { //free worker
                return workerJobs.getKey();
            }
            if (workerJobs.getValue() < min_value) {
                min_value = workerJobs.getValue();
                minWorker = workerJobs.getKey();
            }
        }
        return minWorker;
    }

}
