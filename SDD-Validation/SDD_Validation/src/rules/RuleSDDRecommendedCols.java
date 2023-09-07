package rules;

import java.util.ArrayList;
import java.util.List;

import data.CellProv;
import data.SDDVariable;
import data.SemanticDataDictionary;
import data.Report.Severity;
import data.SemanticDataDictionary.SDDDatum;
import validation.SDDRule;

public class RuleSDDRecommendedCols implements SDDRule{
	
	
	@Override
	public String genErrorMessage() {
		return " is recommended.";
	}

	@Override
	public Severity getSeverity() {
		return Severity.warning;
	}

	@Override
	public CellProv[] checkRule(SemanticDataDictionary sdd) {
		List<CellProv> res = new ArrayList<CellProv>();
		List<SDDVariable> vars = sdd._variables;
		
		for(SDDVariable var : vars) {
			
			// Attribute then attributeOf
			if((var._att.size() > 0) && (var._attOf.size() == 0)) {
				CellProv[] cells = sdd.getProv(var.getName(), SDDDatum.attributeOf);
				for(CellProv cell : cells) {
					// Don't add message if we already have this error
					cell._annotation = "If an SDD defines an Attribute then attributeOf" + genErrorMessage();
					if(!res.contains(cell)) {
						res.add(cell);
					}
				}
			}
			
			// Role then inRelationTo
			if((var._role.size() > 0) && (var._inRelat.size() == 0)) {
				CellProv[] cells = sdd.getProv(var.getName(), SDDDatum.role);
				for(CellProv cell : cells) {
					// Don't add message if we already have this error
					cell._annotation = "If an SDD defines a Role then inRelationTo" + genErrorMessage();
					if(!res.contains(cell)) {
						res.add(cell);
					}
				}
			}
		}
		
		return res.toArray(new CellProv[0]);
	}
}