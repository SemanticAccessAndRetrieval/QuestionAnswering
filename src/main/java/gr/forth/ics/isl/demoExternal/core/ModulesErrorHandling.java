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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 *
 * @author Lefteris Dimitrakis
 */
public class ModulesErrorHandling {

    public static JSONObject questionAnalysisErrorHandling(QuestionAnalysis q_a) {
        JSONObject answer = new JSONObject();

        String question_type = q_a.getQuestionType();

        if (question_type.equalsIgnoreCase("none")) {
            /*   try {
                answer.put("status", "error");
                answer.put("message", "[QuestionAnalysis] Unrecognized type of question.");
                return answer;
            } catch (JSONException ex) {
                Logger.getLogger(ModulesErrorHandling.class.getName()).log(Level.SEVERE, null, ex);
            }*/
        } else {
            try {
                answer.put("question_type", q_a.getQuestionType());
            } catch (JSONException ex) {
                Logger.getLogger(ModulesErrorHandling.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        try {
            answer.put("status", "ok");
        } catch (JSONException ex) {
            Logger.getLogger(ModulesErrorHandling.class.getName()).log(Level.SEVERE, null, ex);
        }
        return answer;
    }

    public static JSONObject entitiesDetectionErrorHandling(EntitiesDetection e_d) {
        JSONObject answer = new JSONObject();

        Set<String> corenlp_entities = e_d.getCorenlpEntities();
        HashMap<String, String> spotlight_entities_uris = e_d.getSpotlightEntitiesUris();
        if (corenlp_entities.isEmpty() && spotlight_entities_uris.isEmpty()) {
            try {
                answer.put("status", "error");
                answer.put("message", "[EntitiesDetection] No Named Entities recognized.");
                return answer;
            } catch (JSONException ex) {
                Logger.getLogger(ModulesErrorHandling.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            try {
                HashSet<String> question_entities = new HashSet<>();
                question_entities.addAll(corenlp_entities);
                question_entities.addAll(spotlight_entities_uris.keySet());
                answer.put("question_entities", question_entities);
            } catch (JSONException ex) {
                Logger.getLogger(ModulesErrorHandling.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        HashMap<String, String> entities_URIs = e_d.getFinalEntitiesWithURIs();

        if (entities_URIs.isEmpty()) {
            try {
                answer.put("status", "error");
                answer.put("message", "[EntitiesDetection] No retrieved named entities.");
                return answer;
            } catch (JSONException ex) {
                Logger.getLogger(ModulesErrorHandling.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        try {
            answer.put("question_entities", entities_URIs.keySet());
            answer.put("retrievedEntities", entities_URIs);
            answer.put("status", "ok");
        } catch (JSONException ex) {
            Logger.getLogger(ModulesErrorHandling.class.getName()).log(Level.SEVERE, null, ex);
        }
        return answer;

    }

    public static JSONObject answerExtractionErrorHandling(AnswerExtraction a_e, String question_type, HashMap<String, String> entity_URI) {
        JSONObject answer = new JSONObject();

        Set<String> useful_words = a_e.getUsefulWords();

        if (useful_words.isEmpty() && !question_type.equalsIgnoreCase("confirmation")) {
            try {
                answer.put("answer", "I cannot directly answer this question, but I have found the following relevant entities: " + entity_URI.values().toString());
                answer.put("plain_answer", "I cannot directly answer this question, but I have found the following relevant entities: " + entity_URI.values().toString());
                answer.put("status", "error");
                answer.put("message", "[AnswerExtraction] No available useful words.");
                return answer;
            } catch (JSONException ex) {
                Logger.getLogger(ModulesErrorHandling.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            try {
                answer.put("answer", "I cannot directly answer this question, but I have found the following relevant entities: " + entity_URI.values().toString());
                answer.put("plain_answer", "I cannot directly answer this question, but I have found the following relevant entities: " + entity_URI.values().toString());
                answer.put("useful_words", useful_words);
            } catch (JSONException ex) {
                Logger.getLogger(ModulesErrorHandling.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        ArrayList<JSONObject> cand_triples = a_e.getCandidateTriples();
        if (cand_triples == null || cand_triples.isEmpty()) {
            try {
                answer.put("answer", "I cannot directly answer this question, but I have found the following relevant entities: " + entity_URI.values().toString());
                answer.put("plain_answer", "I cannot directly answer this question, but I have found the following relevant entities: " + entity_URI.values().toString());
                answer.put("status", "error");
                answer.put("message", "[AnswerExtraction] No candidate triples found.");
                return answer;
            } catch (JSONException ex) {
                Logger.getLogger(ModulesErrorHandling.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        try {
            answer.put("status", "ok");
        } catch (JSONException ex) {
            Logger.getLogger(ModulesErrorHandling.class.getName()).log(Level.SEVERE, null, ex);
        }

        return answer;

    }
}
