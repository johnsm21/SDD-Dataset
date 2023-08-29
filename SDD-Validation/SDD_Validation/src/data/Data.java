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

public class Data {
	public Map<String, Map<String, List<String>>> _data;
	
	public Data(String dataPath) throws IOException {
		this(dataPath, Integer.MAX_VALUE);
	}
	
	public Data(String dataPath, int numRows) throws IOException {
		// Open the Workbook
		FileInputStream file = new FileInputStream(new File(dataPath));
		Workbook workbook = new XSSFWorkbook(file);
		
		
		// Copy all of the data for all sheets
		_data = new HashMap<String, Map<String, List<String>>>();

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
			_data.put(sheetname, new HashMap<String, List<String>>());
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
