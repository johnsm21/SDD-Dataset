package rules;

import java.util.ArrayList;
import java.util.List;

import data.CellProv;
import data.SDDVariable;
import data.Report.Severity;
import data.SemanticDataDictionary.SDDDatum;
import ontology.OntologyDB;
import data.SemanticDataDictionary;
import validation.SDDRule;

public class RuleSDDType implements SDDRule{
	private OntologyDB _ont;
	public RuleSDDType(OntologyDB ont) {
		_ont = ont;
	}

	@Override
	public String genErrorMessage() {
		return "SDD cell had the wrong type ";
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
			
			// Check the attribute for class iris
			for(String iri: var._att) {
				if(!_ont.isClass(iri)) {
					CellProv[] cells = sdd.getProv(var.getName(), SDDDatum.attribute);
					boolean prop = _ont.isProperty(iri);
					for(CellProv cell : cells) {
						// Don't add message if we already have this error
						if(prop) {
							cell._annotation = genErrorMessage() + "expected class IRI, found property IRI: " + iri;
						}
						else {
							cell._annotation = genErrorMessage() + "couldn't find IRI: " + iri;
						}
						
						if(!res.contains(cell)) {
							res.add(cell);
						}
					}
				}
				
				
			}
			
		}
		
		
		
//		for(int i=0; i < vars.size(); i++) {
//			String iName = vars.get(i).getName();
//			for(int j=i+1; j < vars.size(); j++) {
//				String jName = vars.get(j).getName();
//				
//				// We found matching names
//				if(iName.equals(jName)) {
//					// Generate prov & add to list
//					CellProv[] cells = sdd.getProv(iName, SDDDatum.name);
//					for(CellProv cell : cells) {
//						// Don't add message if we already have this error
//						cell._annotation = genErrorMessage();
//						if(!res.contains(cell)) {
//							res.add(cell);
//						}
//					}
//				}	
//			}
//		}
		return res.toArray(new CellProv[0]);
	}

}
