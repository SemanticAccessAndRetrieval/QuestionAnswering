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
            try {
                answer.put("status", "error");
                answer.put("message", "[QuestionAnalysis] Unrecognized type of question.");
                return answer;
            } catch (JSONException ex) {
                Logger.getLogger(ModulesErrorHandling.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        Set<String> question_entities = q_a.getQuestionEntities();

        if (question_entities.isEmpty()) {
            try {
                answer.put("status", "error");
                answer.put("message", "[QuestionAnalysis] No Named Entities recognized.");
                return answer;
            } catch (JSONException ex) {
                Logger.getLogger(ModulesErrorHandling.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        Set<String> useful_words = q_a.getUsefulWords();

        if (useful_words.isEmpty()) {
            try {
                answer.put("status", "error");
                answer.put("message", "[QuestionAnalysis] No available useful words.");
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

    public static JSONObject entitiesDetectionErrorHandling(EntitiesDetection e_d) {
        JSONObject answer = new JSONObject();

        HashMap<String, ArrayList<String>> entity_candidateURIs = e_d.getEntitiesWithCandidateURIs();
        ArrayList<String> cand_URIs;

        // If we don't have a uri for an entity we cannot deliver any answer!
        for (String entity : entity_candidateURIs.keySet()) {
            cand_URIs = entity_candidateURIs.get(entity);
            if (cand_URIs == null) {
                try {
                    answer.put("status", "error");
                    answer.put("message", "[EntitiesDetection] No retrieved URIs for entity: " + entity + ". Error while querying LODSyndesis.");
                    return answer;
                } catch (JSONException ex) {
                    Logger.getLogger(ModulesErrorHandling.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else if (cand_URIs.isEmpty()) {

                try {
                    answer.put("status", "error");
                    answer.put("message", "[EntitiesDetection] No retrieved URIs for entity: " + entity + ". Zero URIs retrieved from LODSyndesis.");
                    return answer;
                } catch (JSONException ex) {
                    Logger.getLogger(ModulesErrorHandling.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

        try {
            answer.put("status", "ok");
        } catch (JSONException ex) {
            Logger.getLogger(ModulesErrorHandling.class.getName()).log(Level.SEVERE, null, ex);
        }
        return answer;

    }

    public static JSONObject answerExtractionErrorHandling(AnswerExtraction a_e) {
        JSONObject answer = new JSONObject();
        ArrayList<JSONObject> cand_triples = a_e.getCandidateTriples();
        if (cand_triples == null || cand_triples.isEmpty()) {
            try {
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
