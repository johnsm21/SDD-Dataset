package rules;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import data.CellProv;
import data.SDDVariable;
import data.SDDVariable.SDDVarType;
import data.SemanticDataDictionary;
import data.Report.Severity;
import data.SemanticDataDictionary.SDDDatum;
import validation.SDDRule;

public class RuleSDDVariableOrphan implements SDDRule{
	
	
	@Override
	public String genErrorMessage() {
		return "Referenced variables must be defined: ";
	}

	@Override
	public Severity getSeverity() {
		return Severity.error;
	}

	@Override
	public CellProv[] checkRule(SemanticDataDictionary sdd) {
		List<CellProv> res = new ArrayList<CellProv>();
		List<SDDVariable> vars = sdd._variables;
		
		// Initialize variable count
		Map<String, Integer> varUsage = new HashMap<String, Integer>();
		for(SDDVariable var : vars) {
			varUsage.put(var.getName(), 0);
		}
		
		
		// Make sure each variable usage is defined
		for(SDDVariable var : vars) {
			
			// attributeOf
			for(String varText : var._attOf) {
				checkVarUsage(varText, sdd, var, varUsage, res, SDDDatum.attributeOf);
			}
			
			// time
			for(String varText : var._time) {
				checkVarUsage(varText, sdd, var, varUsage, res, SDDDatum.time);
			}
			
			// inRelationTo
			for(String varText : var._inRelat) {
				checkVarUsage(varText, sdd, var, varUsage, res, SDDDatum.inRelationTo);
			}
			
			// wasDerivedFrom
			for(String varText : var._wasDeriv) {
				checkVarUsage(varText, sdd, var, varUsage, res, SDDDatum.wasDerivedFrom);
			}
			
			// wasGeneratedBy
			for(String varText : var._wasGener) {
				// looks like classes are also valid?
				// checkVarUsage(varText, sdd, var, varUsage, res, SDDDatum.wasGeneratedBy);
			}
		}

		// Make sure each defined implicit variable is used
		for(SDDVariable var : vars) {
			String varName = var.getName();
			int count = varUsage.get(varName);
			if(var._type == SDDVarType.implicit && count < 1) {
				CellProv[] cells = sdd.getProv(varName, SDDDatum.name);
				for(CellProv cell : cells) {
					// Don't add message if we already have this error
					cell._annotation = "Referenced virtual variable never used: " + varName;
					if(!res.contains(cell)) {
						res.add(cell);
					}
				}
			}
		}
		
		
		return res.toArray(new CellProv[0]);
	}

	private void checkVarUsage(String varText, SemanticDataDictionary sdd, SDDVariable var, Map<String, Integer> varUsage, List<CellProv> res, SDDDatum type) {
		if(varUsage.containsKey(varText)) {
			// Var is defined so increment usage
			varUsage.replace(varText, varUsage.get(varText) + 1);
		}
		else {
			// Var is not def
			CellProv[] cells = sdd.getProv(var.getName(), type);
			for(CellProv cell : cells) {
				// Don't add message if we already have this error
				cell._annotation = genErrorMessage() + varText;
				if(!res.contains(cell)) {
					res.add(cell);
				}
			}
		}
	}
	
}