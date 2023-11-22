package io;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

import data.DataDictionaryException;
import data.CellProv;
import data.DataDictionary;
import data.Data;
import data.SemanticDataDictionary;
import data.SemanticDataDictionaryException;
import data.DataDictionary.Datum;
import data.Report;
import data.SDDAnalyzer;
import data.SDDVariable;
import data.SDDVariable.SDDVarType;
import rules.RUleSDDVarAttributeOrEntity;
import rules.RuleCategoricalMustHaveCategories;
import rules.RuleDDMustHaveDescription;
import rules.RuleDDMustHaveGoodDescription;
import rules.RuleDDMustHaveVarName;
import rules.RuleDDMustNotHaveMisspelling;
import rules.RuleDDMustNotHaveUnknownVarType;
import rules.RuleSDDMutualExclusiveCols;
import rules.RuleSDDRecommendedCols;
import rules.RuleSDDType;
import rules.RuleSDDVariableOrphan;
import rules.RuleUniqueName;
import validation.DDRule;
import validation.SDDRule;
import validation.DDValidator;
import validation.SDDValidator;
import ontology.OntologyDB;

public final class PythonIO {
	
	private DDValidator _ddv;
	private SDDValidator _sddv;
	private OntologyDB _onto;
	private ValueFactory _vf;
	
	
	public PythonIO() throws FileNotFoundException, IOException {
		
		DDRule[] rules = {new RuleUniqueName(), new RuleDDMustHaveVarName(), new RuleDDMustHaveDescription(), 
				new RuleCategoricalMustHaveCategories(), new RuleDDMustNotHaveUnknownVarType(), 
				new RuleDDMustHaveGoodDescription(), new RuleDDMustNotHaveMisspelling()};
		_ddv = new DDValidator(rules);
		
		_onto = new OntologyDB("http://localhost:3030/ontologies/sparql");
		
		
		SDDRule[] sddRules = {new RuleUniqueName(), new RuleSDDType(_onto), new RuleSDDMutualExclusiveCols(),
				new RuleSDDVariableOrphan(), new RuleSDDRecommendedCols(), new RUleSDDVarAttributeOrEntity()
		
		};
		_sddv = new SDDValidator(sddRules);
		
		_vf = SimpleValueFactory.getInstance();
	}
	
	public List<String> getClassLabel(String iri) throws Exception{
		return _onto.getClassLabel(_vf.createIRI(iri));
		
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
	
	public Report validatSDD(String sddPath) {
		try {
			SemanticDataDictionary sdd = new SemanticDataDictionary(sddPath);
			return _sddv.validateSDD(sdd);
		}
		catch(IOException | SemanticDataDictionaryException e) {
			Report r = new Report();
			CellProv c = new CellProv(e.getMessage(), 0, 0);
			r.addMessage( new CellProv[]{c}, Report.Severity.error);
			return r;
		}		
	}
	
	public SDDAnalyzer getAnalyzer(String sddPath) throws IOException, SemanticDataDictionaryException {
		SemanticDataDictionary sdd = new SemanticDataDictionary(sddPath);
		return new SDDAnalyzer(sdd, _onto);
	}
	
	public String test(String ddPath) {
		return ddPath;	
	}
	
	public Report validatData(String sddPath, String ddPath, String dataPath) {
		try {
			
			SemanticDataDictionary sdd = new SemanticDataDictionary(sddPath);
			DataDictionary dd = new DataDictionary(ddPath);
			Data data = new Data(dataPath, 5);
			
			Report r = new Report();
			for(SDDVariable var : sdd._variables) {
				if(var._type == SDDVarType.explicit) {
					// Make sure the dd has the variable
					CellProv[] pov = dd.getProv(var._name, Datum.name);
					if(pov.length == 0) {
						CellProv c = new CellProv("Data Dictionary is missing the variable: " + var._name, 0, 0);
						r.addMessage( new CellProv[]{c}, Report.Severity.error);
					}
					
					// Make sure the data has the column
					if(!data.hasColumn(var._name)) {
						CellProv c = new CellProv("Data is missing the column: " + var._name, 0, 0);
						r.addMessage( new CellProv[]{c}, Report.Severity.error);
					}
				}
				
			}
			
//			System.out.println("---------------------");
//			System.out.println(sddPath + " --> " + ddPath + " --> " + dataPath);
//			System.out.println("---------------------");
			return r;
		}
		catch(IOException | DataDictionaryException | SemanticDataDictionaryException e) {
			Report r = new Report();
			CellProv c = new CellProv(e.getMessage(), 0, 0);
			r.addMessage( new CellProv[]{c}, Report.Severity.error);
			return r;
		}		
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
