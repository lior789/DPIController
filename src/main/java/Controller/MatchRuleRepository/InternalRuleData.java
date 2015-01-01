package Controller.MatchRuleRepository;

import java.util.LinkedList;
import java.util.List;

import Common.Middlebox;
import Controller.InternalMatchRule;

public class InternalRuleData {
	public InternalRuleData(InternalMatchRule rule, Middlebox mb) {
		this.rule = rule;
		middleboxes = new LinkedList<Middlebox>();
		middleboxes.add(mb);
	}

	public InternalMatchRule rule;
	public List<Middlebox> middleboxes;

}
