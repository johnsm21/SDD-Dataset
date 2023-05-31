package rules;

import java.util.ArrayList;
import java.util.List;

import data.CellProv;
import data.DataDictionary;
import data.Variable;
import data.DataDictionary.Datum;
import data.Report.Severity;
import validation.DDRule;

public class RuleDDMustHaveDescription implements DDRule{

	@Override
	public String genErrorMessage() {
		return "Descriptions must have text!";
	}

	@Override
	public Severity getSeverity() {
		return Severity.error;
	}

	@Override
	public CellProv[] checkRule(DataDictionary d) {
		List<CellProv> res = new ArrayList<CellProv>();
		List<Variable> vars = d.getVariables();
		
		for(Variable var : vars) {
			if(var._desc == null || var._desc.isEmpty()) {
				CellProv[] prov = d.getProv(var._name, Datum.description);
				for(CellProv cell : prov) {
					cell._annotation = genErrorMessage();
					res.add(cell);
				}
			}
		}
		
		return res.toArray(new CellProv[0]);
	}
}
