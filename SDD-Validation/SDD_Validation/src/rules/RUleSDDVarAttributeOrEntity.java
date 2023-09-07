package rules;

import java.util.ArrayList;
import java.util.List;

import data.CellProv;
import data.SDDVariable;
import data.SemanticDataDictionary;
import data.Report.Severity;
import data.SemanticDataDictionary.SDDDatum;
import validation.SDDRule;

public class RUleSDDVarAttributeOrEntity implements SDDRule{
	
	
	@Override
	public String genErrorMessage() {
		return "An SDD variable must be defined as either an Attribute or an Entity!";
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
			
			// Check Attribute and entity
			if((var._att.size() == 0) && (var._entity.size() == 0)) {
				CellProv[] cells = sdd.getProv(var.getName(), SDDDatum.attribute);
				for(CellProv cell : cells) {
					// Don't add message if we already have this error
					cell._annotation = genErrorMessage();
					if(!res.contains(cell)) {
						res.add(cell);
					}
				}
				
				cells = sdd.getProv(var.getName(), SDDDatum.entity);
				for(CellProv cell : cells) {
					// Don't add message if we already have this error
					cell._annotation = genErrorMessage();
					if(!res.contains(cell)) {
						res.add(cell);
					}
				}
				
			}

		}
		
		return res.toArray(new CellProv[0]);
	}
}