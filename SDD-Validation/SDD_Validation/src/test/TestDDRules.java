package test;

import static org.junit.Assert.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;

import org.junit.Test;

import data.CellProv;
import data.DataDictionary;
import data.DataDictionaryException;
import rules.RuleUniqueName;
import rules.RuleDDMustHaveVarName;
import rules.RuleDDMustNotHaveMisspelling;
import rules.RuleDDMustNotHaveUnknownVarType;
import validation.DDRule;
import rules.RuleDDMustHaveDescription;
import rules.RuleDDMustHaveGoodDescription;
import rules.RuleCategoricalMustHaveCategories;


public class TestDDRules {

	@Test
	public void noDuplicates() {
		DDRule _rule = new RuleUniqueName();
		String path = "/Users/mjohnson/Projects/SDD-Dataset/data/HHEAR-Studies/2023-05-28/2016-34/34_EPI_DDCB.xlsx";
		try {
			DataDictionary d = new DataDictionary(path);
			CellProv[] msg = _rule.checkRule(d);
			System.out.println(Arrays.toString(msg));
			assertTrue(msg.length == 0);
			
		} catch (IOException | DataDictionaryException e) {
			System.out.println(e);
			fail();
		}
	}
	
	@Test
	public void threeDuplicates() {
		DDRule _rule = new RuleUniqueName();
		String path = "testData/dd_multipleVars.xlsx";
		try {
			DataDictionary d = new DataDictionary(path);
			CellProv[] msg = _rule.checkRule(d);
			System.out.println(Arrays.toString(msg));
			assertTrue(msg.length == 3);
			
		} catch (IOException | DataDictionaryException e) {
			System.out.println(e);
			fail();
		}
	}
	
	@Test
	public void VarNameBase() {
		DDRule _rule = new RuleDDMustHaveVarName();
		String path = "/Users/mjohnson/Projects/SDD-Dataset/data/HHEAR-Studies/2023-05-28/2016-34/34_EPI_DDCB.xlsx";
		try {
			DataDictionary d = new DataDictionary(path);
			CellProv[] msg = _rule.checkRule(d);
			System.out.println(Arrays.toString(msg));
			assertTrue(msg.length == 0);
			
		} catch (IOException | DataDictionaryException e) {
			System.out.println(e);
			fail();
		}
	}
	
	@Test
	public void VarNameMissing() {
		DDRule _rule = new RuleDDMustHaveVarName();
		String path = "testData/dd_multipleVars.xlsx";
		try {
			DataDictionary d = new DataDictionary(path);
			CellProv[] msg = _rule.checkRule(d);
			System.out.println(Arrays.toString(msg));
			assertTrue(msg.length == 0); // we skip over missing var names now 
			
		} catch (IOException | DataDictionaryException e) {
			System.out.println(e);
			fail();
		}
	}
	
	@Test
	public void DescripMustBase() {
		DDRule _rule = new RuleDDMustHaveDescription();
		String path = "/Users/mjohnson/Projects/SDD-Dataset/data/HHEAR-Studies/2023-05-28/2016-34/34_EPI_DDCB.xlsx";
		try {
			DataDictionary d = new DataDictionary(path);
			CellProv[] msg = _rule.checkRule(d);
			System.out.println(Arrays.toString(msg));
			assertTrue(msg.length == 0);
			
		} catch (IOException | DataDictionaryException e) {
			System.out.println(e);
			fail();
		}
	}
	
	@Test
	public void DescriMissing() {
		DDRule _rule = new RuleDDMustHaveDescription();
		String path = "testData/dd_multipleVars.xlsx";
		try {
			DataDictionary d = new DataDictionary(path);
			CellProv[] msg = _rule.checkRule(d);
			System.out.println(Arrays.toString(msg));
			assertTrue(msg.length == 1);
			
		} catch (IOException | DataDictionaryException e) {
			System.out.println(e);
			fail();
		}
	}
	
	@Test
	public void catBase() {
		DDRule _rule = new RuleCategoricalMustHaveCategories();
		String path = "/Users/mjohnson/Projects/SDD-Dataset/data/HHEAR-Studies/2023-05-28/2016-34/34_EPI_DDCB.xlsx";
		try {
			DataDictionary d = new DataDictionary(path);
			CellProv[] msg = _rule.checkRule(d);
			System.out.println(Arrays.toString(msg));
			assertTrue(msg.length == 0);
			
		} catch (IOException | DataDictionaryException e) {
			System.out.println(e);
			fail();
		}
	}
	
	@Test
	public void catMissing() {
		DDRule _rule = new RuleCategoricalMustHaveCategories();
		String path = "testData/dd_multipleVars.xlsx";
		try {
			DataDictionary d = new DataDictionary(path);
			CellProv[] msg = _rule.checkRule(d);
			System.out.println(Arrays.toString(msg));
			assertTrue(msg.length == 1);
			
		} catch (IOException | DataDictionaryException e) {
			System.out.println(e);
			fail();
		}
	}
	
	@Test
	public void noUnknown() {
		DDRule _rule = new RuleDDMustNotHaveUnknownVarType();
		String path = "/Users/mjohnson/Projects/SDD-Dataset/data/HHEAR-Studies/2023-05-28/2016-34/34_EPI_DDCB.xlsx";
		try {
			DataDictionary d = new DataDictionary(path);
			CellProv[] msg = _rule.checkRule(d);
			System.out.println(Arrays.toString(msg));
			assertTrue(msg.length == 0);
			
		} catch (IOException | DataDictionaryException e) {
			System.out.println(e);
			fail();
		}
	}
	
	@Test
	public void unknowns() {
		DDRule _rule = new RuleDDMustNotHaveUnknownVarType();
		String path = "testData/dd_multipleVars.xlsx";
		try {
			DataDictionary d = new DataDictionary(path);
			CellProv[] msg = _rule.checkRule(d);
			System.out.println(Arrays.toString(msg));
			assertTrue(msg.length == 1);
			
		} catch (IOException | DataDictionaryException e) {
			System.out.println(e);
			fail();
		}
	}
	
	
	
	@Test
	public void TestRuleDDMustHaveGoodDescription() {
		DDRule _rule = new RuleDDMustHaveGoodDescription();
		String path = "/Users/mjohnson/Projects/SDD-Dataset/data/HHEAR-Studies/2023-05-28/2016-34/34_EPI_DDCB.xlsx";
		try {
			DataDictionary d = new DataDictionary(path);
			CellProv[] msg = _rule.checkRule(d);
			System.out.println(Arrays.toString(msg));
			assertTrue(msg.length == 1);
			
		} catch (IOException | DataDictionaryException e) {
			System.out.println(e);
			fail();
		}
	}
	
	@Test
	public void TestRuleDDMustNotHaveMisspelling()  {
		
		String path = "testData/dd_multipleVars.xlsx";
		try {
			DDRule _rule = new RuleDDMustNotHaveMisspelling();
			DataDictionary d = new DataDictionary(path);
			CellProv[] msg = _rule.checkRule(d);
			System.out.println(Arrays.toString(msg));
			assertTrue(msg.length == 2);
			
		} catch (IOException | DataDictionaryException e) {
			System.out.println(e);
			fail();
		}
	}
}
