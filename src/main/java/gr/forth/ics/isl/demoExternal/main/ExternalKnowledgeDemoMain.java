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

import edu.mit.jwi.IDictionary;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import gr.forth.ics.isl.demoExternal.LODsyndesis.LODSyndesisChanel;
import gr.forth.ics.isl.demoExternal.core.AnswerExtraction;
import gr.forth.ics.isl.demoExternal.core.EntitiesDetection;
import gr.forth.ics.isl.demoExternal.core.ModulesErrorHandling;
import gr.forth.ics.isl.demoExternal.core.QuestionAnalysis;
import gr.forth.ics.isl.nlp.externalTools.Spotlight;
import gr.forth.ics.isl.utilities.StringUtils;
import java.io.IOException;
import java.net.MalformedURLException;
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
 * This is the main class for the execution of the QA process. The constructor
 * of the class is responsible to initialize all the required resources and
 * tools. Using the created instance, the user can use the methods
 * "getAnswerAsJson", to submit a question in Natural Language and retrieve an
 * answer in JSON format.
 *
 * @author Lefteris Dimitrakis
 */
public class ExternalKnowledgeDemoMain {

    //The paths to the stopwords file
    public static String filePath_en = "/stoplists/stopwordsEn.txt";
    public static String filePath_gr = "/stoplists/stopwordsGr.txt";

    //Core Nlp pipeline instance (for question analysis)
    public static StanfordCoreNLP split_pipeline;
    //Core Nlp pipeline instance (for answer extraction)
    public static StanfordCoreNLP lemma_pipeline;
    //Core Nlp pipeline instance (for entities detection)
    public static StanfordCoreNLP entityMentions_pipeline;
    public static StanfordCoreNLP compounds_pipeline;
    public static IDictionary wordnet_dict;
    //DBPedia Spotlight instance
    public static Spotlight spotlight;
    public static ArrayList<String> wordnetResources = new ArrayList<>();

    // LODSyndesisChanel instance, for accessing the provided rest api.
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

        // Confirmation Questions
        String conf1 = "Is Nintendo located in Kyoto?";
        String conf2 = "Is Tokyo the capital of Japan?";
        String conf3 = "Is Kyoto the capital of Japan?";

        // Definition Questions
        String def1 = "What does Kyoto mean?";
        String def2 = "What is Mount Everest?";
        String def3 = "What is Nintendo?";

