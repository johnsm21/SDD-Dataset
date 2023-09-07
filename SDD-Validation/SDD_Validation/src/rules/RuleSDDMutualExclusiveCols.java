package rules;

import java.util.ArrayList;
import java.util.List;


import data.CellProv;
import data.SDDVariable;
import data.SemanticDataDictionary;
import data.Report.Severity;
import data.SemanticDataDictionary.SDDDatum;
import validation.SDDRule;

public class RuleSDDMutualExclusiveCols implements SDDRule{
	
	
	@Override
	public String genErrorMessage() {
		return " must be mutually exclusive!";
	}

	@Override
	public Severity getSeverity() {
		return Severity.error;
	}

	@Override
	public CellProv[] checkRule(SemanticDataDictionary sdd) {
		List<CellProv> res = new ArrayList<CellProv>();
		List<SDDVariable> vars = sdd._variables;
		
		for(SDDVariable var : vars) {
			
			// Attribute and Entity columns
			if((var._att.size() > 0) && (var._entity.size() > 0)) {
				CellProv[] cells = sdd.getProv(var.getName(), SDDDatum.attribute);
				for(CellProv cell : cells) {
					// Don't add message if we already have this error
					cell._annotation = "Attribute and Entity columns" + genErrorMessage();
					if(!res.contains(cell)) {
						res.add(cell);
					}
				}
				cells = sdd.getProv(var.getName(), SDDDatum.entity);
				for(CellProv cell : cells) {
					// Don't add message if we already have this error
					cell._annotation = "Attribute and Entity columns" + genErrorMessage();
					if(!res.contains(cell)) {
						res.add(cell);
					}
				}
			}
			
			// Role and Relation columns
			if((var._role.size() > 0) && (var._relat.size() > 0)) {
				CellProv[] cells = sdd.getProv(var.getName(), SDDDatum.role);
				for(CellProv cell : cells) {
					// Don't add message if we already have this error
					cell._annotation = "Role and Relation columns" + genErrorMessage();
					if(!res.contains(cell)) {
						res.add(cell);
					}
				}
				cells = sdd.getProv(var.getName(), SDDDatum.relation);
				for(CellProv cell : cells) {
					// Don't add message if we already have this error
					cell._annotation = "Role and Relation columns" + genErrorMessage();
					if(!res.contains(cell)) {
						res.add(cell);
					}
				}
			}
		}
		
		return res.toArray(new CellProv[0]);
	}
}