package test;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Test;

import data.DataDictionary;
import data.DataDictionaryException;

public class TestCodeBook {

	@Test
	public void test1407() throws IOException, DataDictionaryException {
		String path = "/Users/mjohnson/Projects/SDD-Dataset/data/HHEAR-Studies/2023-05-28/2016-1407/1407_Epi_DDCB.xlsx";
		DataDictionary d = new DataDictionary(path);
	}

}
