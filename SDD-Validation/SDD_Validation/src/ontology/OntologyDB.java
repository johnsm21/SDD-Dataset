package ontology;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.FileUtils;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sparql.SPARQLRepository;
import org.eclipse.rdf4j.query.BooleanQuery;


public class OntologyDB {
	// private Model _model;
	// private RDFConnection _model;
	private Repository _model;
	
	public OntologyDB(String url) {
		_model = new SPARQLRepository(url);
		_model.init();
	}
	

	
	public boolean isClass(String iri) {
		String queryString = 
				"ask { \n"
				+ "?className a <http://www.w3.org/2002/07/owl#Class>. \n"
				+ "}";
		queryString = queryString.replace("?className", "<" + iri + ">");
		
		try (RepositoryConnection conn = _model.getConnection()) {
			BooleanQuery boolQuery = conn.prepareBooleanQuery(queryString);
			return boolQuery.evaluate();
		}
	}
	
	public boolean isProperty(String iri) {
		String queryString = 
				"ask { \n"
				+ "?className a <http://www.w3.org/2002/07/owl#DatatypeProperty>. \n"
				+ "}";
		queryString = queryString.replace("?className", "<" + iri + ">");
		try (RepositoryConnection conn = _model.getConnection()) {
			BooleanQuery boolQuery = conn.prepareBooleanQuery(queryString);
			return boolQuery.evaluate();
		}
	}
	
	@SuppressWarnings("unused")
	private static File loadFile(Class c, String filename) throws FileNotFoundException, IOException{
		// Get the dict and load it
		InputStream inputStream = c.getResourceAsStream("/" + filename);
	 	File dict = File.createTempFile("tmp-" + filename, "txt");
	 	dict.deleteOnExit();
	 	
		FileUtils.copyInputStreamToFile(inputStream, dict);
		inputStream.close();
		return dict;
	}
}
