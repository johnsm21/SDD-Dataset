package data;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import data.Variable.VarType;

public class Data {
	public LinkedHashMap<String, LinkedHashMap<String, List<String>>> _data;
	public String _dataPath;
	
	public Data(String dataPath) throws IOException {
		this(dataPath, Integer.MAX_VALUE);
	}
	
	public Data(String dataPath, int numRows) throws IOException {
		_dataPath = dataPath;
		
		// Open the Workbook
		FileInputStream file = new FileInputStream(new File(dataPath));
		Workbook workbook = new XSSFWorkbook(file);
		
		
		// Copy all of the data for all sheets
		_data = new LinkedHashMap<String, LinkedHashMap<String, List<String>>>();

		Iterator<Sheet> it = workbook.sheetIterator();
		while(it.hasNext()) {
			Sheet s = it.next();
			String sheetname = s.getSheetName();
			
			// Get the sheet data
			Sheet sheet = workbook.getSheet(sheetname);
			Iterator<Row> rowIt = sheet.iterator();
			if(!rowIt.hasNext()) {
				workbook.close();
				file.close();
				throw new IOException("Data has an empty sheet " +  sheetname + " is empty in " + dataPath);
			}
			
			// Get the column indices for each field we need
			Row headerRow = rowIt.next();
			_data.put(sheetname, new LinkedHashMap<String, List<String>>());
			Map<Integer, String> indexToHeader = new HashMap<Integer, String>();
			int i = 0;
			for (Cell cell : headerRow) {
				String value = cell.getStringCellValue().strip();
				indexToHeader.put(i, value);
				_data.get(sheetname).put(value, new ArrayList<String>());
				i++;
			}
			
			int rowCount = 0;
			while(rowIt.hasNext() && rowCount < numRows) {
				// i = 0;
				//for (Cell cell : rowIt.next()) {
				Row r = rowIt.next();
				for(i=0; i< headerRow.getPhysicalNumberOfCells(); i++) {
					Cell cell = r.getCell(i);
					
					String value = "";
					if(cell != null) {
						switch(cell.getCellType()) {
							case BLANK:
								value = "";
								break;
								
							case NUMERIC:
								double variable = cell.getNumericCellValue();							
								if ((variable == Math.floor(variable)) && !Double.isInfinite(variable)) {
								    // integer type
									value = String.valueOf((int) variable);
								}
								else {
									value = String.valueOf(variable);
								}
								// String pattern = "\\.0+"; // dot followed by any number of zeros
								// value = String.valueOf(cell.getNumericCellValue()).replaceFirst(pattern, "");
								break;
								
							case STRING:
								value = cell.getStringCellValue();
								value = value.strip();
								break;
								
							default:
								System.out.println("New cell type!");
								System.out.println(cell.getCellType());
								throw new IOException("Data has an unknown cell type " +  cell.getCellType() + " in " + dataPath);
		
						}
					}
					
					_data.get(sheetname).get(indexToHeader.get(i)).add(value);
				}
				rowCount++;
			}
			
			// System.out.println("vvvvvvvvvvvvvv");
			// System.out.println(_data);
		}
				
		// Close the Workbook
		workbook.close();
		file.close();
	}
	
	// Adds a space to cells with missing data
	// This ensures that data is inflated
	public void inflateData() throws Exception {
		for(String sheetname: _data.keySet()) {
			for(String colName: _data.get(sheetname).keySet()) {
				List<String> colData = _data.get(sheetname).get(colName);
				for(int i=0; i<colData.size(); i++) {
					if(colData.get(i).equals("")) {
						colData.set(i, " ");
					}
				}
			}
		}
	}
	
	// Converts all columns with codes to text
	public void enrichData(DataDictionary dd) throws Exception {
		for(Variable v : dd.getVariables()) {
			if(v._type == VarType.categorical) {
				System.out.println(v);
				
				// Look for the column
				boolean found = false;
				for(String sheetname: _data.keySet()) {
					List<String> colData = _data.get(sheetname).get(v._name);
					if(colData != null) {
						// Found column in sheet
						found = true;
						
						for(int i=0; i<colData.size(); i++) {
							
							// Empty Strings are represented by . in studies
							String dataToReplace = colData.get(i);
							if(dataToReplace == "") {
								dataToReplace = ".";
											
								// Check if . is valid data
								if(v._category.get(dataToReplace) == null) {
									Iterator<String> it = v._category.keySet().iterator();
									 // . is not valid get last category
									while(it.hasNext()) {
										dataToReplace = it.next();
									}
								}
							}

							String update = v._category.get(dataToReplace);
							
							if(update == null) {
								throw new Exception("Couldn't find mapping for column " + v._name 
										+ " keys = " + v._category
										+ " table value = " + dataToReplace
										+ " in the table " + _dataPath);
							}
						
							colData.set(i, update);
						}
						
						break;
					}
				}
				
				// We never found the variable
				if(!found) {
					throw new Exception("Couldn't find column " + v._name + " in the table " + _dataPath);
				}
				
			}
		}
	}
	public String getTableName() {
		String[] brokenPath = _dataPath.split("/");
		return brokenPath[brokenPath.length-1].replace(".xlsx", "");

	}
	public void writeToCSV(String outPath) throws Exception {
		if(_data.keySet().size() != 1) {
			throw new Exception("Multiple sheets " + _data.keySet() + " in the table " + _dataPath);
		}
		
		String sheetname = _data.keySet().iterator().next();
		String firstHeader = _data.get(sheetname).keySet().iterator().next();
		int size = _data.get(sheetname).get(firstHeader).size();
		
		File file = new File(outPath);
		FileWriter fw = new FileWriter(file.getAbsoluteFile());
        BufferedWriter bw = new BufferedWriter(fw);

        
        
        // Write headers
        boolean first  = true;
        String content = "";
        for(String colName: _data.get(sheetname).keySet()) {
        	
        	if(colName.contains(",")) {
        		colName = colName.replace(",", " ");
        	}
        	
        	if(first) {
        		content += colName;
        		first = false;
        	}
        	else {
        		content += "," + colName;
        	}
        }
        content += "\n";
        bw.write(content);
        
        // Write data
        for(int i=0; i<size; i++) {
        	first  = true;
            content = "";
            for(String colName: _data.get(sheetname).keySet()) {
            	String datum = _data.get(sheetname).get(colName).get(i);
            	
            	if(datum.contains(",")) {
            		datum = datum.replace(",", " ");
            	}
            	
            	if(first) {
            		content += datum;
            		first = false;
            	}
            	else {
            		content += "," + datum;
            	}
            }
            content += "\n";
            bw.write(content);
        }
        
        // Close connection
        bw.close();
		
	}
	
	public boolean hasColumn(String varName) {
		for(String sheetname: _data.keySet()) {
			for(String colName: _data.get(sheetname).keySet()) {
				if(colName.equals(varName)) {
					return true;
				}
			}
		}
		return false;
	}
}
