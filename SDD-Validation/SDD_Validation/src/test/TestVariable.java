package test;
import data.Variable;
import data.Variable.VarType;
import data.VariableException;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;

import org.junit.Test;

public class TestVariable {
	@Test
	public void integerTest() throws VariableException{
		Variable v = new Variable("PID", "CHEAR Participant ID number ", "Integer", "iD", null);
		assertTrue(v._name.equals("PID"));
		assertTrue(v._desc.equals("CHEAR Participant ID number"));
		assertTrue(v._type.equals(VarType.integer));
		assertTrue(v._unit.equals("iD"));
	}
	
	@Test
	public void nullTest() throws VariableException{
		Variable v = new Variable("PID", "CHEAR Participant ID number ", "Integer", null, null);
		assertTrue(v._name.equals("PID"));
		assertTrue(v._desc.equals("CHEAR Participant ID number"));
		assertTrue(v._type.equals(VarType.integer));
		assertTrue(v._unit == null);
	}
	
	@Test
	public void continousTest() throws VariableException{
		Variable v = new Variable("PID", "CHEAR Participant ID number ", "Continuous", "iD", null);
		assertTrue(v._name.equals("PID"));
		assertTrue(v._desc.equals("CHEAR Participant ID number"));
		assertTrue(v._type.equals(VarType.continuous));
		assertTrue(v._unit.equals("iD"));
	}
	
	@Test
	public void rangeTest() throws VariableException{
		Variable v = new Variable("PID", "CHEAR Participant ID number ", "numerical", "range 0-5", null);
		assertTrue(v._name.equals("PID"));
		assertTrue(v._desc.equals("CHEAR Participant ID number"));
		assertTrue(v._type.equals(VarType.numerical));
		
		int[] answer = {0, 5};
				
		assertTrue(Arrays.equals(answer, v._range));
	}
	
	@Test
	public void catagoryTest() throws VariableException{
		Variable v = new Variable("PID", "CHEAR Participant ID number ", "categorical", 
				"1=illiterate, 2=able to write, 3=primary education, 4=secondary education, "
				+ "6=college/graduate, 7=post-graduate", null);
		assertTrue(v._name.equals("PID"));
		assertTrue(v._desc.equals("CHEAR Participant ID number"));
		assertTrue(v._type.equals(VarType.categorical));
		
		Map<String, String> answer = new HashMap<String, String>();	
		answer.put("1", "illiterate");
		answer.put("2", "able to write");
		answer.put("3", "primary education");
		answer.put("4", "secondary education");
		answer.put("6", "college / graduate");
		answer.put("7", "post-graduate");
		assertTrue(answer.equals(v._category));
	}
	
	@Test
	public void toStringTest() throws VariableException{
		Variable v = new Variable("PID", "CHEAR Participant ID number ", "Integer", "iD", null);
		assertTrue(v._name.equals("PID"));
		assertTrue(v._desc.equals("CHEAR Participant ID number"));
		assertTrue(v._type.equals(VarType.integer));
		assertTrue(v._unit.equals("iD"));
		assertTrue(v.toString().equals("{PID: CHEAR Participant ID number, integer, iD}"));
	}
	
	@Test
	public void badType() throws VariableException{
		Variable v = new Variable("PID", "CHEAR Participant ID number ", "hi", "iD", null);
		assertTrue(v._type.equals(VarType.unknown));
	}
}
