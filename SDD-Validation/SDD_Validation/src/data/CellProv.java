package data;

public class CellProv {
	public final String _sheetName;
	public final int _colIndex;
	public final int _rowIndex;
	public String _annotation;
	
	public CellProv(String sheetName, int colIndex, int rowIndex) {
		_sheetName = sheetName;
		_colIndex = colIndex;
		_rowIndex = rowIndex;
		_annotation = "";
	}
	
	@Override
	public boolean equals(Object c) {
		if (c == null) {
            return false;
        }

        if (c.getClass() != this.getClass()) {
            return false;
        }
        
        final CellProv co = (CellProv) c;
        
        return _sheetName.equals(co._sheetName) && (_colIndex == co._colIndex) && (_rowIndex == co._rowIndex) && (_annotation == co._annotation);
	}
	
	@Override
	public String toString(){
		return "[" + _sheetName + ", col: " + _colIndex + ", row: " + _rowIndex + ", annotation: " + _annotation + "]";
	}
}
