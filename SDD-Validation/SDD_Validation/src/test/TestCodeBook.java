package test;

import java.io.IOException;

import org.junit.Test;

import data.DataDictionary;
import data.DataDictionaryException;

public class TestCodeBook {

	@Test
	// Type 1 test
	public void test1407() throws IOException, DataDictionaryException {
		String path = "/Users/mjohnson/Projects/SDD-Dataset/data/HHEAR-Studies/2023-05-28/2016-1407/1407_Epi_DDCB.xlsx";
		DataDictionary d = new DataDictionary(path);
	}
	
	@Test
	// type 2 test
	public void test2121() throws IOException, DataDictionaryException {
		String path = "/Users/mjohnson/Projects/SDD-Dataset/data/HHEAR-Studies/2023-05-28/2017-2121/2121_EPI_DDCB.xlsx";
		DataDictionary d = new DataDictionary(path);
	}

	@Test
	// type 3 test
	public void test34() throws IOException, DataDictionaryException {
		String path = "/Users/mjohnson/Projects/SDD-Dataset/data/HHEAR-Studies/2023-05-28/2016-34/34_EPI_DDCB.xlsx";
		DataDictionary d = new DataDictionary(path);
	}
}
