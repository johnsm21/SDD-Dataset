package rules;

import java.util.ArrayList;
import java.util.List;

import data.CellProv;
import data.DataDictionary;
import data.Variable;
import data.Variable.VarType;
import data.DataDictionary.Datum;
import data.Report.Severity;
import validation.DDRule;

public class RuleCategoricalMustHaveCategories implements DDRule{

	@Override
	public String genErrorMessage() {
		return "Categorical variables should have two or more defined categories!";
	}

	@Override
	public Severity getSeverity() {
		return Severity.warning;
	}

	@Override
	public CellProv[] checkRule(DataDictionary d) {
		List<CellProv> res = new ArrayList<CellProv>();
		List<Variable> vars = d.getVariables();
		
		for(Variable var : vars) {
			if(var._type == VarType.categorical) {
				int numCats = var._category.size();
				CellProv[] cbProv = d.getProv(var._name, Datum.codebook);
				
				// Wasn't in unit or codebook
				if(numCats < 2) {
					if(cbProv == null) {
						CellProv[] prov = d.getProv(var._name, Datum.unit);
						for(CellProv cell : prov) {
							cell._annotation = genErrorMessage();
							res.add(cell);
						}
					}
					else {
						for(CellProv cell : cbProv) {
							cell._annotation = genErrorMessage();
							res.add(cell);
						}
					}
				}
			}
		}
		
		return res.toArray(new CellProv[0]);
	}
}
