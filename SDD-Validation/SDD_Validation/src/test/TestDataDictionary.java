package test;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import data.DataDictionary;
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
	      e.printStackTrace();
	   }
	}


	@Test
	public void dataDictionaryImportTest() {
	   System.out.println(_d);
	   fail("Not yet implemented");
	}

}
