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

    private final ILoadBalanceStrategy _strategy; //rules per instance
    private final DPIController _controller;

    public DPIForeman(ILoadBalanceStrategy strategy, DPIController controller) {
        _controller = controller;
        _workers = new InstanceRepository();
        _strategy = strategy;
        _strategy.setForeman(this);
    }


    public void addWorker(ServiceInstance worker) {
        DPILogger.LOGGER.trace(String.format("Instance %s,%s is added", worker.id, worker.name));
        _workers.addInstance(worker);
        _strategy.instanceAdded(worker);
    }

    public void removeWorker(ServiceInstance removedWorker) {
        DPILogger.LOGGER.trace(String.format("Instance %s is going to be removed", removedWorker.id));

        List<MatchRule> rules = _workers.removeInstance(removedWorker);
        _strategy.instanceRemoved(removedWorker, rules);
        DPILogger.LOGGER.info(String.format("%s instance removed", removedWorker.id));
    }

    public void removeJobs(List<MatchRule> rules) {
        _strategy.removeRules(rules);
    }

    public boolean addJobs(List<MatchRule> rules) {
        _strategy.addRules(rules);
        return true;
    }

    public ServiceInstance getInstance(MatchRule rule) {
        return _workers.getInstance(rule);
    }

    public List<MatchRule> getRules(ServiceInstance worker) {
        return _workers.getRules(worker);
    }

    public void deallocateRule(List<MatchRule> rules) {
        for (MatchRule rule : rules) {
            _controller.deallocateRule(rules, _workers.getInstance(rule));
            _workers.removeRule(rule);
        }
    }

    public void assignRules(List<MatchRule> rules, ServiceInstance worker) {
        _controller.assignRules(rules, worker);
        _workers.addRules(rules, worker);
    }

    @Override
    public String toString() {
        return _workers.toString();
    }
}
