package rules;

import data.CellProv;
import data.DataDictionary;
import data.DataDictionary.Datum;
import data.Report.Severity;
import validation.DDRule;
import validation.SDDRule;
import data.SemanticDataDictionary;
import data.Variable;

import java.util.List;
import java.util.ArrayList;

public class RuleUniqueName implements DDRule, SDDRule{

	@Override
	public CellProv[] checkRule(SemanticDataDictionary d) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CellProv[] checkRule(DataDictionary d) {
		List<CellProv> res = new ArrayList<CellProv>();
		List<Variable> vars = d.getVariables();
		
		// Check for matching names, but we never need to 
		// check against the earlier search terms
		for(int i=0; i < vars.size(); i++) {
			String iName = vars.get(i)._name;
			for(int j=i+1; j < vars.size(); j++) {
				String jName = vars.get(j)._name;
				
				// We found matching names
				if(iName.equals(jName)) {
					// Generate prov & add to list
					CellProv[] cells = d.getProv(iName, Datum.name);
					for(CellProv cell : cells) {
						// Don't add message if we already have this error
						cell._annotation = genErrorMessage();
						if(!res.contains(cell)) {
							res.add(cell);
						}
					}
				}	
			}
		}
		return res.toArray(new CellProv[0]);
	}
	
	@Override
	public String genErrorMessage() {
		return "Variables must have unique names!";
	}

	@Override
	public Severity getSeverity() {
		return Severity.error;
	}

}
