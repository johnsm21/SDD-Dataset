package data;

import java.util.List;
import java.util.Map;

import java.util.ArrayList;

public class SDDVariable {
	public String _name;
	public SDDVarType _type;
	
	public List<String> _att;
	public List<String> _attOf; // implicit
	public List<String> _unit; // iri or label
	public List<String> _time; // implicit
	public List<String> _entity; // iri
	public List<String> _role; // iri
	public List<String> _relat; // iri
	public List<String> _inRelat; // implicit
	public List<String> _wasDeriv; // implicit
	public List<String> _wasGener; // implicit
	
	
	public SDDVariable(String colName, String attribute, String attributeOf, String unit, 
			String time, String entity, String role, String relation, String inRelationTo, 
			String wasDerivedFrom, String wasGeneratedBy, Map<String, String> prefixMap) throws VariableException{
		
		_name = colName.strip(); // will never be null, we throw away null variables
		
		// Parse name
		if(_name.startsWith("??")) {
			_type = SDDVarType.implicit;
			_name = _name.substring(2);
		}
		else {
			_type = SDDVarType.explicit;
		}
		
		if(_name.isEmpty()) {
			throw new VariableException("variable name is empty!");
		}
		
		// Parse contents
		_att = parseCell(attribute, prefixMap);
		
		_attOf = parseCell(attributeOf, prefixMap);
		_unit = parseCell(unit, prefixMap);
		_time = parseCell(time, prefixMap);
		_entity = parseCell(entity, prefixMap);
		_role = parseCell(role, prefixMap);
		_relat = parseCell(relation, prefixMap);
		_inRelat = parseCell(inRelationTo, prefixMap);
		_wasDeriv = parseCell(wasDerivedFrom, prefixMap);
		_wasGener = parseCell(wasGeneratedBy, prefixMap);
		
	}
	
	private static List<String> parseCell(String rawCell, Map<String, String> prefixMap) throws VariableException{
		List<String> cells = new ArrayList<String>();
		if(rawCell != null) {
			for(String cell : rawCell.split(",")) {
				String cleanCell = cell.strip();
				// System.out.println(cleanCell);
				
				// Check if its a we are an iri						
				if(cleanCell.startsWith("http")) {
					cells.add(cleanCell);
				}
				else {
					// Check to see if we have a prefix we can expand
					if(cleanCell.contains(":")) {
						String[] iri = cleanCell.split(":");
						
						if(iri.length != 2) {
							throw new VariableException("bad iri " + cleanCell);
						}
						
						if(!prefixMap.containsKey(iri[0])) {
							throw new VariableException("missing ontology " + iri[0]);
						}
						
						cleanCell = prefixMap.get(iri[0]) + iri[1];
						cells.add(cleanCell);
					}
					else {
						// Check if its a variable
						if(cleanCell.startsWith("??")) {
							// cleanCell = cleanCell.substring(2);
							cells.add(cleanCell);
						}
						else {
							// We have variable, text or an empty cell
							if(!cleanCell.isEmpty()) {
								cells.add(cleanCell);
								// throw new VariableException("SDD bad data type [" + cleanCell + "]");
							}
						}
					}
				}
			}
		}
		return cells;
		
	}
	
	
	public String toString(){
		String base = _name + ": attribute=" + _att + ", attributeOf=" + _attOf + ", unit=" + _unit
				+ ", time=" + _time + ", entity=" + _entity + ", role=" + _role + ", relation=" + _relat
				+ ", inRelationTo=" + _inRelat + ", wasDerivedFrom=" + _wasDeriv+ ", wasGeneratedBy=" + _wasGener;
		
		if(_type == SDDVarType.implicit) {
			base = "??" + base;
		}
				
		return "{" + base + "}";
	}
	
	public String getName() {
		if(_type == SDDVarType.implicit) {
			return "??" + _name;
		}
		return _name;
	}
	public enum SDDVarType {
		explicit, implicit
	}
}
