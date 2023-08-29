package validation;

import data.CellProv;
import data.Report;
import data.SemanticDataDictionary;

public class SDDValidator {
	private SDDRule[] _rules;
	public SDDValidator(SDDRule[] rules) {
		_rules = rules;
	}
	
	public Report validateSDD(SemanticDataDictionary d) {
		Report repo = new Report();
		
		for(SDDRule r : _rules) {
			CellProv[] prov = r.checkRule(d);
			if(prov.length > 0) {
				repo.addMessage(prov, r.getSeverity());
			}
		}
		
		return repo;
	}
}