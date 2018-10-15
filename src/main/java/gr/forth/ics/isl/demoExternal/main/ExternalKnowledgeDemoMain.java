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

import edu.mit.jwi.Dictionary;
import edu.mit.jwi.IDictionary;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import gr.forth.ics.isl.demoExternal.LODsyndesis.LODSyndesisChanel;
import gr.forth.ics.isl.demoExternal.core.AnswerExtraction;
import gr.forth.ics.isl.demoExternal.core.EntitiesDetection;
import gr.forth.ics.isl.demoExternal.core.ModulesErrorHandling;
import gr.forth.ics.isl.demoExternal.core.QuestionAnalysis;
import gr.forth.ics.isl.utilities.StringUtils;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 *
 * @author Lefteris Dimitrakis
 */
public class ExternalKnowledgeDemoMain {

    //The paths to the stopwords file
    public static String filePath_en = "/stoplists/stopwordsEn.txt";
    public static String filePath_gr = "/stoplists/stopwordsGr.txt";

    //Core Nlp pipeline instance
    public static StanfordCoreNLP split_pipeline;
    public static StanfordCoreNLP ner_pipeline;
    public static IDictionary wordnet_dict;
    public static ArrayList<String> wordnetResources = new ArrayList<>();

    public static LODSyndesisChanel chanel;

    public static void main(String[] args) {

        try {
            initializeToolsAndResources("WNHOME");
        } catch (IOException ex) {
            Logger.getLogger(ExternalKnowledgeDemoMain.class.getName()).log(Level.SEVERE, null, ex);
        }

        // Factoid Questions
        String fact1 = "What is the population of Kyoto?";
        String fact2 = "Which is the capital city of Japan?";
        String fact3 = "Which is the capital of Japan?";
        String fact4 = "What does Nintendo sell?";
        String fact5 = "Where is Mitsubishi located?";
        String fact6 = "Which is the foundation place of Sony?";
        String fact7 = "Which is the death place of Nujabes?";
        String fact8 = "Where is Mount Everest located?";
        String fact9 = "Where did Nujabes died?";  // not answerable (died does not match with deathPlace)

        // Confirmation Questions
        String conf1 = "Is Nintendo located in Kyoto?";
        String conf2 = "Is Tokyo the capital of Japan?";
        String conf3 = "Is Kyoto the capital of Japan?";

        // Definition Questions
        String def1 = "What does Kyoto mean?";
        String def2 = "What is Mount Everest?";
        String def3 = "What is Nintendo?";

        // ==== Question Analysis Step ====
        QuestionAnalysis q_analysis = new QuestionAnalysis();
        q_analysis.analyzeQuestion(def2);

        JSONObject q_aErrorHandling = ModulesErrorHandling.questionAnalysisErrorHandling(q_analysis);
        try {
            if (q_aErrorHandling.getString("status").equalsIgnoreCase("error")) {
                String error_message = q_aErrorHandling.getString("message");
                Logger.getLogger(ExternalKnowledgeDemoMain.class.getName()).log(Level.WARNING, error_message);
                return;
            }
        } catch (JSONException ex) {
            Logger.getLogger(ExternalKnowledgeDemoMain.class.getName()).log(Level.SEVERE, null, ex);
        }

        String question_type = q_analysis.getQuestionType();

        // Store the useful words of the question
        Set<String> useful_words = q_analysis.getUsefulWords();

        // Store the text of the Named Entities
        Set<String> entities = q_analysis.getQuestionEntities();

        String fact = q_analysis.getFact();

        // ==== Entities Detection Step ====
        EntitiesDetection entities_detection = new EntitiesDetection();

        // Retrieve for each entity its candidate URIs from LODSyndesis
        entities_detection.retrieveCandidateEntityURIs(entities);

        JSONObject e_dErrorHandling = ModulesErrorHandling.entitiesDetectionErrorHandling(entities_detection);
        try {
            if (e_dErrorHandling.getString("status").equalsIgnoreCase("error")) {
                String error_message = e_dErrorHandling.getString("message");
                Logger.getLogger(ExternalKnowledgeDemoMain.class.getName()).log(Level.WARNING, error_message);
                return;
            }
        } catch (JSONException ex) {
            Logger.getLogger(ExternalKnowledgeDemoMain.class.getName()).log(Level.SEVERE, null, ex);
        }

        // Hashmap to store each entity and the selected URI (the highest scored)
        HashMap<String, String> entity_URI = entities_detection.getMatchingURIs(entities);

        // ==== Answer Extraction Step ====
        AnswerExtraction answer_extraction = new AnswerExtraction();
        answer_extraction.retrieveCandidateTriplesOptimized(entity_URI, fact, useful_words.size());

        JSONObject a_eErrorHandling = ModulesErrorHandling.answerExtractionErrorHandling(answer_extraction);
        try {
            if (a_eErrorHandling.getString("status").equalsIgnoreCase("error")) {
                String error_message = a_eErrorHandling.getString("message");
                Logger.getLogger(ExternalKnowledgeDemoMain.class.getName()).log(Level.WARNING, error_message);
                return;
            }
        } catch (JSONException ex) {
            Logger.getLogger(ExternalKnowledgeDemoMain.class.getName()).log(Level.SEVERE, null, ex);
        }

        JSONObject answer = answer_extraction.extractAnswer(useful_words, fact, entity_URI, question_type);

        Logger.getLogger(ExternalKnowledgeDemoMain.class.getName()).log(Level.INFO, "===== Answer: {0}", answer);

    }

