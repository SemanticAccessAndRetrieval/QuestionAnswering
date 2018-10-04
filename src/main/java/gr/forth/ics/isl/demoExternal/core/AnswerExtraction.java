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
import java.util.HashMap;
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

    public static JSONObject extractAnswer(Set<String> useful_words, String fact, HashMap<String, String> entity_URI, String question_type) {

        ArrayList<JSONObject> cand_triples = retrieveCandidateTriples(entity_URI, fact);

        ArrayList<String> cand_relations = extractCandidateRelations(cand_triples);

        String matched_relation = getMatchingProperty(new ArrayList<>(useful_words), cand_relations);

        ArrayList<JSONObject> matched_triples = new ArrayList<>();

        for (JSONObject triple : cand_triples) {
            try {
                if (((String) triple.get("predicate")).equalsIgnoreCase("<" + matched_relation + ">")) {
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

    //TODO: To update to more sophisticated answer selection
    // Factoid: Check number of provenance sources for verification?
    // Definition: To create a 1st draft
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
                triple.put("answer", getSuffixOfURI((String) triple.get("object")));
                return triple;
            } catch (JSONException ex) {
                Logger.getLogger(AnswerExtraction.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return null;
    }

    private static JSONObject extractConfirmationAnswer(ArrayList<JSONObject> matched_triples, HashMap<String, String> entity_URI) {

        HashMap<String, ArrayList<String>> entity_equivalentURIs = EntitiesDetection.retrieveEquivalentEntityURIs(entity_URI);

        String subject_uri = "";
        String object_uri = "";

        int matches = 0;

        //For each matching triple
        for (JSONObject triple : matched_triples) {
            try {
                //Extract the subject and object from the triple
                subject_uri = triple.getString("subject").substring(1, triple.getString("subject").length() - 1);

                object_uri = triple.getString("object").substring(1, triple.getString("object").length() - 1);

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

        try {
            tmp_ans.put("answer", "No answer found!");
        } catch (JSONException ex) {
            Logger.getLogger(AnswerExtraction.class.getName()).log(Level.SEVERE, null, ex);
        }
        return tmp_ans;
    }

    private static boolean isMatchingUris(String uri1, String uri2) {
        String uri1_text = getSuffixOfURI(uri1).replaceAll("[^a-zA-Z0-9]", "");
        String uri2_text = getSuffixOfURI(uri2).replaceAll("[^a-zA-Z0-9]", "");

        if (uri1.equalsIgnoreCase(uri2) || uri1_text.equalsIgnoreCase(uri2_text)) {
            return true;
        }

        return false;

    }

    // TODO: avoid unnecessary calls to lodsyndesis, query only the entity with the most triples?
    // Needs optimization
    public static ArrayList<JSONObject> retrieveCandidateTriples(HashMap<String, String> entity_URI, String fact) {
        String tmp_cand_facts = "";
        ArrayList<JSONObject> cand_facts = new ArrayList<>();

        // For each entity
        for (String entity : entity_URI.keySet()) {
            // Retrieve all the candidate triples and concatanate the result to construct a string
            for (String str : chanel.checkFactAsJSON(entity_URI.get(entity), fact, 0.5).get(0)) {
                tmp_cand_facts += str + " ";
            }
            //Store all the candidate triples stored as JSONObjects extracted from the text
            cand_facts.addAll(extractJSONObjectsFromString(tmp_cand_facts));
        }
        Logger.getLogger(AnswerExtraction.class.getName()).log(Level.INFO, "===== Cand. Triples: {0}", cand_facts);

        return cand_facts;
    }

    public static ArrayList<String> extractCandidateRelations(ArrayList<JSONObject> cand_triples) {
        ArrayList<String> cand_relations = new ArrayList<>();
        String tmp_pred;

        // Extract all the properties from the triples
        for (JSONObject ob : cand_triples) {
            try {
                //Retrieve from JSONObject the predicate of the triple
                tmp_pred = (String) ob.get("predicate");
                // Remove the surrounding symbols < ...URI... >
                cand_relations.add(tmp_pred.substring(1, tmp_pred.length() - 1));
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

    public static ArrayList<JSONObject> extractJSONObjectsFromString(String cand_facts) {

        List<String> matches = new ArrayList<String>();

        Pattern ptn = Pattern.compile("\\{.*?\\}");

        Matcher mtch = ptn.matcher(cand_facts);

        while (mtch.find()) {
            matches.add(mtch.group());
        }

        ArrayList<JSONObject> cand_triples = new ArrayList<>();

        for (String object_str : matches) {
            try {
                cand_triples.add(new JSONObject(object_str));
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
