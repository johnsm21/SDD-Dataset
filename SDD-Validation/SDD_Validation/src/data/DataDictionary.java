package data;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;

public class DataDictionary {
	private final static String _sheetKey = "data dictionary";
	private final static String _cbSheetKey = "codebook";
	
	private final static String _nameKey = "varname";
	private final static String _descKey = "vardesc";
	private final static String _typeKey = "type";
	private final static String _unitKey = "units";
	private final static String _minKey = "min";
	private final static String _maxKey = "max";
	
	private String _ddSheetName;
	private int _nameCol = -1;
	private int _descCol = -1;
	private int _typeCol = -1;
	private int _unitCol = -1;
	private int _minCol = -1;
	private int _maxCol = -1;
	
	private String _cbSheetName;
	
	private CodeBook _cb;
	private List<Variable> _variables;
	private Map<Integer, Integer> _ddRowTranslator;
	
	public DataDictionary(String ddPath) throws IOException, DataDictionaryException {
		// Open the Workbook
		FileInputStream file = new FileInputStream(new File(ddPath));
		Workbook workbook = new XSSFWorkbook(file);
		
		
		// Find the correct cb and dd sheets
		_ddSheetName = "";
		_cbSheetName = "";
		Iterator<Sheet> it = workbook.sheetIterator();
		while(it.hasNext()) {
			Sheet s = it.next();
			String sName = Utility.cleanString(s.getSheetName());
			if(sName.equals(_sheetKey)) {
				_ddSheetName = s.getSheetName();
			}
			if(sName.equals(_cbSheetKey)) {
				_cbSheetName = s.getSheetName();
			}
		}
		
		// Make sure we found the sheet
		if(_ddSheetName.equals("")) {
			workbook.close();
			file.close();
			throw new DataDictionaryException("Couldn't find data dictionary sheet in " + ddPath);
		}
		
		if(_cbSheetName.equals("")) {
			workbook.close();
			file.close();
			throw new DataDictionaryException("Couldn't find codebook sheet in " + ddPath);
		}
		
		// Parse Codebook
		try {
			_cb = new CodeBook(workbook.getSheet(_cbSheetName));
		} catch (VariableException e) {
			workbook.close();
			file.close();
			throw new DataDictionaryException("Couldn't parse codebook becasue " +  e.getMessage());
		}
		
		// Parse DD Sheet
		Sheet ddSheet = workbook.getSheet(_ddSheetName);
		
		// System.out.println(_ddSheetName);
		// System.out.println(ddSheet);
		
		// Get the column indices for each field we need
		Iterator<Row> rowIt = ddSheet.iterator();
		if(!rowIt.hasNext()) {
			workbook.close();
			file.close();
			throw new DataDictionaryException("Data dictionary sheet " +  _ddSheetName + " is empty in " + ddPath);
		}
		
		
		Row headerRow = rowIt.next();
		int i = 0;
		int keyCount = 0;
		for (Cell cell : headerRow) {
			switch(Utility.cleanString(cell.getStringCellValue())) {
				case _nameKey:
					_nameCol = i;
					keyCount++;
					break;
				case _descKey:
					_descCol = i;
					keyCount++;
					break;
				case _typeKey:
					_typeCol = i;
					keyCount++;
					break;
				case _unitKey:
					_unitCol = i;
					keyCount++;
					break;
					
				case _minKey:
					_minCol = i;
					keyCount++;
					break;
					
				case _maxKey:
					_maxCol = i;
					keyCount++;
					break;	
					
				default:
			}
			i++;
		}
		
		if( keyCount > 6) {
			workbook.close();
			file.close();
			throw new DataDictionaryException("Found too many data dictionary columns: nameCol = " + _nameCol + ", descCol = " 
					+ _descCol + ", typeCol = " + _typeCol + ", unitCol = " + _unitCol + ", minCol = " + _minCol 
					+ ", maxCol = " + _maxCol + " in " +ddPath);
		}
		
		if( (_nameCol < 0) || (_descCol < 0) || (_typeCol < 0) || (_unitCol < 0) || (_minCol < 0) || (_maxCol < 0)) {
			workbook.close();
			file.close();
			throw new DataDictionaryException("Couldn't find data dictionary columns: nameCol = " + _nameCol + ", descCol = " 
					+ _descCol + ", typeCol = " + _typeCol + ", unitCol = " + _unitCol + ", minCol = " + _minCol 
					+ ", maxCol = " + _maxCol + " in " +ddPath);
		}
		
		System.out.println("Found data dictionary columns: nameCol = " + _nameCol + ", descCol = " 
				+ _descCol + ", typeCol = " + _typeCol + ", unitCol = " + _unitCol + ", minCol = " + _minCol 
				+ ", maxCol = " + _maxCol + " in " +ddPath);
		
		_variables = new ArrayList<Variable>();
		_ddRowTranslator = new HashMap<Integer, Integer>();

		try {
			while(rowIt.hasNext()) {
				Row row = rowIt.next();
			
				String name = getCell(row, _nameCol);
				if((name != null) && !name.isEmpty()) {
					_variables.add(new Variable(name, getCell(row, _descCol), getCell(row, _typeCol), getCell(row, _unitCol), 
							getCell(row, _minCol), getCell(row, _maxCol), _cb._codebook.get(name.trim())));
					_ddRowTranslator.put(_variables.size()-1, row.getRowNum());
				}
			}
		}
		catch(VariableException e) {
			workbook.close();
			file.close();
			throw new DataDictionaryException(e.getMessage() + " in " +ddPath);
		}
		
		
		// System.out.println(_variables);
		
		// Close the Workbook
		workbook.close();
		file.close();
	}
	
	public CellProv[] getProv(String varName, Datum d) {
		List<CellProv> cl = new ArrayList<CellProv>();
		
		for(int i=0; i<_variables.size(); i++) {
			Variable v = _variables.get(i);
			if(v._name.equals(varName)) {
				int index = 0;
				switch(d) {
					case name:
						index = _nameCol;
						break;
					case description:
						index = _descCol;
						break;
					case type:
						index = _typeCol;
						break;
					case unit:
						index = _unitCol;
						break;
					case codebook:
						return _cb.getProv(varName, d); // Just let the code book handle it
				}
				
				cl.add(new CellProv(_ddSheetName, index, _ddRowTranslator.get(i)));
			}
		}
			
		return cl.toArray(new CellProv[0]);
	}
	
	/**
	 * This function tries to extract cell contents, if the cell is empty it caches the NullPointerException 
	 * and just passes the null.
	 * @param r
	 * @param col
	 * @return
	 */
	private static String getCell(Row r, int col) {
		/*
		try {
			// System.out.println(r.getCell(col).getStringCellValue());
			return r.getCell(col).getStringCellValue();
		}
		catch(NullPointerException e) {
			return null;
		}
		*/
		return Utility.getCellAsString(r.getCell(col));
		
	}
	
	public List<Variable> getVariables(){
		// This show probably make a deep copy
		// but for dev time and run time, I'll
		// make an assumption that Rules do not 
		// modify the variable list or variable data
		return _variables;
	}
	
	public enum Datum {
	    name, description, type, unit, codebook
	}
}
