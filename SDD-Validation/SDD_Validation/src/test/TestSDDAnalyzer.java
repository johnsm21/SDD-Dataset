package test;

import static org.junit.Assert.*;

import org.junit.Test;

import data.SDDAnalyzer;
import data.SemanticDataDictionary;
import ontology.OntologyDB;

public class TestSDDAnalyzer {

	@Test
	public void test() throws Exception {
		OntologyDB onto = new OntologyDB("http://localhost:3030/ontologies/sparql");
		SemanticDataDictionary sdd = new SemanticDataDictionary("/Users/mjohnson/Projects/SDD-Dataset/data/HHEAR-Studies/2023-06-16_clean/2016-1407/SDD-2016-1407-v1.xlsx");
		SDDAnalyzer sa = new SDDAnalyzer(sdd, onto );
		
		System.out.println("getAttributeCount = " + sa.getAttributeCount());
		
		System.out.println(sa.getOnts());
	}

}
