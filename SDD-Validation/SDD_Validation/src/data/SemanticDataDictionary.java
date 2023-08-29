package data;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import data.DataDictionary.Datum;

public class SemanticDataDictionary {
	private final static String _sheetKey = "dictionary mapping";
	private final static String _prefixKey = "prefixes";
	
	private final static String _columnKey = "column";
	private final static String _attKey = "attribute";
	private final static String _attOfKey = "attributeof";
	private final static String _unitKey = "unit";
	private final static String _timeKey = "time";
	private final static String _entityKey = "entity";
	private final static String _roleKey = "role";
	private final static String _relationKey = "relation";
	private final static String _inrelationKey = "inrelationto";
	private final static String _derivedKey = "wasderivedfrom";
	
	private final static String _prefixColKey = "prefix";
	private final static String _uriKey = "uri";

	
	private String _sddSheetName;
	private int _columnCol = -1;
	private int _attCol = -1;
	private int _attOfCol = -1;
	private int _unitCol = -1;
	private int _timeCol = -1;
	private int _entityCol = -1;
	private int _roleCol = -1;
	private int _relationCol = -1;
	private int _inrelationCol = -1;
	private int _derivedCol = -1;
	
	private String _prefixSheetName;
	private int _prefixCol = -1;
	private int _uriCol = -1;


