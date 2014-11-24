package Controller;

import Common.DPILogger;
import Common.Protocol.MatchRule;

import java.util.List;

/**
 * this class has a key job in the controller
 * it got notified on all the match rules and all the instances
 * and divides the work (patterns) between the instances(DPI-Services)
 * it uses DPIInstancesStrategy to split the work
 * Created by Lior on 20/11/2014.
 */
public class DPIForeman {
    private final InstanceRepository _workers; //rules per instance
    private MiddleboxRepository _middleboxes; //rules per middlebox
    private final ILoadBalanceStrategy _strategy; //rules per instance

    public DPIForeman() {
        _workers = new InstanceRepository();
        _strategy = new SimpleLoadBalanceStrategy(_workers);
        _middleboxes = new MiddleboxRepository();
    }

    public void addWorker(String id, String name) {
        DPILogger.LOGGER.trace(String.format("Instance %s,%s is added", id, name));
        InstanceData worker = new InstanceData(id, name);
        _workers.addInstance(worker);
        _strategy.instanceAdded(worker);
    }

    public void removeWorker(String id) {
        DPILogger.LOGGER.trace(String.format("Instance %s is going to be removed", id));
        InstanceData removedInstance = new InstanceData(id);
        List<MatchRule> rules = _workers.removeInstance(removedInstance);
        _strategy.instanceRemoved(removedInstance, rules);
        DPILogger.LOGGER.info(String.format("%s instance removed", id));
    }

    public boolean addMiddlebox(String id, String name) {
        DPILogger.LOGGER.trace(String.format("Middlebox %s,%s is  going to be added", id, name));
        return _middleboxes.addMiddlebox(id, name);
    }

    public boolean removeMiddlebox(String id) {
        DPILogger.LOGGER.trace(String.format("Middlebox %s is going to be removed", id));
        List<String> removedRules = _middleboxes.removeMiddlebox(id);
        if (removedRules == null) {
            DPILogger.LOGGER.trace(String.format("no such middlebox %s", id));
            return false;
        }
        DPILogger.LOGGER.trace(String.format("%s rules is been removed", removedRules.size()));
        _strategy.removeRules(removedRules);
        return true;
    }

    public boolean removeRules(String id, List<String> rules) {
        DPILogger.LOGGER.trace(String.format("%s rules going to be removed from Middlebox %s", rules.size(), id));
        if (!_middleboxes.removeRules(id, rules)) {
            return false;
        }
        _strategy.removeRules(rules);
        return true;
    }

    public boolean addRules(String id, List<MatchRule> rules) {
        DPILogger.LOGGER.trace(String.format("%s rules going to be added to Middlebox %s", rules.size(), id));
        if (!_middleboxes.addRules(id, rules)) {
            return false;
        }
        _strategy.addRules(rules);
        return true;
    }
}
