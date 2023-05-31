package validation;

import data.CellProv;
import data.SemanticDataDictionary;

public interface SDDRule extends Rule{

	public CellProv[] checkRule(SemanticDataDictionary d);
}
