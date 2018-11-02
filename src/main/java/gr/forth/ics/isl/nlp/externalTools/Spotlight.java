package gr.forth.ics.isl.nlp.externalTools;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import gr.forth.ics.isl.nlp.externalTools.models.AnnotationUnit;
import gr.forth.ics.isl.nlp.externalTools.models.CandidatesUnit;
import static gr.forth.ics.isl.nlp.externalTools.models.Constants.EMPTY;
import static gr.forth.ics.isl.nlp.externalTools.models.Prefixes.DBPEDIA_ONTOLOGY;
import static gr.forth.ics.isl.nlp.externalTools.models.Prefixes.SCHEMA_ONTOLOGY;
import gr.forth.ics.isl.nlp.externalTools.models.ResourceCandidate;
import gr.forth.ics.isl.nlp.externalTools.models.ResourceItem;
import gr.forth.ics.isl.nlp.externalTools.models.SurfaceForm;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import static org.apache.http.HttpHeaders.ACCEPT;
import static org.apache.http.HttpHeaders.CONTENT_TYPE;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;

public class Spotlight {

    private static final String URLCand = "http://api.dbpedia-spotlight.org/en/candidates";
    private static final String URLAnno = "https://api.dbpedia-spotlight.org/en/annotate";
    private final HttpClient client;
    private final HttpPost requestAnno;
    private final HttpPost requestCand;
    private final double confidence = 0.0;
    private final int support = 20;

    public Spotlight() {

        client = HttpClientBuilder.create().build();
        requestAnno = new HttpPost(URLAnno);
        requestCand = new HttpPost(URLCand);

        init();

    }

    private void init() {

        requestAnno.addHeader(ACCEPT, "application/json");
        requestAnno.addHeader(CONTENT_TYPE, "application/x-www-form-urlencoded;charset=ISO-8859-1");

        requestCand.addHeader(ACCEPT, "application/json");
        requestCand.addHeader(CONTENT_TYPE, "application/x-www-form-urlencoded;charset=ISO-8859-1");
    }

    private AnnotationUnit get() throws IOException {
        Gson gson = new Gson();

        AnnotationUnit annotationUnit = gson.fromJson(getContent(requestAnno), AnnotationUnit.class);
        fixPrefixes(annotationUnit.getResources());

        return annotationUnit;
    }

    private CandidatesUnit getCandidates() throws IOException {
        JsonParser parser = new JsonParser();
        String json = getContent(requestCand);

        CandidatesUnit candidatesUnit = new CandidatesUnit();
        JsonElement jsonTree = parser.parse(json);
        if (jsonTree.isJsonObject()) {
            JsonObject jsonObject = jsonTree.getAsJsonObject();
            JsonElement annotation = jsonObject.get("annotation");
            if (annotation.isJsonObject()) {
                String text = annotation.getAsJsonObject().get("@text").toString();
                candidatesUnit.setText(text);
                JsonElement surfaceForm = annotation.getAsJsonObject().get("surfaceForm");
                if (surfaceForm.isJsonArray()) {
                    for (JsonElement sf : surfaceForm.getAsJsonArray()) {
                        if (sf.isJsonObject()) {
                            String name = sf.getAsJsonObject().get("@name").toString();
                            int offset = sf.getAsJsonObject().get("@offset").getAsInt();
                            SurfaceForm surfaceFormObj = new SurfaceForm(name, offset);

                            JsonElement resource = sf.getAsJsonObject().get("resource");
                            if (resource.isJsonArray()) {
                                for (JsonElement rs : resource.getAsJsonArray()) {
                                    if (rs.isJsonObject()) {
                                        surfaceFormObj.addResource(getResourceCandidate(rs));
                                    }
                                }
                                candidatesUnit.addSurfaceForm(surfaceFormObj);
                            } else {
                                if (resource.isJsonObject()) {
                                    surfaceFormObj.addResource(getResourceCandidate(resource));
                                }
                                candidatesUnit.addSurfaceForm(surfaceFormObj);
                            }
                        }
                    }
                } else {
                    if (surfaceForm.isJsonObject()) {
                        String name = surfaceForm.getAsJsonObject().get("@name").toString();
                        int offset = surfaceForm.getAsJsonObject().get("@offset").getAsInt();
                        SurfaceForm surfaceFormObj = new SurfaceForm(name, offset);

                        JsonElement resource = surfaceForm.getAsJsonObject().get("resource");
                        if (resource.isJsonArray()) {
                            for (JsonElement rs : resource.getAsJsonArray()) {
                                if (rs.isJsonObject()) {
                                    surfaceFormObj.addResource(getResourceCandidate(rs));
                                }
                            }
                            candidatesUnit.addSurfaceForm(surfaceFormObj);
                        }
                        if (resource.isJsonObject()) {
                            surfaceFormObj.addResource(getResourceCandidate(resource));
                        }
                        candidatesUnit.addSurfaceForm(surfaceFormObj);
                    }
                }
            }
        }

        for (SurfaceForm sf : candidatesUnit.getSurfaceForm()) {
            for (ResourceCandidate rc : sf.getResources()) {
                fixPrefixes(rc);
            }
        }

        return candidatesUnit;
    }

