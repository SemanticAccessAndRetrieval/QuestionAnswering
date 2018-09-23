/*
 *  This code belongs to the Semantic Access and Retrieval (SAR) group of the 
 *  Information Systems Laboratory (ISL) of the 
 *  Institute of Computer Science (ICS) of the  
 *  Foundation for Research and Technology - Hellas (FORTH)
 *  Nobody is allowed to use, copy, distribute, or modify this work.
 *  It is published for reasons of research results reproducibility.
 *  (c) 2017 Semantic Access and Retrieval group, All rights reserved
 */
package gr.forth.ics.isl.demoExternal.main;

import gr.forth.ics.isl.demoExternal.LODsyndesis.LODSyndesisChanel;
import gr.forth.ics.isl.demoExternal.core.AnswerExtraction;
import gr.forth.ics.isl.demoExternal.core.EntitiesDetection;
import gr.forth.ics.isl.demoExternal.core.QuestionAnalysis;
import java.util.HashMap;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Lefteris Dimitrakis
 */
public class ExternalKnowledgeDemoMain {

    public static LODSyndesisChanel chanel;

    public static void main(String[] args) {

        // Question 1 on focus (factoid)
        String query1 = "What is the population of Kyoto?";

        // Question 2 on focus (confirmation)
        String query2 = "Is Nintendo located in Kyoto?";

        // Question 3 on focus (definition)
        String query3 = "What does Kyoto mean?";

        // ==== Question Analysis Step ====
        QuestionAnalysis q_analysis = new QuestionAnalysis("WNHOME");
        q_analysis.analyzeQuestion(query1);

        String question_type = q_analysis.getQuestionType();

        if (question_type.equals("none")) {
            Logger.getLogger(ExternalKnowledgeDemoMain.class.getName()).log(Level.INFO, "===== Answer: {0}", "Unrecognized type of question. No answer found!");
        } else {
        // Store the useful words of the question
        Set<String> useful_words = q_analysis.getUsefulWords();

        // Store the text of the Named Entities
        Set<String> entities = q_analysis.getQuestionEntities();

        String fact = q_analysis.getFact();

        // ==== Entities Detection Step ====
        // Hashmap to store each entity and the selected URI (the highest scored)
        HashMap<String, String> entity_URI = EntitiesDetection.retrieveMatchingURIs(entities);

        // ==== Answer Extraction Step ====
        String answer = AnswerExtraction.extractAnswer(useful_words, fact, entity_URI, question_type);

        Logger.getLogger(ExternalKnowledgeDemoMain.class.getName()).log(Level.INFO, "===== Answer: {0}", answer);
        }
    }

    public ExternalKnowledgeDemoMain(String wordnetPath) {

    }

    /*
    public JSONObject getAnalyzedQueryAsJson(String query) {
        try {
            JSONObject obj = new JSONObject();

            HashMap<String, String> clean_query = getCleanTokensWithPos(query);

            obj.put("clean_query", clean_query.keySet());

            ArrayList<String> expanded_query = getCleanExpandedQuery(query);

            obj.put("expanded_query", expanded_query);

            HashMap<String, String> word_NamedEntity = getTokensWithNer(query);

            obj.put("recognized_NamedEntities", word_NamedEntity);

            return obj;
        } catch (JSONException ex) {
            Logger.getLogger(ExternalKnowledgeDemoMain.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }
    */
}
