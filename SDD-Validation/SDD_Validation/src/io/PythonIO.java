package io;

import java.io.FileNotFoundException;
import java.io.IOException;

import data.DataDictionaryException;
import data.CellProv;
import data.DataDictionary;
import data.Report;
import rules.RuleCategoricalMustHaveCategories;
import rules.RuleDDMustHaveDescription;
import rules.RuleDDMustHaveGoodDescription;
import rules.RuleDDMustHaveVarName;
import rules.RuleDDMustNotHaveMisspelling;
import rules.RuleDDMustNotHaveUnknownVarType;
import rules.RuleUniqueName;
import validation.DDRule;
import validation.DDValidator;

public final class PythonIO {
	
	private DDValidator _ddv;
	public PythonIO() throws FileNotFoundException, IOException {
		
		DDRule[] rules = {new RuleUniqueName(), new RuleDDMustHaveVarName(), new RuleDDMustHaveDescription(), 
				new RuleCategoricalMustHaveCategories(), new RuleDDMustNotHaveUnknownVarType(), 
				new RuleDDMustHaveGoodDescription(), new RuleDDMustNotHaveMisspelling()};
		_ddv = new DDValidator(rules);
	}
	
	
	public static String[] validateSDD(String ddPath, String sddPath) {
		return new String[0];
		
	}
	
	public Report validatDD(String ddPath) {
		try {
			DataDictionary d = new DataDictionary(ddPath);
			return _ddv.validateDD(d);
		}
		catch(IOException | DataDictionaryException e) {
			Report r = new Report();
			CellProv c = new CellProv(e.getMessage(), 0, 0);
			r.addMessage( new CellProv[]{c}, Report.Severity.error);
			return r;
		}		
	}
	
	public String test(String ddPath) {
		return ddPath;	
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
