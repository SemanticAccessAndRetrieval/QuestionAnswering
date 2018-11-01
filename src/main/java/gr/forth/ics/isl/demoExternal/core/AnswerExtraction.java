/*
 *  This code belongs to the Semantic Access and Retrieval (SAR) group of the 
 *  Information Systems Laboratory (ISL) of the 
 *  Institute of Computer Science (ICS) of the  
 *  Foundation for Research and Technology - Hellas (FORTH)
 *  Nobody is allowed to use, copy, distribute, or modify this work.
 *  It is published for reasons of research results reproducibility.
 *  (c) 2017 Semantic Access and Retrieval group, All rights reserved
 */
package gr.forth.ics.isl.demoExternal.core;

import static gr.forth.ics.isl.demoExternal.main.ExternalKnowledgeDemoMain.chanel;
import gr.forth.ics.isl.utilities.StringUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 *
 * @author Lefteris Dimitrakis
 */
public class AnswerExtraction {

    public static ArrayList<String> definition_relations = new ArrayList<>(Arrays.asList("comment", "description", "abstract"));

    private ArrayList<JSONObject> candidate_triples;

    public ArrayList<JSONObject> getCandidateTriples() {
        return this.candidate_triples;
    }

    public void setCandidateTriples(ArrayList<JSONObject> triples) {
        this.candidate_triples = triples;
    }

    public void retrieveCandidateTriplesOptimized(HashMap<String, String> entity_URI, String fact, int numOfUsefulWords) {
        String tmp_cand_facts = "";
        ArrayList<JSONObject> cand_facts = new ArrayList<>();

        String max_entity = getEntityWithMaxCardinality(entity_URI);

        // Get the question entities
        Set<String> entities = new HashSet<>(entity_URI.keySet());
        // Remove the selected entity from the set
        entities.remove(max_entity);

        // if there are more than one entities in the question
        // we add the rest of the uris, as words in the fact
        if (!entities.isEmpty()) {
            fact += " ";
            numOfUsefulWords += entities.size();
            for (String entity : entities) {
                fact += entity_URI.get(entity) + " ";
            }
            fact = fact.trim();
        }
        double threshold = 1.0d / numOfUsefulWords;

        // Retrieve all the candidate triples and concatanate the result to construct a string
        for (String str : chanel.checkFactAsJSON(entity_URI.get(max_entity), fact, threshold).get(0)) {
            tmp_cand_facts += str + " ";
        }
        //Store all the candidate triples stored as JSONObjects extracted from the text
        cand_facts.addAll(extractJSONObjectsFromString(tmp_cand_facts));
        Logger.getLogger(AnswerExtraction.class.getName()).log(Level.INFO, "===== Cand. Triples: {0}", cand_facts);

        try {
            cand_facts = getCleanTriples(cand_facts);
        } catch (JSONException ex) {
            Logger.getLogger(AnswerExtraction.class.getName()).log(Level.SEVERE, null, ex);
        }
        Logger.getLogger(AnswerExtraction.class.getName()).log(Level.INFO, "=====Clean Cand. Triples: {0}", cand_facts);

        this.candidate_triples = cand_facts;

    }

