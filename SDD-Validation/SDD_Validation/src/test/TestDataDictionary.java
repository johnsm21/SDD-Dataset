package test;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

import data.CellProv;
import data.DataDictionary;
import data.DataDictionary.Datum;
import data.DataDictionaryException;

public class TestDataDictionary {

	private DataDictionary _d;
	
	@Before
	public void setUpBeforeClass() throws Exception {
	   String path = "/Users/mjohnson/Projects/SDD-Dataset/data/HHEAR-Studies/2023-05-28/2016-34/34_EPI_DDCB.xlsx";
	   try {
	      _d = new DataDictionary(path);
	   } catch (IOException | DataDictionaryException e) {
	      System.out.println(e);
	   }
	}

	@Test
	public void getProvTestName() {
		CellProv[] results = _d.getProv("parity", Datum.name);
		CellProv[] answers = {new CellProv("DATA DICTIONARY", 0, 7)};
		// System.out.println(Arrays.toString(results));
		// System.out.println(Arrays.toString(answers));
		assertTrue(Arrays.equals(results, answers));
	}
	
	@Test
	public void getProvTestDesc() {
		CellProv[] results = _d.getProv("PID", Datum.description);
		CellProv[] answers = {new CellProv("DATA DICTIONARY", 2, 1)};
		// System.out.println(Arrays.toString(results));
		// System.out.println(Arrays.toString(answers));
		assertTrue(Arrays.equals(results, answers));
	}
	
	@Test
	public void getProvTestType() {
		CellProv[] results = _d.getProv("bmi", Datum.type);
		CellProv[] answers = {new CellProv("DATA DICTIONARY", 5, 10)};
		// System.out.println(Arrays.toString(results));
		// System.out.println(Arrays.toString(answers));
		assertTrue(Arrays.equals(results, answers));
	}
	
	@Test
	public void getProvTestUnit() {
		CellProv[] results = _d.getProv("bmicat", Datum.unit);
		CellProv[] answers = {new CellProv("DATA DICTIONARY", 7, 15)};
		// System.out.println(Arrays.toString(results));
		// System.out.println(Arrays.toString(answers));
		assertTrue(Arrays.equals(results, answers));
	}
}
