package validation;

import data.Report.Severity;

public interface Rule {
	public String genErrorMessage();
	public Severity getSeverity();

}
