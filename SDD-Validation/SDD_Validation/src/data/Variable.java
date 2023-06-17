package data;

import java.util.Map;
import java.util.HashMap;

public class Variable {
	private final static String _integerTypeKey = "integer";
	private final static String _continuousTypeKey = "continuous";
	private final static String _numericalTypeKey = "numerical";
	private final static String _numericTypeKey = "numeric";
	private final static String _categoricalTypeKey = "categorical";
	private final static String _primaryTypeKey = "primary key";
	private final static String _dateTypeKey = "date";
	private final static String _enumeratedTypeKey = "enumerated"; // This is the same as categorical
	private final static String _enumeratedIntTypeKey = "enumerated integer"; // This is the same as categorical
	
	private final static String _floatTypeKey = "float";
	private final static String _charTypeKey = "character";
	private final static String _textTypeKey = "text";
	private final static String _alphanumericTypeKey = "alphanumeric";
	private final static String _stringTypeKey = "string";
	private final static String _decimalTypeKey = "decimal";
	private final static String _emptyTypeKey = "";
	
	private final static String[] _categoricalSeperators = {",", ";", ", "};
	private final static String[] _expandChars = {"/", "<", ">"}; // might need to add '-', maybe we do a glove check?
	
	public String _name;
	public String _desc;
	public VarType _type;
	public Map<String, String> _category;
	public String _unit;
	public int[] _range;
	
	/*
	 * This is the classic parser that should only be used for legacy tests
	 */
	public Variable(String name, String desc, String varType, String unit, Map<String, String> cats) throws VariableException {
		this(name, desc, varType, unit, "", "", cats);
	}
	
	public Variable(String name, String desc, String varType, String unit, String min, String max, Map<String, String> cats) throws VariableException {
		_name = name.strip(); // will never be null, we throw away null variables
		if(desc == null) {
			_desc = ""; // This will be caught by rules later
		}
		else {
			_desc = desc.strip();
		}
		
		
		// Check if we have a unit
		String cleanUnit = unit;
		if(cleanUnit != null) {
			cleanUnit.strip();
		}
		
		// Extract variable type and unit
		String type;
		if(varType == null) {
			type = "";
		}
		else {
			type = Utility.cleanString(varType);
		}
	
		switch(type) {
			case _integerTypeKey: 
				_type = VarType.integer;
				_unit = cleanUnit;
				break;
			case _continuousTypeKey: 
				_type = VarType.continuous;
				_unit = cleanUnit;
				break;
			case _numericTypeKey:
			case _numericalTypeKey: 
				_type = VarType.numerical;
				// try to extract it  from the unit
				try {
					_range = extractRange(cleanUnit);
				}
				catch(VariableException e) {	
					// try to convert min and max to ints
					try {
						_range  = new int[2];
						_range[0] = Integer.parseInt(min);
						_range[1] = Integer.parseInt(max);
					}
					catch(NumberFormatException nf) {
						// one wasn't defined so this is just a continuous variable
						_range = null;
						_type = VarType.continuous;
						_unit = cleanUnit;
					}
				}
				break;
				
			case _alphanumericTypeKey:	
			case _stringTypeKey: 
			case _textTypeKey:
			case _charTypeKey:
				_type = VarType.string;
				_unit = cleanUnit;
				break;
				
			case _decimalTypeKey: 
			case _floatTypeKey:
				_type = VarType.decimal;
				_unit = cleanUnit;
				break;
				
			case _dateTypeKey:
				_type = VarType.date;
				_unit = cleanUnit;
				break;
				
			case _enumeratedTypeKey: 
			case _enumeratedIntTypeKey:
			case _categoricalTypeKey: 
				_type = VarType.categorical;
				if(cats == null) { // Data is not in code books so try to parse from units
					try {
						_category = extractCategory(cleanUnit);
					}
					catch(Exception e) {
						// System.out.println("Warning: " + _name);
						_category = new HashMap<String, String>();
					}
				}
				else {
					_category = cats;
				}
				// System.out.println(_category);
				break;
				
			case _primaryTypeKey: 
				_type = VarType.integer;
				_unit = cleanUnit;
				// System.out.println("Warning: primary key found! ToDo: need to fully integrate");
				break;
				
			case _emptyTypeKey: 
			default:
				_type = VarType.unknown;
				_unit = cleanUnit;
				// System.out.println("Unknown VarType Found: [" + type +"]");
				// throw new VariableException("Unknown Type: " + varType);
		}		
	}
	
	
	public static Map<String, String> extractCategory(String unit) throws VariableException {
		// We need to determine what the separator were using it should be ; or ,
	    // The number of the separator should be the number of categories - 1

	    // Determine the number of categories
	    String[] subs  = unit.split("=");	
	    int numCats = subs.length - 1;

	    // Determine the separator used
	    String separator = null;
	    for(String sep :_categoricalSeperators) {
	    	// Determine the number of separators
	    	int sepCount  = unit.split(sep).length; // this is number of comma's
		    if (numCats == sepCount) {
		    	separator = sep;
		    	break; // We can end early
		    }
	    }
	    
	    // Check that separator was chosen
	    if(separator == null) {
	    	throw new VariableException("Couldn't parse category: " + unit);
	    }

	    // Perform split
	    subs = unit.trim().split(separator); // ['1=illiterate', ' 2=able to write', ...]

	    //  Get just the codes and named entities
	    Map<String, String> cats = new HashMap<String, String>();
	    for(String sub : subs) {
	    	String[] s = sub.split("="); // ['1', 'illiterate']
	    	if(s.length != 2) {
		    	throw new VariableException("Couldn't parse category: " + unit);
		    }
	    	cats.put(s[0].strip(), expandCategory(s[1].strip()));
	    }

	    return cats;

	}
	