        System.out.println(getAnswerAsJson("Is Crete located in Greece?", "plain"));
    }

    public ExternalKnowledgeDemoMain() {
        try {
            initializeToolsAndResources("");
        } catch (IOException ex) {
            Logger.getLogger(ExternalKnowledgeDemoMain.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    /**
     * Function for the initialization of the required resources. Appropriate
     * method for the Toshiba demo: Avoid initializing all the common
     * resources/tools with the user reviews demo (i.e. wordnet and stop-words)
     *
     * @param dict
     * @throws MalformedURLException
     * @throws IOException
     */
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

        Properties lemma_props = new Properties();
        lemma_props.put("annotators", "tokenize, ssplit, pos, lemma");
        lemma_props.put("tokenize.language", "en");
        lemma_pipeline = new StanfordCoreNLP(lemma_props);

        Properties entityMentions_props = new Properties();
        entityMentions_props.put("annotators", "tokenize, ssplit, truecase, pos, lemma,  ner, entitymentions");
        entityMentions_props.put("tokenize.language", "en");
        entityMentions_props.put("truecase.overwriteText", "true");
        entityMentions_pipeline = new StanfordCoreNLP(entityMentions_props);

        Properties compound_props = new Properties();
        compound_props.put("annotators", "tokenize, ssplit, truecase, pos, parse");
        compound_props.put("tokenize.language", "en");
        compound_props.put("truecase.overwriteText", "true");
        compounds_pipeline = new StanfordCoreNLP(compound_props);

        spotlight = new Spotlight();

        chanel = new LODSyndesisChanel();

    }

    /**
     * Function for the initialization of the required resources. i.e. all the
     * coreNLP properties, stop-words, spotlight, lodsyndesis chanel etc.
     *
     * @param wordnetPath
     * @throws MalformedURLException
     * @throws IOException
     */
    public static void initializeToolsAndResources(String wordnetPath) throws MalformedURLException, IOException {

        Logger.getLogger(ExternalKnowledgeDemoMain.class.getName()).log(Level.INFO, "...Generating stop-words lists...");
        StringUtils.generateStopListsFromExternalSource(filePath_en, filePath_gr);

        Properties split_props = new Properties();
        //Properties without lemmatization
        split_props.put("annotators", "tokenize, ssplit, pos");
        split_props.put("tokenize.language", "en");
        split_pipeline = new StanfordCoreNLP(split_props);

        Properties lemma_props = new Properties();
        lemma_props.put("annotators", "tokenize, ssplit, pos, lemma");
        lemma_props.put("tokenize.language", "en");
        lemma_pipeline = new StanfordCoreNLP(lemma_props);

        Properties entityMentions_props = new Properties();
        entityMentions_props.put("annotators", "tokenize, ssplit, truecase, pos, lemma,  ner, entitymentions");
        entityMentions_props.put("tokenize.language", "en");
        entityMentions_props.put("truecase.overwriteText", "true");
        entityMentions_pipeline = new StanfordCoreNLP(entityMentions_props);

        Properties compound_props = new Properties();
        compound_props.put("annotators", "tokenize, ssplit, truecase, pos, parse");
        compound_props.put("tokenize.language", "en");
        compound_props.put("truecase.overwriteText", "true");
        compounds_pipeline = new StanfordCoreNLP(compound_props);

        spotlight = new Spotlight();

        chanel = new LODSyndesisChanel();

    }

    /**
     * Function for submitting a question in Natural Language and retrieve an
     * answer in JSON format. This function executes the whole QA pipeline, to
     * analyze the input question and retrieve an answer. The JSON contains
     * information like: question type, question entities, answer triple,
     * provenance, confidence score etc.
     *
     * This function is being exploited for the online demo, which takes an
     * additional input "format" with value "plain" or "triple".
     *
     * @param query
     * @param format
     * @return
     */
    public static JSONObject getAnswerAsJson(String query, String format) {
        try {
            JSONObject obj = new JSONObject();

            obj.put("source", "external");

            // ==== Question Analysis Step ====
            QuestionAnalysis q_analysis = new QuestionAnalysis();
            q_analysis.analyzeQuestion(query);

            String question = q_analysis.getQuestion();

            obj.put("question", question);

            JSONObject q_aErrorHandling = ModulesErrorHandling.questionAnalysisErrorHandling(q_analysis);

            if (q_aErrorHandling.getString("status").equalsIgnoreCase("error")) {
                return constructErrorJson(obj, q_aErrorHandling, "questionAnalysis");
            }

            String question_type = q_analysis.getQuestionType();


            // ==== Entities Detection Step ====
            EntitiesDetection entities_detection = new EntitiesDetection();
            String NEtool = "both";

            // identify NamedEntities in the question using SCNLP and Spotlight
            entities_detection.identifyNamedEntities(question, NEtool);

            HashMap<String, String> entity_URI = entities_detection.extractEntitiesWithUris(question, NEtool);

            JSONObject e_dErrorHandling = ModulesErrorHandling.entitiesDetectionErrorHandling(entities_detection);

            if (e_dErrorHandling.getString("status").equalsIgnoreCase("error")) {
                obj.put("question_type", question_type);
                return constructErrorJson(obj, e_dErrorHandling, "entitiesDetection");
            }

            obj.put("question_entities", entity_URI.keySet());
            obj.put("retrievedEntities", entity_URI);

            // ==== Answer Extraction Step ====
            AnswerExtraction answer_extraction = new AnswerExtraction();

            ArrayList<String> expansionResources = new ArrayList<>();
            expansionResources.add("lemma");
            expansionResources.add("verb");
            expansionResources.add("noun");

            HashSet<String> usef_words = answer_extraction.extractUsefulWordsWithoutEntityWords(question, entity_URI.keySet());

            if (usef_words.isEmpty() && question.toLowerCase().startsWith("what is")) {
                question_type = "definition";
            }
            obj.put("question_type", question_type);

            // Store the useful words of the question
            Set<String> useful_words = answer_extraction.extractUsefulWords(question, question_type, entity_URI.keySet(), expansionResources);

            answer_extraction.setUsefulWords(useful_words);

            String fact = answer_extraction.extractFact(useful_words);

            answer_extraction.retrieveCandidateTriplesOptimized(question_type, entity_URI, fact, useful_words.size(), "min");

            JSONObject a_eErrorHandling = ModulesErrorHandling.answerExtractionErrorHandling(answer_extraction, question_type, entity_URI);

            if (a_eErrorHandling.getString("status").equalsIgnoreCase("error")) {
                return constructErrorJson(obj, a_eErrorHandling, "answerExtraction");
            }

            obj.put("useful_words", useful_words);

            JSONObject answer_triple = answer_extraction.extractAnswer(useful_words, fact, entity_URI, question_type);

            Logger.getLogger(ExternalKnowledgeDemoMain.class.getName()).log(Level.INFO, "===== Answer: {0}", answer_triple);

            String answer = answer_triple.getString("answer");
            obj.put("plain_answer", AnswerExtraction.getSuffixOfURI(answer));
            obj.put("answer", answer_triple.get("answer"));
            answer_triple.remove("answer");
            obj.put("triple", answer_triple);

            return obj;

        } catch (JSONException ex) {
            Logger.getLogger(ExternalKnowledgeDemoMain.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }

    /**
     * Function for submitting a question in Natural Language and retrieve an
     * answer in JSON format. This function executes the whole QA pipeline, to
     * analyze the input question and retrieve an answer. The JSON contains
     * information like: question type, question entities, answer triple,
     * provenance, confidence score etc.
     *
     * @param query
     * @return
     */
    public static JSONObject getAnswerAsJson(String query) {
        try {
            JSONObject obj = new JSONObject();

            obj.put("source", "external");

            // ==== Question Analysis Step ====
            QuestionAnalysis q_analysis = new QuestionAnalysis();
            q_analysis.analyzeQuestion(query);

            String question = q_analysis.getQuestion();

            obj.put("question", question);

            JSONObject q_aErrorHandling = ModulesErrorHandling.questionAnalysisErrorHandling(q_analysis);

            if (q_aErrorHandling.getString("status").equalsIgnoreCase("error")) {
                return constructErrorJson(obj, q_aErrorHandling, "questionAnalysis");
            }

            String question_type = q_analysis.getQuestionType();

            obj.put("question_type", question_type);

            // ==== Entities Detection Step ====
            EntitiesDetection entities_detection = new EntitiesDetection();
            String NEtool = "both";
            //String NEtool = "scnlp";
            //String NEtool = "dbpedia";
            // identify NamedEntities in the question using SCNLP and Spotlight
            entities_detection.identifyNamedEntities(question, NEtool);

            HashMap<String, String> entity_URI = entities_detection.extractEntitiesWithUris(question, NEtool);

            JSONObject e_dErrorHandling = ModulesErrorHandling.entitiesDetectionErrorHandling(entities_detection);

            if (e_dErrorHandling.getString("status").equalsIgnoreCase("error")) {
                return constructErrorJson(obj, e_dErrorHandling, "entitiesDetection");
            }

            obj.put("question_entities", entity_URI.keySet());
            obj.put("retrievedEntities", entity_URI);

            // ==== Answer Extraction Step ====
            AnswerExtraction answer_extraction = new AnswerExtraction();

            ArrayList<String> expansionResources = new ArrayList<>();
            expansionResources.add("lemma");
            expansionResources.add("verb");
            expansionResources.add("noun");

            // Store the useful words of the question
            Set<String> useful_words = answer_extraction.extractUsefulWords(question, question_type, entity_URI.keySet(), expansionResources);

            answer_extraction.setUsefulWords(useful_words);

            String fact = answer_extraction.extractFact(useful_words);

            answer_extraction.retrieveCandidateTriplesOptimized(question_type, entity_URI, fact, useful_words.size(), "max");

            JSONObject a_eErrorHandling = ModulesErrorHandling.answerExtractionErrorHandling(answer_extraction, question_type, entity_URI);

            if (a_eErrorHandling.getString("status").equalsIgnoreCase("error")) {
                return constructErrorJson(obj, a_eErrorHandling, "answerExtraction");
            }

            obj.put("useful_words", useful_words);

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

    /**
     * This function is responsible to handle a possible error and depending the
     * module the error occurred include as many information as possible in the
     * answer JSON to return to the user.
     *
     *
     * @param current_answer
     * @param error
     * @param module
     * @return
     */
    private static JSONObject constructErrorJson(JSONObject current_answer, JSONObject error, String module) {
        // All the json tags used for the Question Analysis module
        ArrayList<String> qA_tags = new ArrayList<>(Arrays.asList("question_type"));
        // All the json tags used for the Entities Detection module
        ArrayList<String> eD_tags = new ArrayList<>(Arrays.asList("question_entities", "retrievedEntities"));
        // All the json tags used for the Answer Extraction module
        ArrayList<String> aE_tags = new ArrayList<>(Arrays.asList("useful_words", "answer", "plain_answer"));

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
        } else {
            for (String tag : aE_tags) {
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
    }
}
