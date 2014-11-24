package Controller;

import Common.Protocol.MatchRule;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by Lior on 24/11/2014.
 */
public class InstanceRepository {
    private final Map<InstanceData, List<MatchRule>> _instancesMap;
    private final Map<MatchRule, InstanceData> _rulesMap;

    public InstanceRepository() {
        _instancesMap = new HashMap<InstanceData, List<MatchRule>>();
        _rulesMap = new HashMap<MatchRule, InstanceData>();
    }

    public void addInstance(InstanceData worker) {
        _instancesMap.put(worker, new LinkedList<MatchRule>());
    }

    public List<MatchRule> removeInstance(InstanceData removedInstance) {
        List<MatchRule> matchRules = _instancesMap.get(removedInstance);
        for (MatchRule matchRule : matchRules) {
            _rulesMap.remove(matchRule);
        }
        return matchRules;
    }

    public InstanceData getInstance(MatchRule rule) {
        return _rulesMap.get(rule);
    }

    public List<MatchRule> getRules(InstanceData worker) {
        return _instancesMap.get(worker);
    }

    public void removeRule(MatchRule rule) {
        InstanceData instance = _rulesMap.get(rule);
        List<MatchRule> rules = _instancesMap.get(instance);
        rules.remove(rule);
        _rulesMap.remove(rule);
    }

    public void addRule(MatchRule rule, InstanceData instnace) {
        _instancesMap.get(instnace).add(rule);
        _rulesMap.put(rule, instnace);
    }


    public void addRules(List<MatchRule> rules, InstanceData mostFreeWorker) {
        for (MatchRule rule : rules) {
            addRule(rule, mostFreeWorker);
        }
    }
}