    public ExternalKnowledgeDemoMain(String wordnetPath) {

    }

    // Appropriate method for the Toshiba demo.
    // Avoid initializing all the common resources/tool with the user comments demo
    // Specifically, avoid initiliaze wordnet and stop-words.
    public static void initializeToolsAndResourcesForDemo(IDictionary dict) throws MalformedURLException, IOException {
        wordnet_dict = dict;
        wordnet_dict.open();

        // Choose wordnet sources to be used
        wordnetResources.add("synonyms");
        //wordnetResources.add("antonyms");
        //wordnetResources.add("hypernyms");

        Properties split_props = new Properties();
        //Properties including lemmatization
        //props.put("annotators", "tokenize, ssplit, pos, lemma");
        //Properties without lemmatization
        split_props.put("annotators", "tokenize, ssplit, pos");
        split_props.put("tokenize.language", "en");
        split_pipeline = new StanfordCoreNLP(split_props);

        Properties ner_props = new Properties();
        ner_props.put("annotators", "tokenize, ssplit, truecase, pos, lemma,  ner");
        ner_props.put("tokenize.language", "en");
        ner_props.put("truecase.overwriteText", "true");
        ner_pipeline = new StanfordCoreNLP(ner_props);

        chanel = new LODSyndesisChanel();

    }

    public static void initializeToolsAndResources(String wordnetPath) throws MalformedURLException, IOException {

        Logger.getLogger(ExternalKnowledgeDemoMain.class.getName()).log(Level.INFO, "...Generating stop-words lists...");
        StringUtils.generateStopListsFromExternalSource(filePath_en, filePath_gr);

        Logger.getLogger(QuestionAnalysis.class.getName()).log(Level.INFO, "...loading wordnet...");
        String wnhome = System.getenv(wordnetPath);
        String path = wnhome + File.separator + "dict";
        URL url = new URL("file", null, path);
        // construct the dictionary object and open it
        wordnet_dict = new Dictionary(url);
        wordnet_dict.open();

        // Choose wordnet sources to be used
        wordnetResources.add("synonyms");
        //wordnetResources.add("antonyms");
        //wordnetResources.add("hypernyms");

        Properties split_props = new Properties();
        //Properties including lemmatization
        //props.put("annotators", "tokenize, ssplit, pos, lemma");
        //Properties without lemmatization
        split_props.put("annotators", "tokenize, ssplit, pos");
        split_props.put("tokenize.language", "en");
        split_pipeline = new StanfordCoreNLP(split_props);

        Properties ner_props = new Properties();
        ner_props.put("annotators", "tokenize, ssplit, truecase, pos, lemma,  ner");
        ner_props.put("tokenize.language", "en");
        ner_props.put("truecase.overwriteText", "true");
        ner_pipeline = new StanfordCoreNLP(ner_props);

        chanel = new LODSyndesisChanel();

    }

