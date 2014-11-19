package Controller;

import Common.Protocol.MatchRule;

import java.util.*;

/**
 * This class is in charge on keeping track on all the match rules registered by the middleboxes
 * in addition this repository should provide the api needed to load balance the rules among the dpi-services
 * Created by Lior on 17/11/2014.
 */
public class MatchRulesRepository {

    private HashMap<MiddleboxData, HashMap<String, MatchRule>> _rulesDictionary;

    public MatchRulesRepository() {
        _rulesDictionary = new HashMap<MiddleboxData, HashMap<String, MatchRule>>();
    }

    public boolean addMiddlebox(String id, String name) {
        MiddleboxData middlebox = new MiddleboxData(id, name);
        if (_rulesDictionary.containsKey(middlebox)) {
            return false;
        }
        _rulesDictionary.put(middlebox, new HashMap<String, MatchRule>());
        return true;
    }

    public boolean removeMiddlebox(String middleboxId) {
        MiddleboxData middlebox = new MiddleboxData(middleboxId);
        if (!_rulesDictionary.containsKey(middlebox)) {
            return false;
        }
        _rulesDictionary.remove(middlebox);
        return true;
        //TODO: probably should update rules in the future
    }

    public boolean removeRules(String middleboxId, List<String> ruleIds) {
        HashMap<String, MatchRule> ruleHashMap = _rulesDictionary.get(new MiddleboxData(middleboxId));
        if (ruleHashMap == null) {
            System.out.println("unknown middlebox middleboxId: " + middleboxId);
            return false;
        }
        for (String ruleId : ruleIds) {
            if (ruleHashMap.remove(ruleId) == null) {
                System.out.println(String.format("No such rule %s in middlebox %s", ruleId, middleboxId));
            }
        }
        return true;
    }


    /**
     * this methods add rules to the repository corresponding to input middlebox
     * existing rule-ids are overwritten
     * @param middleboxId the middlebox id that we want to add rules to
     * @param rules list of MatchRules we want to add
     * @return false if middlebox doesn't exists, true otherwise
     */
    public boolean addRules(String middleboxId, List<MatchRule> rules) {
        HashMap<String, MatchRule> ruleHashMap = _rulesDictionary.get(new MiddleboxData(middleboxId));
        if (ruleHashMap == null) {
            System.out.println("unknown middlebox middleboxId: " + middleboxId);
            return false;
        }
        for (MatchRule rule : rules) {
            ruleHashMap.put(rule.rid,rule);
        }
        return true;
    }


    /**
     * this is a data class used to store and find the middlebox within the repository
     */
    private class MiddleboxData {
        String _id;
        String _name;

        public MiddleboxData(String id, String name) {
            this._id = id;
            this._name = name;
        }

        public MiddleboxData(String id) {
            _id = id;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            MiddleboxData that = (MiddleboxData) o;

            if (!_id.equals(that._id)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            return _id.hashCode();
        }
    }
}
