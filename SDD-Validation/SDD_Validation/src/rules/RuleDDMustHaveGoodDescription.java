package rules;

import java.util.ArrayList;
import java.util.List;

import data.CellProv;
import data.DataDictionary;
import data.Variable;
import data.DataDictionary.Datum;
import data.Report.Severity;
import validation.DDRule;
import metric.ShannonEntropy;

public class RuleDDMustHaveGoodDescription implements DDRule{
	private final double _entropy_min = 3.5;
	private final int _word_min = 3;
	
	@Override
	public String genErrorMessage() {
		return "Variables should have a good description: ";
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
			ShannonEntropy e = new ShannonEntropy(var._desc);
			int wordCount = var._desc.trim().split(" ").length;
			
			// System.out.println(var._desc + ": " +e);
			
			if( (e.entropy < _entropy_min) || (wordCount < _word_min)) {
				CellProv[] prov = d.getProv(var._name, Datum.description);
				for(CellProv cell : prov) {
					cell._annotation = genErrorMessage();
					
					if(e.entropy < _entropy_min) {
						cell._annotation = cell._annotation + "entropy too small (" + e.entropy + "), ";
					}
					
					if(wordCount < _word_min) {
						cell._annotation = cell._annotation + "word count too small (" + wordCount + ")";
					}
					res.add(cell);
				}
			}
		}
		
		return res.toArray(new CellProv[0]);
	}
}