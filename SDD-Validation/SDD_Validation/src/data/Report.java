package data;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

public class Report {
	public List<CellProv> _warnings;
	public List<CellProv> _errors;
	
	public Report() {
		_warnings = new ArrayList<CellProv>();
		_errors = new ArrayList<CellProv>();

	}
	
	public Report(Report r) {
		_warnings = r._warnings;
		_errors = r._errors;
	}
	
	public void addMessage(CellProv[] cp, Severity s) {
		switch(s) {
			case warning:
				_warnings.addAll((List<CellProv>) Arrays.asList(cp));
				break;
				
			case error:
				_errors.addAll((List<CellProv>) Arrays.asList(cp));
		}
		
	}
	
	public Boolean isValid() {
		return _errors.size() == 0;
	}
	
	public enum Severity {
		warning, error
	}
}