    private ResourceCandidate getResourceCandidate(JsonElement resource) {
        String label = resource.getAsJsonObject().get("@label").toString();
        String uri = resource.getAsJsonObject().get("@uri").toString();
        String contextualScore = resource.getAsJsonObject().get("@contextualScore").toString();
        String percentageOfSecondRank = resource.getAsJsonObject().get("@percentageOfSecondRank").toString();
        String support = resource.getAsJsonObject().get("@support").toString();
        String priorScore = resource.getAsJsonObject().get("@priorScore").toString();
        String finalScore = resource.getAsJsonObject().get("@finalScore").toString();
        String types = resource.getAsJsonObject().get("@types").toString();

        ResourceCandidate resourceCandidate = new ResourceCandidate(uri, label, contextualScore, percentageOfSecondRank, support, priorScore, finalScore, types);
        return resourceCandidate;
    }

    private String fixPrefixes(String value) {

        if (value != null && !value.isEmpty()) {
            return value.replace("Http", "http").
                    replace("DBpedia:", DBPEDIA_ONTOLOGY).
                    replace("Schema:", SCHEMA_ONTOLOGY);
        }
        return value;

    }

    private void fixPrefixes(ResourceCandidate resource) {
        resource.setTypes(fixPrefixes(resource.getTypes()));
    }

    private void fixPrefixes(ResourceItem resource) {
        resource.setTypes(fixPrefixes(resource.getTypes()));
    }

    private void fixPrefixes(List<ResourceItem> resources) {

        if (resources != null && !resources.isEmpty()) {

            resources.forEach(resourceItem -> fixPrefixes(resourceItem));

        }

    }

    private String getContent(HttpPost request) throws IOException {

        HttpResponse response = client.execute(request);

        BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

        StringBuffer result = new StringBuffer();

        String line = EMPTY;

        while ((line = rd.readLine()) != null) {
            result.append(line);
        }

        return result.toString();
    }

    public AnnotationUnit get(URL url) throws IOException {

        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("url", url.toString()));
        requestAnno.setEntity(new UrlEncodedFormEntity(params));

        return get();
    }

    public CandidatesUnit getCandidates(URL url) throws IOException {

        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("url", url.toString()));
        params.add(new BasicNameValuePair("confidence", String.valueOf(confidence)));
        params.add(new BasicNameValuePair("support", String.valueOf(support)));
        requestCand.setEntity(new UrlEncodedFormEntity(params));

        return getCandidates();
    }

    public AnnotationUnit get(String text) throws IOException {

        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("text", text));
        requestAnno.setEntity(new UrlEncodedFormEntity(params));

        return get();
    }

    public CandidatesUnit getCandidates(String text) throws IOException {

        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("text", text));
        params.add(new BasicNameValuePair("confidence", String.valueOf(confidence)));
        params.add(new BasicNameValuePair("support", String.valueOf(support)));
        requestCand.setEntity(new UrlEncodedFormEntity(params));

        return getCandidates();
    }

    private static void print(AnnotationUnit annotationUnit) {

        if (annotationUnit != null) {
            annotationUnit.getResources().stream().forEach(r -> System.out.println(
                    r.getSurfaceForm() + " " + r.getUri()));
        }
    }

    public static void main(String[] args) throws IOException {
        Spotlight spotlight = new Spotlight(); // init spotlight
        AnnotationUnit annotationUnit; // keeps the returned annotations

        String text1 = "What works written by Phillip Pullman?"; // text to annotate 1
        String text2 = "Who is the father of manu ginobili?"; // text to annotate 2
        String text3 = "where is the birthplace of mario cipollini?"; // text to annotate 3
        String text4 = "Where is rebecca harms from?"; // text to annotate 4
        String text5 = "where did pope clement xi die?"; // text to annotate 5
        String text6 = "what is the nationality of benjamin demott?"; // text to annotate 6

        System.out.println("=== ANNOTATION ===");

        annotationUnit = spotlight.get(text1); // annotate
        print(annotationUnit); // print annotation unit

        annotationUnit = spotlight.get(text2); // annotate
        print(annotationUnit); // print annotation unit

        annotationUnit = spotlight.get(text3); // annotate
        print(annotationUnit); // print annotation unit

        annotationUnit = spotlight.get(text4); // annotate
        print(annotationUnit); // print annotation unit

        annotationUnit = spotlight.get(text5); // annotate
        print(annotationUnit); // print annotation unit

        annotationUnit = spotlight.get(text6); // annotate
        print(annotationUnit); // print annotation unit
    }
}
