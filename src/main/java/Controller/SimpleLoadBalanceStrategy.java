package Controller;

import Common.DPILogger;
import Common.Protocol.MatchRule;

import java.util.*;

/**
 * this load balance strategy always asigns new rules to the instance with the least rules at the moment
 * Created by Lior on 23/11/2014.
 */
public class SimpleLoadBalanceStrategy implements ILoadBalanceStrategy {
    // TODO: return rule command
    private final InstanceRepository _instances;
    private final Map<InstanceData, Integer> _instancesLoad;

    public SimpleLoadBalanceStrategy(InstanceRepository workerToJobs) {

        _instances = workerToJobs;
        _instancesLoad = new HashMap<InstanceData, Integer>();
    }

    @Override
    public void instanceAdded(InstanceData instance) {
        _instancesLoad.put(instance, 0);
        return;
    }

    @Override
    public void instanceRemoved(InstanceData instance, List<MatchRule> removedRules) {
        this.addRules(removedRules);
        DPILogger.LOGGER.trace("Instances state after change: \n " + _instances.toString());
    }

    @Override
    public void removeRules(List<String> removedRules) {
        for (String ruleId : removedRules) {
            MatchRule rule = new MatchRule(ruleId);
            InstanceData worker = _instances.getInstance(rule);
            List<MatchRule> workerRules = _instances.getRules(worker);
            workerRules.remove(rule);
            _instancesLoad.put(worker, workerRules.size());
            _instances.removeRule(rule);
        }
        DPILogger.LOGGER.info(String.format("%s removed rules:\n %s", removedRules.size(), removedRules));
        DPILogger.LOGGER.trace("Instances state after change: \n " + _instances.toString());
    }

    @Override
    public void addRules(List<MatchRule> rules) {
        InstanceData mostFreeWorker = findMostFreeInstance();
        _instances.addRules(rules, mostFreeWorker);
        List<MatchRule> existingRules = _instances.getRules(mostFreeWorker);
        _instancesLoad.put(mostFreeWorker, existingRules.size());

        DPILogger.LOGGER.info(String.format("added %s rules to worker %s", rules.size(), mostFreeWorker.id));
        DPILogger.LOGGER.trace("Instances state after change: \n " + _instances.toString());
    }

    /**
     * @return the worker with the minimum rules
     */
    private InstanceData findMostFreeInstance() {
        int min_value = Integer.MAX_VALUE;
        InstanceData minWorker = null;
        for (Map.Entry<InstanceData, Integer> workerJobs : _instancesLoad.entrySet()) {
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
