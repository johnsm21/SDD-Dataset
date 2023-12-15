package ontology;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.io.FileUtils;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sparql.SPARQLRepository;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.BooleanQuery;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;

public class OntologyDB {
	// private Model _model;
	// private RDFConnection _model;
	private Repository _model;
	private String _url;
	
	public OntologyDB(String url) {
		_url = url;
		_model = new SPARQLRepository(_url);
		_model.init();
	}
	
	public List<String> getClassLabels(IRI graph) throws Exception {
		List<String> arl = new ArrayList<String>();
		
		// Generate Query String
		String queryString = 
				  "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n"
				+ "PREFIX owl: <http://www.w3.org/2002/07/owl#> \n"
				+ "select distinct ?variable ?label \n"
				+ "where { \n"
				+ "	graph ?g { \n"
				+ "     { ?variable a owl:Class } \n"
				+ "      UNION \n"
				+ "     { ?variable a rdfs:Class } \n" 
				+ "		?variable	rdfs:label	?label	. \n"
				+ "	} \n"
				+ "}";
		
		queryString = queryString.replace("?g", "<" + graph.stringValue() + ">");
		// System.out.println(queryString);
		queryString = URLEncoder.encode(queryString, StandardCharsets.UTF_8.toString());
		
		OkHttpClient client = new OkHttpClient();
		Request request = new Request.Builder()
				.url(_url + "?query=" + queryString)
				.build();

		try (Response response = client.newCall(request).execute()) {
			String docString = response.body().string();
			// System.out.println(docString);
			List<Map<String, String>> parsed = parseHttpResponse(docString);
			
			// System.out.println(parsed);
			
			for(Map<String, String> parse : parsed) {
				arl.add(parse.get("variable"));
				arl.add(parse.get("label"));
			}
		}
	
		
		return arl;
	}
	
	public List<String> getClassLabel(IRI iri) throws Exception {
		List<String> arl = new ArrayList<String>();
		
		// Generate Query String
		String queryString = 
				  "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n"
				+ "PREFIX dcterms: <http://purl.org/dc/terms/> \n"
				+ "PREFIX iao: <http://purl.obolibrary.org/obo/IAO_> \n"
				+ "PREFIX skos: <http://www.w3.org/2004/02/skos/core#> \n"
				+ "select distinct ?label \n"
				+ "where { \n"
				+ "	graph ?g { \n"
				+ "		?variable	rdfs:label	?label	. \n"
				+ "	} \n"
				+ "}";
		
		queryString = queryString.replace("?variable", "<" + iri.stringValue() + ">");
		// System.out.println(queryString);
		queryString = URLEncoder.encode(queryString, StandardCharsets.UTF_8.toString());
		
		OkHttpClient client = new OkHttpClient();
		Request request = new Request.Builder()
				.url(_url + "?query=" + queryString)
				.build();

		try (Response response = client.newCall(request).execute()) {
			String docString = response.body().string();
			// System.out.println(docString);
			List<Map<String, String>> parsed = parseHttpResponse(docString);
			
			// System.out.println(parsed);
			
			for(Map<String, String> parse : parsed) {
				arl.add(parse.get("label"));
			}
		}
	
		
		return arl;
	}
	
	public List<String> getOntologyOldFashion(IRI iri) throws Exception {
		List<String> arl = new ArrayList<String>();
		
		// Generate Query String
		String queryString = 
				  "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n"
				+ "PREFIX dcterms: <http://purl.org/dc/terms/> \n"
				+ "PREFIX iao: <http://purl.obolibrary.org/obo/IAO_> \n"
				+ "PREFIX skos: <http://www.w3.org/2004/02/skos/core#> \n"
				+ "select distinct ?g \n"
				+ "where { \n"
				+ "	graph ?g { \n"
				+ "		?variable	rdfs:label	?label	. \n"
//				+ "		{ \n"
//				+ "			?variable	dcterms:description	?description . \n"
//				+ "		} \n"
//				+ "		UNION \n"
//				+ "		{ \n"
//				+ "			?variable	iao:0000115	?description . \n"
//				+ "		} \n"
//				+ "		UNION \n"
//				+ "		{ \n"
//				+ "			?variable	skos:definition	?description . \n"
//				+ "		} \n"
//				+ "		UNION \n"
//				+ "		{ \n"
//				+ "			?variable	rdfs:subClassOf ?superClass . \n"
//				+ "			?superClass	skos:definition	?description . \n"
//				+ "		} \n"
				+ "	} \n"
				+ "}";
		
		queryString = queryString.replace("?variable", "<" + iri.stringValue() + ">");
		// System.out.println(queryString);
		queryString = URLEncoder.encode(queryString, StandardCharsets.UTF_8.toString());
		
		OkHttpClient client = new OkHttpClient();
		Request request = new Request.Builder()
				.url(_url + "?query=" + queryString)
				.build();

		try (Response response = client.newCall(request).execute()) {
			String docString = response.body().string();
			// System.out.println(docString);
			List<Map<String, String>> parsed = parseHttpResponse(docString);
			
			// System.out.println(parsed);
			
			for(Map<String, String> parse : parsed) {
				arl.add(parse.get("g"));
			}
		}
	
		
		return arl;
	}
	
