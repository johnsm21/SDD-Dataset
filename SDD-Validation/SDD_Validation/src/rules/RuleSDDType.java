package rules;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.query.QueryEvaluationException;

import data.CellProv;
import data.SDDVariable;
import data.Report.Severity;
import data.SemanticDataDictionary.SDDDatum;
import ontology.OntologyDB;
import data.SemanticDataDictionary;
import validation.SDDRule;

public class RuleSDDType implements SDDRule{
	private OntologyDB _ont;
	private ValueFactory _vf;
	public RuleSDDType(OntologyDB ont) {
		_ont = ont;
		_vf = SimpleValueFactory.getInstance();
	}

	@Override
	public String genErrorMessage() {
		return "SDD cell had the wrong type ";
	}

	@Override
	public Severity getSeverity() {
		return Severity.warning; //error
	}

	@Override
	public CellProv[] checkRule(SemanticDataDictionary sdd) {
		List<CellProv> res = new ArrayList<CellProv>();
		List<SDDVariable> vars = sdd._variables;
		
		for(SDDVariable var : vars) {
			
			// Check the attribute for class iris
			for(String iriText: var._att) {
				checkForClass(iriText, sdd, var, res, SDDDatum.attribute);
			}
			
			// Check the attribute for variables
			for(String iriText: var._attOf) {
				checkForVars(iriText, sdd, var, res, SDDDatum.attributeOf);
			}
			
			// Check the unit for class iris
			for(String iriText: var._unit) {
				checkForClass(iriText, sdd, var, res, SDDDatum.unit);
			}
			
			// Check the time for variables
			for(String iriText: var._time) {
				checkForVars(iriText, sdd, var, res, SDDDatum.time);
			}
			
			// Check the entity for class iris
			for(String iriText: var._entity) {
				checkForClass(iriText, sdd, var, res, SDDDatum.entity);
			}
			
			// Check the role for class iris
			for(String iriText: var._role) {
				checkForClass(iriText, sdd, var, res, SDDDatum.role);
			}
			
			// Check the relation for property iris
			for(String iriText: var._relat) {
				checkForProperty(iriText, sdd, var, res, SDDDatum.relation);
			}
			
			// Check the inRelationTo for variables
			for(String iriText: var._inRelat) {
				checkForVars(iriText, sdd, var, res, SDDDatum.inRelationTo);
			}
			
			// Check the wasDerivedFrom for variables
			for(String iriText: var._wasDeriv) {
				checkForVars(iriText, sdd, var, res, SDDDatum.wasDerivedFrom);
			}
			
			// Check the wasGeneratedBy for variables
			for(String iriText: var._wasGener) {
				// looks like classes are also valid?
				// checkForVars(iriText, sdd, var, res, SDDDatum.wasGeneratedBy);
			}
		}
		
		return res.toArray(new CellProv[0]);
	}
	private void checkForVars(String varText, SemanticDataDictionary sdd, SDDVariable var, List<CellProv> res, SDDDatum type) {
		// Check if its a we are an iri						
		if(varText.startsWith("http")) {
			CellProv[] cells = sdd.getProv(var.getName(), type);
			
			for(CellProv cell : cells) {
				// Don't add message if we already have this error
				cell._annotation = genErrorMessage() + "expected variable, found IRI: " + varText;
				if(!res.contains(cell)) {
					res.add(cell);
				}
			}
		}
		
	}
	
	
	private void checkForClass(String iriText, SemanticDataDictionary sdd, SDDVariable var, List<CellProv> res, SDDDatum type) {
		try {
			// Try to make an IRI from the text string
			IRI iri = _vf.createIRI(iriText);
			
			if(!_ont.isClass(iri)) {
				CellProv[] cells = sdd.getProv(var.getName(), type);
				boolean prop = _ont.isProperty(iri);
				for(CellProv cell : cells) {
					// Don't add message if we already have this error
					if(prop) {
						cell._annotation = genErrorMessage() + "expected class IRI, found property IRI: " + iriText;
					}
					else {
						cell._annotation = genErrorMessage() + "couldn't find IRI: " + iriText;
					}
					
					if(!res.contains(cell)) {
						res.add(cell);
					}
				}
			}
		}
		catch(IllegalArgumentException | QueryEvaluationException e) {
			CellProv[] cells = sdd.getProv(var.getName(), type);
			for(CellProv cell : cells) {
				cell._annotation = "SDD cell had a bad IRI: " + iriText;
				if(!res.contains(cell)) {
					res.add(cell);
				}
			}
		}
	}
	
	private void checkForProperty(String iriText, SemanticDataDictionary sdd, SDDVariable var, List<CellProv> res, SDDDatum type) {
		try {
			// Try to make an IRI from the text string
			IRI iri = _vf.createIRI(iriText);
			
			if(!_ont.isProperty(iri)) {
				CellProv[] cells = sdd.getProv(var.getName(), type);
				boolean prop = _ont.isClass(iri);
				for(CellProv cell : cells) {
					// Don't add message if we already have this error
					if(prop) {
						cell._annotation = genErrorMessage() + "expected property IRI, found class IRI: " + iriText;
					}
					else {
						cell._annotation = genErrorMessage() + "couldn't find IRI: " + iriText;
					}
					
					if(!res.contains(cell)) {
						res.add(cell);
					}
				}
			}
		}
		catch(IllegalArgumentException | QueryEvaluationException e) {
			CellProv[] cells = sdd.getProv(var.getName(), type);
			for(CellProv cell : cells) {
				cell._annotation = "SDD cell had a bad IRI: " + iriText;
				if(!res.contains(cell)) {
					res.add(cell);
				}
			}
		}
	}

}
