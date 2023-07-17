package rules;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import com.swabunga.spell.engine.SpellDictionaryHashMap;
import com.swabunga.spell.event.SpellCheckEvent;
import com.swabunga.spell.event.SpellCheckListener;
import com.swabunga.spell.event.SpellChecker;
import com.swabunga.spell.event.StringWordTokenizer;

import data.CellProv;
import data.DataDictionary;
import data.Variable;
import data.DataDictionary.Datum;
import data.Report.Severity;
import validation.DDRule;

public class RuleDDMustNotHaveMisspelling implements DDRule{

	private SpellChecker _checker;
	private DDSpellCheck _ddCheck;
	
	public RuleDDMustNotHaveMisspelling() throws FileNotFoundException, IOException {
		// Get the dict and load it
		InputStream inputStream = getClass().getResourceAsStream("/strict-english.txt");
		System.out.println(inputStream);
	 	File dict = File.createTempFile("load-dict", "txt");
	 	dict.deleteOnExit();
	 	
		FileUtils.copyInputStreamToFile(inputStream, dict);
		inputStream.close();
		
		// File dict = new File(getClass().getClassLoader().getResource("words.utf-8.txt").getFile());
		// File dict = new File(getClass().getClassLoader().getResource("strict-english.txt").getFile());
		_checker = new SpellChecker(new SpellDictionaryHashMap(dict));
		_ddCheck = new DDSpellCheck();
		_checker.addSpellCheckListener(_ddCheck);
	}
	
	private String getMisspelledWords(String text) {
		StringWordTokenizer texTok = new StringWordTokenizer(text);
		_checker.checkSpelling(texTok);
		String[] miss = _ddCheck.getMisspellings();
		_ddCheck.clearMisspellings();
		  
		if(miss.length == 0) {
			return null;
		}
		  
		String missOut = "";
		for(String s : miss) {
			missOut = missOut + s;
			List suggestions = _checker.getSuggestions(s, 0);
		    if (suggestions.size() > 0){
		    	missOut = missOut + "[suggestion = " + suggestions.get(0).toString() + "]";
		    }
			missOut = missOut + ", ";
		}
				
		_checker.reset();
		return missOut.substring(0, missOut.length()-2);
	}
	
	@Override
	public String genErrorMessage() {
		return "Descriptions must not have misspellings: ";
	}

	@Override
	public Severity getSeverity() {
		return Severity.warning;
	}

	@Override
	public CellProv[] checkRule(DataDictionary d) {
		List<CellProv> res = new ArrayList<CellProv>();
		List<Variable> vars = d.getVariables();
		
		for(Variable var : vars) {
			if(var._desc != null) {
				String missOut = getMisspelledWords(var._desc);
				if(missOut != null) {
					CellProv[] prov = d.getProv(var._name, Datum.description);
					for(CellProv cell : prov) {
						cell._annotation = genErrorMessage() + missOut;
						res.add(cell);
					}
				}
			}
		}
		
		return res.toArray(new CellProv[0]);
	}
	//Private nested or inner class 
	private class DDSpellCheck implements SpellCheckListener {
		
		private List<String> _bWordList;
		private DDSpellCheck() {
			_bWordList = new ArrayList<String>();
		}
		
		@Override
		public void spellingError(SpellCheckEvent event) {
			event.ignoreWord(true);
			_bWordList.add(event.getInvalidWord());
		}

		public String[] getMisspellings() {
			return _bWordList.toArray(new String[0]);
		}
		
		public void clearMisspellings() {
			_bWordList.clear();
		}
	}
}
