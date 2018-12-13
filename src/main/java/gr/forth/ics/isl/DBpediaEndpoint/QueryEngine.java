/*
 *  This code belongs to the Semantic Access and Retrieval (SAR) group of the
 *  Information Systems Laboratory (ISL) of the
 *  Institute of Computer Science (ICS) of the
 *  Foundation for Research and Technology - Hellas (FORTH)
 *  Nobody is allowed to use, copy, distribute, or modify this work.
 *  It is published for reasons of research results reproducibility.
 *  (c) 2017 Semantic Access and Retrieval group, All rights reserved
 */
package gr.forth.ics.isl.DBpediaEndpoint;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.jena.query.*;
import org.apache.jena.sparql.engine.http.QueryEngineHTTP;

/**
 *
 * @author Sgo
 */
public class QueryEngine {

    /**
     * Query an Endpoint using the given SPARQl query
     *
     * @param szQuery
     * @param szEndpoint
     * @throws Exception
     */
    public ArrayList<String> queryEndpoint(String szQuery, String szEndpoint)
            throws Exception {
        // Create a Query with the given String
        Query query = QueryFactory.create(szQuery);

        // Create the Execution Factory using the given Endpoint
        QueryExecution qexec = QueryExecutionFactory.sparqlService(
                szEndpoint, query);

        // Set Timeout
        ((QueryEngineHTTP) qexec).addParam("timeout", "100000");

        ArrayList<String> answer = new ArrayList<>();
        if (szQuery.contains("ASK")) {
            boolean ans = qexec.execAsk();
            answer.add(String.valueOf(ans));
        } else if (szQuery.contains("SELECT")) {
            ResultSet rs = qexec.execSelect();
//            if (!rs.hasNext()) {
//                System.out.println("No results");
//            }

            while (rs.hasNext()) {
                // Get Result
                QuerySolution qs = rs.next();

                // Get Variable Names
                Iterator<String> itVars = qs.varNames();

                String partialAnswer = "{";
                // Display Result
                while (itVars.hasNext()) {
                    String szVar = itVars.next().toString();
                    String szVal = qs.get(szVar).toString();

                    if (itVars.hasNext()) {
                        partialAnswer += "" + szVar + "=" + szVal + ",";
                    } else {
                        partialAnswer += "" + szVar + "=" + szVal + "}";
                    }
                }
                answer.add(partialAnswer);
            }

        } else {
            answer.add("Unrecognized Query Type");
        }
        //System.out.println(answer);
        return answer;
    } // End of Method: queryEndpoint()

    public boolean isDBpediaClass(String slot_uri) {
        try {
            String query = "PREFIX dbo: <http://dbpedia.org/ontology/> PREFIX dbp: <http://dbpedia.org/property/> PREFIX dbr: <http://dbpedia.org/resource/> PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
                    + "ASK { "
                    + "{?entity rdf:type <" + slot_uri + ">.} UNION"
                    + "{?entity dbo:type <" + slot_uri + ">.} UNION"
                    + "{?entity dbp:type <" + slot_uri + ">.} UNION"
                    + "{?entity dbr:type <" + slot_uri + ">.}"
                    + "}";

            boolean isClass = Boolean.valueOf(this.queryEndpoint(query, "http://dbpedia.org/sparql").get(0));

            return isClass;
        } catch (Exception ex) {
            Logger.getLogger(QueryEngine.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    public static void main(String[] args) throws IOException {
        String slot_uri = "dbo:Scientist";

        // SPARQL Query
        String szQuery = "PREFIX dbo: <http://dbpedia.org/ontology/> PREFIX dbp: <http://dbpedia.org/property/> PREFIX res: <http://dbpedia.org/resource/> PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
                + "SELECT DISTINCT ?uri ?string WHERE { "
                + "?uri rdf:type dbo:City . "
                + "?uri dbo:isPartOf res:Aichi_Prefecture . "
                + "?uri dbp:populationTotal ?inhabitants . "
                + "FILTER (?inhabitants > 100000) . "
                + "OPTIONAL { "
                + "?uri rdfs:label ?string. "
                + "FILTER (lang(?string) = 'en') "
                + "} "
                + "}";
        // Arguments
        if (args != null && args.length == 1) {
            szQuery = new String(
                    Files.readAllBytes(Paths.get(args[0])),
                    Charset.defaultCharset());
        }

        // DBPedia Endpoint
        String szEndpoint = "http://dbpedia.org/sparql";

        // Query DBPedia
        try {
            QueryEngine q = new QueryEngine();
            System.out.println(q.isDBpediaClass(slot_uri));
        } catch (Exception ex) {
            System.err.println(ex);
        }
    }
}
