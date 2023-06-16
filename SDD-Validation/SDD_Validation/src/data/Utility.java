package data;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;

public class Utility {
	public static String cleanString(String s) {
		return s.toLowerCase().strip();
		
	}
	
	public static String getCellAsString(Cell c) {
		String scaleText;
		if(c == null) {
			scaleText = "";
		}
		else {
			if(CellType.NUMERIC == c.getCellType()) {
				scaleText = String.valueOf((int) c.getNumericCellValue()); // categories should always be ints
			}
			else { // String
				scaleText = c.getStringCellValue();
			}
			
		}
		return scaleText.trim();
	}
}
