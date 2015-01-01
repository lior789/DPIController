package Controller.MatchRuleRepository;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import Common.Middlebox;
import Common.Protocol.MatchRule;
import Controller.InternalMatchRule;

/**
 * This class is in charge on keeping track on all the match rules registered by
 * the middleboxes in addition this repository should provide the api needed to
 * load balance the rules among the dpi-services Created by Lior on 17/11/2014.
 */
public class MatchRulesRepository {
	private static int _rulesCount = 0;
	private static final Logger LOGGER = Logger
			.getLogger(MatchRulesRepository.class);
	private final HashMap<Middlebox, Set<MatchRule>> _rulesDictionary;
	private final Map<MatchRulePattern, InternalRuleData> _globalRules;

	public MatchRulesRepository() {
		_rulesDictionary = new HashMap<Middlebox, Set<MatchRule>>();
		_globalRules = new HashMap<>();
	}

	public boolean addMiddlebox(Middlebox mb) {
		if (_rulesDictionary.containsKey(mb)) {
			return false;
		}
		_rulesDictionary.put(mb, new HashSet<MatchRule>());
		return true;
	}

	/**
	 * @param middleboxId
	 * @return list of InternalMatchRules to be removed (no other middlebox has
	 *         them), null if no such middlebox
	 */
	public List<InternalMatchRule> removeMiddlebox(Middlebox mb) {
		Set<MatchRule> rules = _rulesDictionary.get(mb);
		if (rules == null) {
			return null;
		}
		List<InternalMatchRule> result = removeRules(mb,
				new LinkedList<MatchRule>(rules));
		_rulesDictionary.remove(mb);
		return result;
	}

	/**
	 * remove MatchRules for a middlebox mb
	 * 
	 * @param mb
	 * @param rules
	 * @return list of removed internal rules (no other middlebox had them),
	 *         null if not such mb
	 */
	public List<InternalMatchRule> removeRules(Middlebox mb,
			List<MatchRule> rules) {
		Set<MatchRule> currentRules = _rulesDictionary.get(mb);
		if (currentRules == null) {
			LOGGER.warn("unknown middlebox id: " + mb.id);
			return null;
		}
		List<InternalMatchRule> rulesToRemove = new LinkedList<InternalMatchRule>();
		for (MatchRule rule : rules) {
			if (!currentRules.remove(rule)) {
				LOGGER.warn(String.format("No such rule %s in middlebox %s",
						rule.rid, mb.id));
			} else {
				InternalMatchRule tmp = removeFromRuleSet(mb, rule);
				if (tmp != null) {
					rulesToRemove.add(tmp);
				}
			}

		}
		return rulesToRemove;
	}

	private InternalMatchRule removeFromRuleSet(Middlebox mb, MatchRule rule) {
		InternalMatchRule tmp = null;
		MatchRulePattern pattern = new MatchRulePattern(rule);
		InternalRuleData matchRuleData = _globalRules.get(pattern);
		matchRuleData.middleboxes.remove(mb);
		if (matchRuleData.middleboxes.size() == 0) {
			_globalRules.remove(pattern);
			tmp = matchRuleData.rule;
		}
		return tmp;
	}

	/**
	 * this methods add rules to the repository corresponding to input middlebox
	 * existing rule-ids are overwritten the repository
	 * 
	 * @param middleboxId
	 *            the middlebox id that we want to add rules to
	 * @param rules
	 *            list of MatchRules we want to add
	 * @return list of internal rules with internal ids duplicate rules are
	 *         merged
	 */
	public List<InternalMatchRule> addRules(Middlebox mb, List<MatchRule> rules) {
		Set<MatchRule> currentRules = _rulesDictionary.get(mb);
		if (currentRules == null) {
			LOGGER.warn("unknown middlebox id: " + mb.id);
			return null;
		}
		currentRules.addAll(rules);
		Set<InternalMatchRule> result = new HashSet<InternalMatchRule>();
		for (MatchRule matchRule : currentRules) {
			InternalMatchRule tmp = addToRulesSet(mb, matchRule);
			result.add(tmp);
		}
		return new LinkedList<InternalMatchRule>(result);
	}

	private InternalMatchRule addToRulesSet(Middlebox mb, MatchRule matchRule) {
		MatchRulePattern rulePattern = new MatchRulePattern(matchRule);
		if (_globalRules.containsKey(rulePattern)) {
			InternalRuleData matchRuleData = _globalRules.get(rulePattern);
			matchRuleData.middleboxes.add(mb);
			return matchRuleData.rule;
		} else {
			String id = generateRuleId();
			InternalMatchRule newRule = new InternalMatchRule(matchRule, id);
			InternalRuleData ruleData = new InternalRuleData(newRule, mb);
			_globalRules.put(rulePattern, ruleData);
			return newRule;
		}
	}

	private String generateRuleId() {
		_rulesCount++;
		return "g" + _rulesCount;
	}

	public Collection<Middlebox> getAllMiddleboxes() {
		return this._rulesDictionary.keySet();
	}

	public Set<MatchRulePattern> getPatterns() {
		return _globalRules.keySet();
	}

}
