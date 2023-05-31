package validation;

import data.CellProv;
import data.DataDictionary;

public interface DDRule extends Rule{
	
	public CellProv[] checkRule(DataDictionary d);

}
