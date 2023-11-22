package test;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Test;

import data.Data;
import data.DataDictionary;
import data.SemanticDataDictionary;

public class TestData {
	@Test
	public void noDuplicates() throws Exception {
		String ddPath = "/Users/mjohnson/Projects/SDD-Dataset/data/nhanes/2023-09-12_clean/ucpreg/DD-UCPREG_J.xlsx";
		String dataPath = "/Users/mjohnson/Projects/SDD-Dataset/data/nhanes/2023-09-12_clean/ucpreg/DA-NHANES-2017-2018-UCPREG_J.xlsx";
		String outPath = "/Users/mjohnson/Projects/SDD-Dataset/data/SDD-Dataset/2023-10-30/output/DA-NHANES-2017-2018-UCPREG_J.csv";
		System.out.println(ddPath);
		System.out.println(dataPath);
		
		DataDictionary dd = new DataDictionary(ddPath);
		Data data = new Data(dataPath, 30);
		
		
		System.out.println(data._data);
		
		data.enrichData(dd);
		
		System.out.println(data._data);
		
		data.writeToCSV(outPath);
		assertTrue(true);
	}
	
	@Test
	public void genCTA() throws Exception {
		String sddPath = "/Users/mjohnson/Projects/SDD-Dataset/data/nhanes/2023-09-12_clean/ucpreg/SDD-NHANES-UCPREG.xlsx";
		String dataPath = "/Users/mjohnson/Projects/SDD-Dataset/data/nhanes/2023-09-12_clean/ucpreg/DA-NHANES-2017-2018-UCPREG_J.xlsx";
		SemanticDataDictionary sdd = new SemanticDataDictionary(sddPath);
		Data data = new Data(dataPath, 10);
		
		String[] output = sdd.generateCTATarget(data);
		System.out.println(Arrays.asList(output));
		assertTrue(true);
	}
	
}