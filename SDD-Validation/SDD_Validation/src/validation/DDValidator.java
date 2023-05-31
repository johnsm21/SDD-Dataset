package validation;

import data.CellProv;
import data.DataDictionary;
import data.Report;

public class DDValidator {
	private DDRule[] _rules;
	public DDValidator(DDRule[] rules) {
		_rules = rules;
	}
	
	public Report validateDD(DataDictionary d) {
		Report repo = new Report();
		
		for(DDRule r : _rules) {
			CellProv[] prov = r.checkRule(d);
			repo.addMessage(prov, r.getSeverity());
		}
		
		return repo;
	}
}

// DD Rules
	// Check for multiple same Var Names
	// Check that numerics have ranges
	// Check for categorical data