    public static JSONObject getAnswerAsJson(String query) {
        try {
            JSONObject obj = new JSONObject();

            obj.put("source", "external");

            // ==== Question Analysis Step ====
            QuestionAnalysis q_analysis = new QuestionAnalysis();
            q_analysis.analyzeQuestion(query);

            JSONObject q_aErrorHandling = ModulesErrorHandling.questionAnalysisErrorHandling(q_analysis);

            if (q_aErrorHandling.getString("status").equalsIgnoreCase("error")) {
                return constructErrorJson(obj, q_aErrorHandling, "questionAnalysis");
            }

            String question_type = q_analysis.getQuestionType();

            obj.put("question_type", question_type);

            // Store the useful words of the question
            Set<String> useful_words = q_analysis.getUsefulWords();

            obj.put("useful_words", useful_words);

            // Store the text of the Named Entities
            Set<String> entities = q_analysis.getQuestionEntities();

            obj.put("question_entities", entities);

            String fact = q_analysis.getFact();

            // ==== Entities Detection Step ====
            EntitiesDetection entities_detection = new EntitiesDetection();

            // Retrieve for each entity its candidate URIs from LODSyndesis
            entities_detection.retrieveCandidateEntityURIs(entities);

            JSONObject e_dErrorHandling = ModulesErrorHandling.entitiesDetectionErrorHandling(entities_detection);

            if (e_dErrorHandling.getString("status").equalsIgnoreCase("error")) {
                return constructErrorJson(obj, e_dErrorHandling, "entitiesDetection");
            }

            // Hashmap to store each entity and the selected URI (the highest scored)
            HashMap<String, String> entity_URI = entities_detection.getMatchingURIs(entities);

            obj.put("retrievedEntities", entity_URI);

            // ==== Answer Extraction Step ====
            AnswerExtraction answer_extraction = new AnswerExtraction();
            answer_extraction.retrieveCandidateTriplesOptimized(entity_URI, fact, useful_words.size());

            JSONObject a_eErrorHandling = ModulesErrorHandling.answerExtractionErrorHandling(answer_extraction);

            if (a_eErrorHandling.getString("status").equalsIgnoreCase("error")) {
                return constructErrorJson(obj, a_eErrorHandling, "answerExtraction");
            }

            JSONObject answer_triple = answer_extraction.extractAnswer(useful_words, fact, entity_URI, question_type);

            Logger.getLogger(ExternalKnowledgeDemoMain.class.getName()).log(Level.INFO, "===== Answer: {0}", answer_triple);

            obj.put("answer", answer_triple.get("answer"));
            answer_triple.remove("answer");
            obj.put("triple", answer_triple);

            return obj;

        } catch (JSONException ex) {
            Logger.getLogger(ExternalKnowledgeDemoMain.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }

    private static JSONObject constructErrorJson(JSONObject current_answer, JSONObject error, String module) {
        ArrayList<String> qA_tags = new ArrayList<>(Arrays.asList("question_type", "question_entities", "useful_words"));
        ArrayList<String> eD_tags = new ArrayList<>(Arrays.asList("retrievedEntities"));

        try {
            String error_message = error.getString("message");
            Logger.getLogger(ExternalKnowledgeDemoMain.class.getName()).log(Level.WARNING, error_message);
            current_answer.put("errorMessage", error_message);
        } catch (JSONException ex) {
            Logger.getLogger(ExternalKnowledgeDemoMain.class.getName()).log(Level.SEVERE, null, ex);
        }

        if (module.equalsIgnoreCase("questionAnalysis")) {

            for (String tag : qA_tags) {
                if (error.has(tag)) {
                    try {
                        current_answer.put(tag, error.get(tag));
                    } catch (JSONException ex) {
                        Logger.getLogger(ExternalKnowledgeDemoMain.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
            return current_answer;
        } else if (module.equalsIgnoreCase("entitiesDetection")) {
            for (String tag : eD_tags) {
                if (error.has(tag)) {
                    try {
                        current_answer.put(tag, error.get(tag));
                    } catch (JSONException ex) {
                        Logger.getLogger(ExternalKnowledgeDemoMain.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
            return current_answer;
        }
        return current_answer;

    }

    public static JSONObject getEvaluationAnswerAsJson(String query) {
        try {
            JSONObject obj = new JSONObject();

            obj.put("question", query.replaceAll("\"", "").trim());

            // ==== Question Analysis Step ====
            QuestionAnalysis q_analysis = new QuestionAnalysis();
            q_analysis.analyzeQuestion(query);

            JSONObject q_aErrorHandling = ModulesErrorHandling.questionAnalysisErrorHandling(q_analysis);

            if (q_aErrorHandling.getString("status").equalsIgnoreCase("error")) {
                return constructErrorJson(obj, q_aErrorHandling, "questionAnalysis");
            }

            String question_type = q_analysis.getQuestionType();

            obj.put("question_type", question_type);

            // Store the useful words of the question
            Set<String> useful_words = q_analysis.getUsefulWords();

            obj.put("useful_words", useful_words);

            // Store the text of the Named Entities
            Set<String> entities = q_analysis.getQuestionEntities();

            obj.put("question_entities", entities);

            System.out.println("entities prin: " + entities);

            Set<String> tmp_entities = new HashSet<>(entities);
            for (String entity : tmp_entities) {
                if (org.apache.commons.lang3.StringUtils.isNumericSpace(entity)) {
                    entities.remove(entity);
                }
            }
            System.out.println("entities meta: " + entities);
            String fact = q_analysis.getFact();

            // ==== Entities Detection Step ====
            EntitiesDetection entities_detection = new EntitiesDetection();

            // Retrieve for each entity its candidate URIs from LODSyndesis
            entities_detection.retrieveCandidateEntityURIs(entities);

            JSONObject e_dErrorHandling = ModulesErrorHandling.entitiesDetectionErrorHandling(entities_detection);

            if (e_dErrorHandling.getString("status").equalsIgnoreCase("error")) {
                return constructErrorJson(obj, e_dErrorHandling, "entitiesDetection");
            }

            // Hashmap to store each entity and the selected URI (the highest scored)
            HashMap<String, String> entity_URI = entities_detection.getMatchingURIs(entities);

            obj.put("retrievedEntities", entity_URI);

            // ==== Answer Extraction Step ====
            AnswerExtraction answer_extraction = new AnswerExtraction();
            answer_extraction.retrieveCandidateTriplesOptimized(entity_URI, fact, useful_words.size());

            JSONObject a_eErrorHandling = ModulesErrorHandling.answerExtractionErrorHandling(answer_extraction);

            if (a_eErrorHandling.getString("status").equalsIgnoreCase("error")) {
                return constructErrorJson(obj, a_eErrorHandling, "answerExtraction");
            }

            JSONObject answer_triple = answer_extraction.extractAnswer(useful_words, fact, entity_URI, question_type);

            Logger.getLogger(ExternalKnowledgeDemoMain.class.getName()).log(Level.INFO, "===== Answer: {0}", answer_triple);

            answer_triple.remove("answer");
            obj.put("triple", answer_triple);

            return obj;

        } catch (JSONException ex) {
            Logger.getLogger(ExternalKnowledgeDemoMain.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }
}
