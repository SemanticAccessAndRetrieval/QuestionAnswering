/*
 *  This code belongs to the Semantic Access and Retrieval (SAR) group of the
 *  Information Systems Laboratory (ISL) of the
 *  Institute of Computer Science (ICS) of the
 *  Foundation for Research and Technology - Hellas (FORTH)
 *  Nobody is allowed to use, copy, distribute, or modify this work.
 *  It is published for reasons of research results reproducibility.
 *  (c) 2017 Semantic Access and Retrieval group, All rights reserved
 */
package gr.forth.ics.isl.demoExternal.LODsyndesis;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import static org.apache.http.HttpHeaders.ACCEPT;
import static org.apache.http.HttpHeaders.CONTENT_TYPE;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;

/**
 *
 * @author Sgo
 *
 * This Class is used to send requests and receive requests form LODsyndesis
 * rest API. It supports Object Coreference, Fact Validation, All Facts for An
 * Entity and LODsyndesis keyword search services.
 */
public class LODSyndesisChanel {

    private final HttpClient client;
    private HttpGet objectCoreference;
    private HttpGet allFacts;
    private HttpGet factChecking;
    private static final String URL = "http://83.212.101.8:8080/LODsyndesis/rest-api";
    private String serviceName;

    /**
     * Used to open connection with client and LODsyndesis
     */
    public LODSyndesisChanel() {
        client = HttpClientBuilder.create().build();
    }

    /**
     * Used to search for a given entity and its same as entities.
     *
     * @param uri
     * @return Triples of sameAs entities
     */
    public ArrayList<ArrayList<String>> getEntity(String uri) {
        try {
            serviceName = "objectCoreference";
            objectCoreference = new HttpGet(URL + "/" + serviceName + "?uri=" + uri);
            objectCoreference.addHeader(ACCEPT, "text/plain");
            objectCoreference.addHeader(CONTENT_TYPE, "application/n-triples");

            ArrayList<ArrayList<String>> allTriples = getContent(objectCoreference);

            return allTriples;
        } catch (IOException ex) {
            Logger.getLogger(LODSyndesisChanel.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    /**
     * Used to receive all facts (triples) about a given entity.
     *
     * @param uri
     * @return quadruples of triples (facts) and their provenance (KB from which
     * they derived from)
     */
    public ArrayList<ArrayList<String>> getAllFacts(String uri) {
        try {
            serviceName = "allFacts";
            allFacts = new HttpGet(URL + "/" + serviceName + "?uri=" + uri);
            allFacts.addHeader(ACCEPT, "application/n-quads");
            allFacts.addHeader(CONTENT_TYPE, "application/n-quads");

            ArrayList<ArrayList<String>> allQuads = getContent(allFacts);

            return allQuads;
        } catch (IOException ex) {
            Logger.getLogger(LODSyndesisChanel.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    /**
     * Used to check for a given fact, i.e triples match a given
     * subject-predicate tuple.
     *
     * @param uri
     * @param fact
     * @return quadruples of triples (facts) and their provenance (KB from which
     * they derived from)
     */
    public ArrayList<ArrayList<String>> checkFact(String uri, String fact) {
        try {
            serviceName = "factChecking";
            String URLEncodedFact = getURLEncodedFact(fact);
            factChecking = new HttpGet(URL + "/" + serviceName + "?uri=" + uri + "&fact=" + URLEncodedFact);
            factChecking.addHeader(ACCEPT, "application/n-quads");
            factChecking.addHeader(CONTENT_TYPE, "application/n-quads");

            ArrayList<ArrayList<String>> allQuads = getContent(factChecking);

            return allQuads;
        } catch (IOException ex) {
            Logger.getLogger(LODSyndesisChanel.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    /**
     * Used to transform a fact (predicate into URL valid substring). i.e.
     * replaces white spaces with %20.
     *
     * @param fact
     * @return return fact as a valid URL substring.
     */
    private String getURLEncodedFact(String fact) {
        String URLEncodedFact = "";
        String[] factSplited = fact.split(" ");
        int cnt = 0;
        for (String subFact : factSplited) {
            cnt++;
            if (cnt == factSplited.length) {
                URLEncodedFact += subFact;
            } else {
                URLEncodedFact += subFact + "%20";
            }
        }
        return URLEncodedFact;
    }

    /**
     * Used to execute the request, receive the response and produce an
     * interpretable structure with it.
     *
     * @param request
     * @return An interpretable structure that contains current service
     * response.
     * @throws IOException
     */
    private ArrayList<ArrayList<String>> getContent(HttpGet request) throws IOException {

        HttpResponse response = client.execute(request);
        BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        ArrayList<ArrayList<String>> result = new ArrayList<>();
        String line = "";

        while ((line = rd.readLine()) != null) {
            String[] lineSplited = line.split("\\s+");
            ArrayList<String> lineSplitedClean = new ArrayList<>();
            for (String lineUnit : lineSplited) {
                if (lineUnit.equals(".")) {
                    continue;
                } else {
                    lineSplitedClean.add(lineUnit);
                }
            }
            result.add(lineSplitedClean);
        }
        return result;
    }

    public static void main(String[] args) {
        LODSyndesisChanel chanel = new LODSyndesisChanel();
        System.out.println(chanel.getEntity("http://dbpedia.org/resource/Aristotle_University_of_Thessaloniki"));
        System.out.println(chanel.getAllFacts("http://dbpedia.org/resource/Spetses"));
        System.out.println(chanel.checkFact("http://dbpedia.org/resource/Aristotle_University_of_Thessaloniki", "p31c"));
    }
}