    public JSONObject extractAnswer(Set<String> useful_words, String fact, HashMap<String, String> entity_URI, String question_type) {

        if (question_type.equalsIgnoreCase("definition")) {
            JSONObject answer = extractAnswerText(this.candidate_triples, question_type, entity_URI);

            return answer;
        }

        ArrayList<String> cand_relations = extractCandidateRelations(this.candidate_triples);

        String matched_relation = getMatchingProperty(new ArrayList<>(useful_words), cand_relations);

        ArrayList<JSONObject> matched_triples = new ArrayList<>();

        // Here we can use for matching also the sameAs relations
        // (when it will be available for predicates from LODSyndesis)
        for (JSONObject triple : this.candidate_triples) {
            try {
                if (((String) triple.get("predicate")).equalsIgnoreCase(matched_relation)) {
                    matched_triples.add(triple);
                }
            } catch (JSONException ex) {
                Logger.getLogger(AnswerExtraction.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        //TODO: We can check for identical triples in matched_triples and have a list of provenance datasets for this triple
        JSONObject answer = extractAnswerText(matched_triples, question_type, entity_URI);

        return answer;

    }

    public static ArrayList<String> extractCandidateRelations(ArrayList<JSONObject> cand_triples) {
        ArrayList<String> cand_relations = new ArrayList<>();
        String tmp_pred;

        // Extract all the properties from the triples
        for (JSONObject ob : cand_triples) {
            try {
                //Retrieve from JSONObject the predicate of the triple
                tmp_pred = ob.getString("predicate");
                cand_relations.add(tmp_pred);
            } catch (JSONException ex) {
                Logger.getLogger(AnswerExtraction.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        Logger.getLogger(AnswerExtraction.class.getName()).log(Level.INFO, "===== Cand. Relations: {0}", cand_relations);

        return cand_relations;
    }

    // TODO: Maybe pass as argument also a threshold for considering a property as matched
    public static String getMatchingProperty(ArrayList<String> useful_words, ArrayList<String> candidate_predicates) {
        HashMap<String, Float> uri_distance = new HashMap<>();

        int cnt = 0;
        float tmp_distance = 0.0f;
        float min_distance = Float.MAX_VALUE;
        int min_cnt = 0;

        for (String cand_pred : candidate_predicates) {
            for (String useful_word : useful_words) {
                tmp_distance += StringUtils.LevenshteinDistance(useful_word, getSuffixOfURI(cand_pred));
            }
            tmp_distance /= useful_words.size();
            if (tmp_distance < min_distance) {
                min_distance = tmp_distance;
                min_cnt = cnt;
            }
            uri_distance.put(cand_pred, tmp_distance);
            cnt++;
            tmp_distance = 0.0f;
        }

        if (!candidate_predicates.isEmpty()) {
            return candidate_predicates.get(min_cnt);
        } else {
            return "";
        }
    }

    //TODO: To update to more sophisticated answer selection
    // Factoid: Check number of provenance sources for verification?
    public static JSONObject extractAnswerText(ArrayList<JSONObject> matched_triples, String question_type, HashMap<String, String> entity_URI) {

        if (matched_triples.isEmpty()) {
            JSONObject tmp_ans = new JSONObject();
            try {
                tmp_ans.put("answer", "No answer found!");
                return tmp_ans;
            } catch (JSONException ex) {
                Logger.getLogger(AnswerExtraction.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        if (question_type.equalsIgnoreCase("factoid")) {
            return extractFactoidAnswer(matched_triples, entity_URI);
        } else if (question_type.equalsIgnoreCase("confirmation")) {
            return extractConfirmationAnswer(matched_triples, entity_URI);
        } else {
            return extractDefinitionAnswer(matched_triples, entity_URI);
        }
    }

    private static JSONObject extractFactoidAnswer(ArrayList<JSONObject> matched_triples, HashMap<String, String> entity_URI) {

        for (JSONObject triple : matched_triples) {
            try {
                triple.remove("threshold");
                String object = triple.getString("object");
                triple.put("answer", getSuffixOfURI(object));
                return triple;
            } catch (JSONException ex) {
                Logger.getLogger(AnswerExtraction.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return null;
    }

    private static JSONObject extractConfirmationAnswer(ArrayList<JSONObject> matched_triples, HashMap<String, String> entity_URI) {

        HashMap<String, ArrayList<String>> entity_equivalentURIs = getCleanSameAsUris(EntitiesDetection.retrieveEquivalentEntityURIs(entity_URI));

        String subject_uri = "";
        String object_uri = "";

        int matches = 0;

        //For each matching triple
        for (JSONObject triple : matched_triples) {
            try {
                //Extract the subject and object from the triple
                subject_uri = triple.getString("subject");

                object_uri = triple.getString("object");

            } catch (JSONException ex) {
                Logger.getLogger(AnswerExtraction.class.getName()).log(Level.SEVERE, null, ex);
            }

            for (ArrayList<String> uris : entity_equivalentURIs.values()) {
                //For each matching entity uri
                for (String uri : uris) {
                    //if the uri matches either with the subject or the object, increase the matches by 1
                    if (isMatchingUris(uri, subject_uri) || isMatchingUris(uri, object_uri)) {
                        matches++;
                        break;
                    }
                }
                if (matches == 2) {
                    break;
                }
            }

            // If both subject and object matched, then the answer is yes
            if (matches == 2) {
                triple.remove("threshold");
                try {
                    triple.put("answer", "Yes!");
                } catch (JSONException ex) {
                    Logger.getLogger(AnswerExtraction.class.getName()).log(Level.SEVERE, null, ex);
                }
                return triple;
            }
            matches = 0;
        }

        // If not both subject and object matched, then the answer is no
        JSONObject tmp_ans = new JSONObject();

        try {
            tmp_ans.put("answer", "No!");
        } catch (JSONException ex) {
            Logger.getLogger(AnswerExtraction.class.getName()).log(Level.SEVERE, null, ex);
        }
        return tmp_ans;
    }

    private static JSONObject extractDefinitionAnswer(ArrayList<JSONObject> matched_triples, HashMap<String, String> entity_URI) {

        JSONObject tmp_ans = new JSONObject();

        String predicate_uri = "";

        for (String relation : definition_relations) {
            //For each matching triple
            for (JSONObject triple : matched_triples) {
                try {
                    //Extract the predicate from the triple
                    predicate_uri = triple.getString("predicate");
                } catch (JSONException ex) {
                    Logger.getLogger(AnswerExtraction.class.getName()).log(Level.SEVERE, null, ex);
                }
                if (isMatchingUris(predicate_uri, relation)) {
                    triple.remove("threshold");
                    try {
                        triple.put("answer", triple.getString("object"));
                    } catch (JSONException ex) {
                        Logger.getLogger(AnswerExtraction.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    return triple;
                }
            }
        }

        try {
            tmp_ans.put("answer", "No answer found!");
        } catch (JSONException ex) {
            Logger.getLogger(AnswerExtraction.class.getName()).log(Level.SEVERE, null, ex);
        }
        return tmp_ans;
    }

    public static boolean isMatchingUris(String uri1, String uri2) {
        String uri1_text = getSuffixOfURI(uri1).replaceAll("[^a-zA-Z0-9]", "");
        String uri2_text = getSuffixOfURI(uri2).replaceAll("[^a-zA-Z0-9]", "");

        if (uri1.equalsIgnoreCase(uri2) || uri1_text.equalsIgnoreCase(uri2_text)) {
            return true;
        }

        return false;

    }

    public ArrayList<JSONObject> getCleanTriples(ArrayList<JSONObject> triples) throws JSONException {
        ArrayList<JSONObject> clean_triples = new ArrayList<>();
        ArrayList<String> triple_relations = new ArrayList<>(Arrays.asList("predicate", "provenance", "subject", "object"));
        JSONObject tmp;
        for (JSONObject trpl : triples) {
            tmp = new JSONObject();
            for (String relation : triple_relations) {
                String uri = trpl.getString(relation);
                String clean_uri = getCleanUri(uri);
                tmp.put(relation, clean_uri);
            }
            tmp.put("threshold", trpl.getString("threshold"));
            clean_triples.add(tmp);
        }
        return clean_triples;
    }

    public static HashMap<String, ArrayList<String>> getCleanSameAsUris(HashMap<String, ArrayList<String>> uri_sameAs) {
        HashMap<String, ArrayList<String>> clean_uri_sameAs = new HashMap<>();

        ArrayList<String> tmp_clean_sameAs;
        for (String entity_uri : uri_sameAs.keySet()) {
            tmp_clean_sameAs = new ArrayList<>();
            for (String sameAs : uri_sameAs.get(entity_uri)) {
                String clean = getCleanUri(sameAs);
                tmp_clean_sameAs.add(clean);
            }
            clean_uri_sameAs.put(entity_uri, tmp_clean_sameAs);
        }

        return clean_uri_sameAs;
    }

    public static String getCleanUri(String uri) {
        String clean_uri = uri;

        if (uri.startsWith("<") && uri.endsWith(">")) {
            clean_uri = uri.substring(1, uri.length() - 1);
        } else {
            String tmp_uri = uri.replaceAll("\"", "");

            if ((tmp_uri.startsWith("[[") && tmp_uri.endsWith("]]")) || (tmp_uri.startsWith("{{") && tmp_uri.endsWith("}}"))) {
                clean_uri = tmp_uri.substring(2, tmp_uri.length() - 2);
            } else {
                clean_uri = tmp_uri;

                Pattern p = Pattern.compile("href=(.*?)\\>");
                Matcher m = p.matcher(clean_uri);
                if (m.find()) {
                    clean_uri = m.group(1); // this variable should contain the link URL
                }

            }

        }

        return clean_uri;
    }

    public static String getEntityWithMaxCardinality(HashMap<String, String> entity_URI) {
        String final_entity = "";
        int max_card = Integer.MIN_VALUE;

        String cardinality_result = "";
        JSONObject uri_cardinality;

        for (String entity : entity_URI.keySet()) {
            cardinality_result = "";
            for (String str : chanel.getCardinalityAsJSON(entity_URI.get(entity)).get(0)) {
                cardinality_result += str + " ";
            }

            uri_cardinality = AnswerExtraction.extractJSONObjectsFromString(cardinality_result).get(0);

            try {
                if (uri_cardinality.getInt("cardinality") > max_card) {
                    max_card = uri_cardinality.getInt("cardinality");
                    final_entity = entity;
                }
            } catch (JSONException ex) {
                Logger.getLogger(AnswerExtraction.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return final_entity;
    }

    public static ArrayList<JSONObject> extractJSONObjectsFromString(String cand_facts) {

        List<String> matches = new ArrayList<String>();

        Pattern ptn = Pattern.compile("\\{.*?\\}");

        Matcher mtch = ptn.matcher(cand_facts);

        while (mtch.find()) {
            matches.add(mtch.group());
        }

        ArrayList<JSONObject> cand_triples = new ArrayList<>();
        JSONObject tmp_object;
        for (String object_str : matches) {
            try {
                tmp_object = new JSONObject(object_str);
                cand_triples.add(tmp_object);
            } catch (JSONException ex) {
                Logger.getLogger(AnswerExtraction.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return cand_triples;
    }

    public static String getSuffixOfURI(String uri) {
        String[] tmp = uri.split("\\/|#");
        String suffix = tmp[tmp.length - 1];
        return suffix;
    }

}
