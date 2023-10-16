package data;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

import ontology.OntologyDB;

public class SDDAnalyzer {
	private SemanticDataDictionary _sdd;
	private OntologyDB _db;
	private Set<String> _ontoSet;
	private ValueFactory _vf;
	
	public SDDAnalyzer(SemanticDataDictionary sdd, OntologyDB db ) {
		_sdd = sdd;
		_db = db;
		_vf = SimpleValueFactory.getInstance();
		
		_ontoSet = new HashSet<String>();
		for(SDDVariable var :_sdd._variables) {
			for(String iri : var._att) {
				_ontoSet.add(iri);
			}
			
			for(String iri : var._unit) {
				_ontoSet.add(iri);
			}
			
			for(String iri : var._entity) {
				_ontoSet.add(iri);
			}
			
			for(String iri : var._role) {
				_ontoSet.add(iri);
			}
			
			for(String iri : var._relat) {
				_ontoSet.add(iri);
			}
		}
	}
	
	public int getAttributeCount() {
		int count = 0;
		for(SDDVariable var :_sdd._variables) {
			if(var._att.size() > 0) {
				count++;
			}
		}
		return count;
	}
	
	public String[] getOnts() throws Exception {
		Set<String> result = new HashSet<String>();
		
		for(String iri : _ontoSet) {
			// List<String> onts = _db.getOntology(_vf.createIRI(iri));
			List<String> onts = _db.getOntologyOldFashion(_vf.createIRI(iri));
			
			
			if(onts.isEmpty()) {
				throw new SemanticDataDictionaryException("No ontology found for " + iri);
			}
			else {
				boolean haveMatch = false;
				for(String ont: onts) {
					if(result.contains(ont)) {
						haveMatch = true;
						break;
					}
				}
				
				if(!haveMatch) {
					result.add(onts.get(0));
				}
			}
		}
		
		return result.toArray(new String[0]);
	}

}