	private static String expandCategory(String cat) {
		String result = cat;
		for(String exp : _expandChars) {
			if(cat.contains(exp)) {
				String fatCat = "";
				Boolean isFirst = true;
				for(String catPart : cat.split(exp)) {
					if(isFirst) {
						fatCat = catPart;
						isFirst = false;
					}
					else {
						fatCat = fatCat + ' ' + exp + ' ' + catPart;
					}
				}
				result = fatCat.strip();
			}
			
		}
		return result;
	}

	
	private static int[] extractRange(String unit) throws VariableException {
		if(!unit.contains("range")) {
			throw new VariableException("Couldn't parse range: " + unit);
		}
		String[] cleanRange = unit.replace("range", "").strip().split("-");
	
		if(cleanRange.length != 2) {
			throw new VariableException("Couldn't parse range: " + unit);
		}
		int[] ranges = new int[2];
		try {
			ranges[0] = Integer.parseInt(cleanRange[0].trim());
			ranges[1] = Integer.parseInt(cleanRange[1].trim());
		}
		catch(NumberFormatException e) {
			throw new VariableException("Couldn't parse range: " + unit);
		}
		  
		return ranges;
	}
	
	public String toString(){
		String base = "{" + _name + ": " + _desc + ", ";
		
		switch(_type) {
			case integer:
				base = base + "integer, ";
				if(_unit != null) {
					base = base + _unit;
				}
				break;
			case continuous:
				base = base + "continuous, ";
				if(_unit != null) {
					base = base + _unit;
				}
				break;
			case numerical:
				base = base + "numerical, range " + _range[0] + " - " + _range[1];
				break;
			case categorical:
				base = base + "categorical, " + _category;
				break;
			case string:
				base = base + "string, ";
				if(_unit != null) {
					base = base + _unit;
				}
				break;
				
			case decimal:
				base = base + "decimal, ";
				if(_unit != null) {
					base = base + _unit;
				}
				break;
				
			case date:
				base = base + "date, ";
				if(_unit != null) {
					base = base + _unit;
				}
				break;
				
			case empty:
				base = base + "empty, ";
				if(_unit != null) {
					base = base + _unit;
				}
				break;
				
			case unknown:
				base = base + "unknown, ";
				if(_unit != null) {
					base = base + _unit;
				}
				break;
		}
				
		return base + "}";
	}
	
	public enum VarType {
	    integer, continuous, numerical, categorical, string, decimal, empty, date, unknown
	}
}


