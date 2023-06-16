package data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import data.DataDictionary.Datum;

public class CodeBook {

	private final static String _nameKey = "varname";
	private final static String _codeKey = "codes";
	private final static String _defKey = "definition";
	private final static String _scaleKey = "scale:";

	private String _codeBookSheet;
	private int _nameCol = -1;
	private int _codeCol = -1;
	private int _defCol = -1;
	private int _scaleCol = -1;
	
	public  Map<String, Map<String, String>> _codebook;
	public  Map<String, Integer> _varToRow; // Gives the first row the variable shows up in the codebook
	
	public CodeBook(Sheet cbSheet) throws DataDictionaryException, VariableException {
		_codeBookSheet = cbSheet.getSheetName();
		// Get the column indices for each field we need
		Iterator<Row> rowIt = cbSheet.iterator();
		if(!rowIt.hasNext()) {
			throw new DataDictionaryException("CodeBook sheet " +  _codeBookSheet + " is empty!");
		}
		
		// Get Headers
		Row headerRow = rowIt.next();
		int i = 0;
		int keyCount = 0;
		for (Cell cell : headerRow) {
			String header = Utility.cleanString(cell.getStringCellValue());
			if(header.contains(_scaleKey)) { // we do this because its long and people modify it
				_scaleCol = i;
				keyCount++;
			}
			else {
				switch(header) {
				case _nameKey:
					_nameCol = i;
					keyCount++;
					break;
				case _codeKey:
					_codeCol = i;
					keyCount++;
					break;
				case _defKey:
					_defCol = i;
					keyCount++;
					break;
				default:
				}
			}
			i++;
		}
		
		// Make sure the headers are good
		if( keyCount > 4) {
			throw new DataDictionaryException("Found too many codebook columns: nameCol = " + _nameCol + ", codeCol = " 
					+ _codeCol + ", defCol = " + _defCol + ", scaleCol = " + _scaleCol);
		}
		
		if( (_nameCol < 0) || (_codeCol < 0) || (_defCol < 0) || (_scaleCol < 0)) {
			throw new DataDictionaryException("Couldn't find codebook columns: nameCol = " + _nameCol + ", codeCol = " 
					+ _codeCol + ", defCol = " + _defCol + ", scaleCol = " + _scaleCol);
		}
		
		System.out.println("Found codebook columns: nameCol = " + _nameCol + ", codeCol = " 
				+ _codeCol + ", defCol = " + _defCol + ", scaleCol = " + _scaleCol);
		
		// Extract Categorical codes and definitions
		/*
		 * There are 3 types of codebooks:
		 * 
		 * 	Type 1:
		 * 		VARNAME		CODES				DEFINITION			SCALE
		 * 		gender		1					Male				2
		 * 					2					Female			
		 * 		...
		 * 
		 * Type 2:
		 * 		VARNAME		CODES				DEFINITION			SCALE
		 * 		gender		1,2					1=Male, 2=Female	2
		 * 		...
		 * 
		 * Type 3:
		 * 		VARNAME		CODES				DEFINITION			SCALE
		 * 		gender		1=Male, 2=Female	Child's Gender		2
		 * 		...
		 */
		
		// Look for categorical rows
		_codebook = new HashMap<String, Map<String, String>>();
		int isType1 = 0;
		int isType2 = 0;
		int isType3 = 0;
		Boolean skipIt = false;
		Row currentRow = null;
		_varToRow = new HashMap<String, Integer>();
		while(rowIt.hasNext()) {
			// Lets us skip updates when we need to because of type 1 parsing
			if(skipIt) {
				skipIt  = false;
			}
			else {
				currentRow = rowIt.next();
			}
			
			// Retrieve cell values
			String scaleText = Utility.getCellAsString(currentRow.getCell(_scaleCol));
			String var = Utility.getCellAsString(currentRow.getCell(_nameCol));
			
			// Add the prov info
			_varToRow.put(var, currentRow.getRowNum());
			
			// Found a categorical row
			if(isCategorical(scaleText)) {				
				// Extract other cells
				String code = Utility.getCellAsString(currentRow.getCell(_codeCol));
				String def = Utility.getCellAsString(currentRow.getCell(_defCol));
				
				// System.out.println(var + ", " + code + ", " + def);
				
				Map<String, String> result = extractType2(code, def);
				if(result == null) { // not type 2 keep looking
					
					result = extractType3(code);
					if(result == null) { // not type 3 keep looking
						
						// Check if current row is type 1 compatible
						if( var.equals("") || code.equals("") || var.equals("")) {
							throw new VariableException("Unknown codebook format!");
						}
						
						// Lets try to extract a type 1 
						result = new HashMap<String, String>();
						result.put(code, def); // add the first line
						while(rowIt.hasNext()) {
							currentRow = rowIt.next();
							String varSub = Utility.getCellAsString(currentRow.getCell(_nameCol));
							String codeSub = Utility.getCellAsString(currentRow.getCell(_codeCol));
							String defSub = Utility.getCellAsString(currentRow.getCell(_defCol));
							if( (varSub.equals("") || (varSub.equals(var)) ) && !codeSub.equals("") && !defSub.equals("")) {
								result.put(codeSub, defSub);
							}
							else { // we didn't find a valid row so treat it like a new category
								skipIt  = true; // after we continue were not going to need to get a new row and there is no way to rewind
								break; 
							}
						}
						
						isType1 = 1; // Assume its type 1 and throw an error if were wrong
						// System.out.println("Assume type 1");
						
					}
					else { // is type 3 track 
						isType3 = 1;
						// System.out.println("Type 3");
					}
				}
				else { // is type 2 track 
					isType2 = 1;
					// System.out.println("Type 2");
				}
				
				// We have a result!
				_codebook.put(var, result);
			}
		}
		
		if((isType1 + isType2 + isType3) > 1) {
			throw new DataDictionaryException("CodeBook sheet " +  _codeBookSheet + " had multiple parsing types!");
		}
		
		// System.out.println(_codebook);
		
	}
	
	/**
	 * Returns null if the variables is not in the codebook or its the wrong data type being asked for
	 * @param varName
	 * @param d
	 * @return
	 */
	public CellProv[] getProv(String varName, Datum d) {
		if(d != Datum.codebook) {
			return null;
		}
		
		// Check to see if variable is in the codebook
		Integer row = _varToRow.get(varName);
		if(row == null) {
			return null;
		}

		return new CellProv[] {new CellProv(_codeBookSheet, _nameCol, row)};
		
	}
	
	// Returning null means its not type2
	private static Map<String, String> extractType2(String code, String def) throws VariableException {
		if(code.contains("=") || !def.contains("=")) {
			return null;
		}
		
		return Variable.extractCategory(Utility.cleanString(def));
	}
	
	// Returning null means its not type3
	private static Map<String, String> extractType3(String code) throws VariableException {
		if(!code.contains("=")) {
			return null;
		}
		
		return Variable.extractCategory(Utility.cleanString(code));
	}
	
	
	private static Boolean isCategorical(String s) {
		if(s == null) {
			return false;
		}

		switch(Utility.cleanString(s)) {
			case "2":
			case "2.0":
			case "categorical":
					return true;
			default:
				return false;
		}
	}
}
