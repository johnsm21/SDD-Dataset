package test;

import static org.junit.Assert.*;

import java.util.List;

import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.junit.Before;
import org.junit.Test;

import ontology.OntologyDB;

public class TestOntologyDB {
	private OntologyDB _onto;
	private ValueFactory _vf;
	
	@Before
	public void setUp() throws Exception {
		_onto = new OntologyDB("http://localhost:3030/ontologies/sparql");
		_vf = SimpleValueFactory.getInstance();
	}

	@Test
	public void TestLabel() throws Exception {
		List<String> labels = _onto.getClassLabel(_vf.createIRI("http://semanticscience.org/resource/SIO_000115"));
		System.out.println(labels);
		
		assertTrue(labels.size() == 1);
	}

}