	public List<SDDVariable> _variables;
	public Map<String, String> _prefixMap;
	private Map<Integer, Integer> _sddRowTranslator;
	
	
	public SemanticDataDictionary(String sddPath) throws IOException, SemanticDataDictionaryException {
		// Open the Workbook
		FileInputStream file = new FileInputStream(new File(sddPath));
		Workbook workbook = new XSSFWorkbook(file);
		
		
		// Find the correct sdd sheets
		_sddSheetName = "";
		_prefixSheetName = "";
		Iterator<Sheet> it = workbook.sheetIterator();
		while(it.hasNext()) {
			Sheet s = it.next();
			String sName = Utility.cleanString(s.getSheetName());
			if(sName.equals(_sheetKey)) {
				_sddSheetName = s.getSheetName();
			}
			if(sName.equals(_prefixKey)) {
				_prefixSheetName = s.getSheetName();
			}
		}
		
		// Make sure we found the sdd sheet
		if(_sddSheetName.equals("")) {
			workbook.close();
			file.close();
			throw new SemanticDataDictionaryException("Couldn't find dictionary mapping sheet in " + sddPath);
		}
		
		// Make sure we found the prefix sheet
		if(_prefixSheetName.equals("")) {
			workbook.close();
			file.close();
			throw new SemanticDataDictionaryException("Couldn't find dictionary prefix sheet in " + sddPath);
		}
		
		// Parse Prefix Sheet
		_prefixMap = new HashMap<String, String>();
		Sheet prefixSheet = workbook.getSheet(_prefixSheetName);
		
		
		// Get the column indices for each field we need
		Iterator<Row> rowIt = prefixSheet.iterator();
		if(!rowIt.hasNext()) {
			workbook.close();
			file.close();
			throw new SemanticDataDictionaryException("Prefix sheet " +  _sddSheetName + " is empty in " + sddPath);
		}
		
		
		Row headerRow = rowIt.next();
		int i = 0;
		int keyCount = 0;
		for (Cell cell : headerRow) {
			switch(Utility.cleanString(cell.getStringCellValue())) {
				case _prefixColKey:
					_prefixCol = i;
					keyCount++;
					break;
					
				case _uriKey:
					_uriCol = i;
					keyCount++;
					break;
					
				default:
			}
			i++;
		}
		
		if( keyCount > 2) {
			workbook.close();
			file.close();
			throw new SemanticDataDictionaryException("Found too many prefix columns: _prefixCol = " + _prefixCol + ", _uriCol = " 
					+ _uriCol + " in " + sddPath);
		}
		
		if( (_prefixCol < 0) || (_uriCol < 0) ) {
			workbook.close();
			file.close();
			throw new SemanticDataDictionaryException("Couldn't find sdd prefix columns: _prefixCol = " + _prefixCol + ", _uriCol = " 
					+ _uriCol + " in " + sddPath);
		}
		
		System.out.println("Found data dictionary columns: _prefixCol = " + _prefixCol + ", _uriCol = " 
				+ _uriCol + " in " + sddPath);
		
		while(rowIt.hasNext()) {
			Row row = rowIt.next();
		
			String pre = DataDictionary.getCell(row, _prefixCol);
			String expan = DataDictionary.getCell(row, _uriCol);
			if((pre != null) && !pre.isEmpty()) {
				_prefixMap.put(pre, expan);

			}
		}
		System.out.println(_prefixMap);
		
		
		
		// Parse SDD Sheet
		Sheet sddSheet = workbook.getSheet(_sddSheetName);
		
		// System.out.println(_ddSheetName);
		// System.out.println(ddSheet);
		
		// Get the column indices for each field we need
		rowIt = sddSheet.iterator();
		if(!rowIt.hasNext()) {
			workbook.close();
			file.close();
			throw new SemanticDataDictionaryException("SDD sheet " +  _sddSheetName + " is empty in " + sddPath);
		}
		
		
		headerRow = rowIt.next();
		i = 0;
		keyCount = 0;
		for (Cell cell : headerRow) {
			switch(Utility.cleanString(cell.getStringCellValue())) {
				case _columnKey:
					_columnCol = i;
					keyCount++;
					break;
					
				case _attKey:
					_attCol = i;
					keyCount++;
					break;
					
				case _attOfKey:
					_attOfCol = i;
					keyCount++;
					break;
					
				case _unitKey:
					_unitCol = i;
					keyCount++;
					break;
					
				case _timeKey:
					_timeCol = i;
					keyCount++;
					break;
					
				case _entityKey:
					_entityCol = i;
					keyCount++;
					break;
					
				case _roleKey:
					_roleCol = i;
					keyCount++;
					break;
					
				case _relationKey:
					_relationCol = i;
					keyCount++;
					break;
					
				case _inrelationKey:
					_inrelationCol = i;
					keyCount++;
					break;
					
				case _derivedKey:
					_derivedCol = i;
					keyCount++;
					break;
					
				default:
			}
			i++;
		}
		
		if( keyCount > 10) {
			workbook.close();
			file.close();
			throw new SemanticDataDictionaryException("Found too many SDD columns: columnCol = " + _columnCol 
					+ ", attCol = " + _attCol + ", _attOfCol = " + _attOfCol + ", _unitCol = " + _unitCol + ", _timeCol = " + _timeCol
					+ ", _entityCol = " + _entityCol + ", _roleCol = " + _roleCol + ", _relationCol = " + _relationCol + ", _inrelationCol = " + _inrelationCol
					+ ", _derivedCol = " + _derivedCol + " in " + sddPath);
		}
		
		if( (_columnCol < 0) || (_attCol < 0) || (_attOfCol < 0)|| (_unitCol < 0)|| (_timeCol < 0)|| (_entityCol < 0)|| (_roleCol < 0)|| (_relationCol < 0)|| (_inrelationCol < 0)|| (_derivedCol < 0)) {
			workbook.close();
			file.close();
			throw new SemanticDataDictionaryException("Couldn't find data dictionary columns: columnCol = " + _columnCol 
					+ ", attCol = " + _attCol + ", _attOfCol = " + _attOfCol + ", _unitCol = " + _unitCol + ", _timeCol = " + _timeCol
					+ ", _entityCol = " + _entityCol + ", _roleCol = " + _roleCol + ", _relationCol = " + _relationCol + ", _inrelationCol = " + _inrelationCol
					+ ", _derivedCol = " + _derivedCol + " in " + sddPath);
		}
		
		System.out.println("Found data dictionary columns: columnCol = " + _columnCol 
				+ ", attCol = " + _attCol + ", _attOfCol = " + _attOfCol + ", _unitCol = " + _unitCol + ", _timeCol = " + _timeCol
				+ ", _entityCol = " + _entityCol + ", _roleCol = " + _roleCol + ", _relationCol = " + _relationCol + ", _inrelationCol = " + _inrelationCol
				+ ", _derivedCol = " + _derivedCol + " in " + sddPath);
		
		_variables = new ArrayList<SDDVariable>();
		_sddRowTranslator = new HashMap<Integer, Integer>();

		try {
			while(rowIt.hasNext()) {
				Row row = rowIt.next();
			
				String name = DataDictionary.getCell(row, _columnCol);
				if((name != null) && !name.isEmpty()) {
					// _variables.add(new SDDVariable(name, DataDictionary.getCell(row, _attCol)));
					
					_variables.add(new SDDVariable(name, DataDictionary.getCell(row, _attCol), DataDictionary.getCell(row, _attOfCol), DataDictionary.getCell(row, _unitCol), 
							DataDictionary.getCell(row, _timeCol), DataDictionary.getCell(row, _entityCol), DataDictionary.getCell(row, _roleCol), 
							DataDictionary.getCell(row, _relationCol), DataDictionary.getCell(row, _inrelationCol), DataDictionary.getCell(row, _derivedCol), _prefixMap));
					_sddRowTranslator.put(_variables.size()-1, row.getRowNum());
				}
			}
		}
		catch(VariableException e) {
			workbook.close();
			file.close();
			throw new SemanticDataDictionaryException(e.getMessage() + " in " +sddPath);
		}
		
		
		System.out.println(_variables);
		
		// Close the Workbook
		workbook.close();
		file.close();
	}
	
	public CellProv[] getProv(String varName, SDDDatum d) {
		List<CellProv> cl = new ArrayList<CellProv>();
		
		for(int i=0; i<_variables.size(); i++) {
			SDDVariable v = _variables.get(i);
			if(v.getName().equals(varName)) {
				int index = 0;
				switch(d) {
					case name:
						index = _columnCol;
						break;
					case attribute:
						index = _attCol;
						break;
					case attributeOf:
						index = _attOfCol;
						break;
					case unit:
						index = _unitCol;
						break;
					case time:
						index = _timeCol;
						break;
					case entity:
						index = _entityCol;
						break;
					case role:
						index = _roleCol;
						break;
					case relation:
						index = _relationCol;
						break;
					case inRelationTo:
						index = _inrelationCol;
						break;
					case wasDerivedFrom:
						index = _derivedCol;
					
				}
				
				cl.add(new CellProv(_sddSheetName, index, _sddRowTranslator.get(i)));
			}
		}

		return cl.toArray(new CellProv[0]);
	}
	
	public enum SDDDatum {
	    name, attribute, attributeOf, unit, time, entity, role, relation, inRelationTo, wasDerivedFrom
	}
}
