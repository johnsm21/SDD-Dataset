package test;

import static org.junit.Assert.*;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.junit.Test;
import io.PythonIO;
import data.Report;

public class TestPythonIO {

	@Test
	public void validDD() throws FileNotFoundException, IOException {
		String path = "/Users/mjohnson/Projects/SDD-Dataset/data/HHEAR-Studies/2023-05-28/2016-34/34_EPI_DDCB.xlsx";
		//fail("Not yet implemented");
		
		PythonIO pio = new PythonIO();
		Report r = pio.validatDD(path);
		assertTrue(r.isValid());
	}
	
	@Test
	public void invalidDD() throws FileNotFoundException, IOException {
		String path = "testData/dd_multipleVars.xlsx";
		
		PythonIO pio = new PythonIO();
		Report r = pio.validatDD(path);
		assertTrue(!r.isValid());
	}
}
