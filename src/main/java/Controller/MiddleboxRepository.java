package Controller;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import Common.Middlebox;
import Common.Protocol.MatchRule;

/**
 * This class is in charge on keeping track on all the match rules registered by
 * the middleboxes in addition this repository should provide the api needed to
 * load balance the rules among the dpi-services Created by Lior on 17/11/2014.
 */
public class MiddleboxRepository {

	private static final Logger LOGGER = Logger
			.getLogger(MiddleboxRepository.class);
	private final HashMap<Middlebox, Set<MatchRule>> _rulesDictionary;

	public MiddleboxRepository() {
		_rulesDictionary = new HashMap<Middlebox, Set<MatchRule>>();
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
	 * @return list of matchRules to be removed, null if no such middlebox
	 */
	public List<MatchRule> removeMiddlebox(Middlebox mb) {
		Set<MatchRule> rules = _rulesDictionary.get(mb);
		if (rules == null) {
			return null;
		}
		_rulesDictionary.remove(mb);
		return new LinkedList<>(rules);
	}

	public boolean removeRules(Middlebox mb, List<MatchRule> rules) {
		Set<MatchRule> currentRules = _rulesDictionary.get(mb);
		if (currentRules == null) {
			LOGGER.warn("unknown middlebox id: " + mb.id);
			return false;
		}
		for (MatchRule rule : rules) {
			if (!currentRules.remove(rule)) {
				LOGGER.warn(String.format("No such rule %s in middlebox %s",
						rule.rid, mb.id));
			}
		}
		return true;
	}

	/**
	 * this methods add rules to the repository corresponding to input middlebox
	 * existing rule-ids are overwritten
	 * 
	 * @param middleboxId
	 *            the middlebox id that we want to add rules to
	 * @param rules
	 *            list of MatchRules we want to add
	 * @return false if middlebox doesn't exists, true otherwise
	 */
	public boolean addRules(Middlebox mb, List<MatchRule> rules) {
		Set<MatchRule> currentRules = _rulesDictionary.get(mb);
		if (currentRules == null) {
			LOGGER.warn("unknown middlebox id: " + mb.id);
			return false;
		}
		currentRules.addAll(rules);
		return true;
	}

	public Collection<Middlebox> getAllMiddleboxes() {
		return this._rulesDictionary.keySet();
	}

}