	private List<Map<String, String>> parseHttpResponse(String response) throws Exception {
		List<Map<String, String>> parsedResults = new ArrayList<>();
		Document d = loadXMLFromString(response);
		
		// Iterate over the results
		NodeList results = d.getElementsByTagName("result");
		for(int i = 0; i < results.getLength(); i++) {			
			// Create a new binding set
			Map<String, String> parsedResult = new HashMap<>();
			parsedResults.add(parsedResult);

			// Iterate over the results tags
			Node result = results.item(i);
			NodeList bindings = result.getChildNodes();
			for(int j = 0; j < bindings.getLength(); j++) {
				
				// Iterate over bindings
				Node binding = bindings.item(j);
				if(binding.getNodeName().equals("binding")) { //there are invisible type nodes
					String varName = binding.getAttributes().item(0).toString().split("=")[1].replace("\"", "");
					NodeList uris = binding.getChildNodes();
					for(int k = 0; k < uris.getLength(); k++) {
						Node uri = uris.item(k);
						// System.out.println(uri.getNodeName());
						if(uri.getNodeName().equals("uri") || uri.getNodeName().equals("literal")) {
							String g = uri.getTextContent();
							// System.out.println(varName + "=" +g);
							parsedResult.put(varName, g);
						}
					}
				}
			}
		}
		return parsedResults;
	}
	
	private static Document loadXMLFromString(String xml) throws Exception {
	    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	    DocumentBuilder builder = factory.newDocumentBuilder();
	    InputSource is = new InputSource(new StringReader(xml));
	    return builder.parse(is);
	}
		
	public List<String> getOntology(IRI iri) {
		String queryString = 
				  "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n"
				+ "PREFIX dcterms: <http://purl.org/dc/terms/> \n"
				+ "PREFIX iao: <http://purl.obolibrary.org/obo/IAO_> \n"
				+ "select distinct ?g \n"
				+ "where { \n"
				+ "	graph ?g { \n"
				+ "		?variable	rdfs:label	?label	. \n"
				+ "		{ \n"
				+ "			?variable	dcterms:description	?description . \n"
				+ "		} \n"
				+ "		UNION \n"
				+ "		{ \n"
				+ "			?variable	iao:0000115	?description . \n"
				+ "		} \n"
				+ "	} \n"
				+ "}";
		queryString = queryString.replace("?variable", "<" + iri.stringValue() + ">");
		System.out.println(queryString);
		
		try (RepositoryConnection conn = _model.getConnection()) {
			List<String> arl = new ArrayList<String>();
			TupleQuery tq = conn.prepareTupleQuery(queryString);
			//tq.evaluate(new SPARQLResultsJSONWriter(System.out));
		 
			
			try(TupleQueryResult result = tq.evaluate()){
				while(result.hasNext()) {
					BindingSet b = result.next();
					System.out.println(b);
					arl.add(b.getValue("g").stringValue());
				}
			}
			catch(QueryEvaluationException e) {
				System.out.println(e);
			}
			return arl;
		}
	}
	
	public boolean isClass(IRI iri) {
		String queryString = 
				"ask { \n"
				+ "graph ?g { \n"
				+ "{ ?className a* <http://www.w3.org/2002/07/owl#Class>} \n"
				+ "UNION \n"
				+ "{ ?className a* <http://semanticscience.org/resource/SIO_000074>} \n"
				+ "}"
				+ "}";
		queryString = queryString.replace("?className", "<" + iri.stringValue() + ">");
		// System.out.println(queryString);
		try (RepositoryConnection conn = _model.getConnection()) {
			BooleanQuery boolQuery = conn.prepareBooleanQuery(queryString);
			return boolQuery.evaluate();
		}
	}
	
	public boolean isProperty(IRI iri) {
		String queryString = 
				"ask { \n"
				+ "graph ?g { \n"
				+ "{ ?className a* <http://www.w3.org/2002/07/owl#DatatypeProperty>} \n"
				+ "UNION \n"
				+ "{ ?className a* <http://www.w3.org/2002/07/owl#ObjectProperty>} \n"
				+ "}"
				+ "}";
		
		queryString = queryString.replace("?className", "<" + iri.stringValue() + ">");
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
