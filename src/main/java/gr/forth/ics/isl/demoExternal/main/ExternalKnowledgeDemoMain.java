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
import gr.forth.ics.isl.demoExternal.core.QuestionAnalysis;
import gr.forth.ics.isl.utilities.StringUtils;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
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

        // Question 1 on focus (factoid)
        String query1 = "What is the population of Kyoto?";
        String query4 = "Which is the capital city of Japan?";
        String query5 = "Which is the capital of Japan?";
        String query6 = "What does Nintendo sell?";
        String query7 = "Where is Mitsubishi located?";
        String query8 = "Which is the foundation place of Sony?";
        String query9 = "Which is the death place of Nujabes?";
        String query10 = "Where is Mount Everest located?";
        String query11 = "Where did Nujabes died?";  // not answerable (died does not match with deathPlace)

        // Question 2 on focus (confirmation)
        String query2 = "Is Nintendo located in Kyoto?";
        String query12 = "Is Tokyo the capital of Japan?";
        String query13 = "Is Kyoto the capital of Japan?";

        // Question 3 on focus (definition)
        String query3 = "What does Kyoto mean?";

        // ==== Question Analysis Step ====
        QuestionAnalysis q_analysis = new QuestionAnalysis();
        q_analysis.analyzeQuestion(query8);

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

            if (entity_URI == null) {
                Logger.getLogger(ExternalKnowledgeDemoMain.class.getName()).log(Level.INFO, "===== Answer: {0}", "No answer found!");
            } else {
                // ==== Answer Extraction Step ====
                JSONObject answer = AnswerExtraction.extractAnswer(useful_words, fact, entity_URI, question_type);

                Logger.getLogger(ExternalKnowledgeDemoMain.class.getName()).log(Level.INFO, "===== Answer: {0}", answer);
            }

        }
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

            String question_type = q_analysis.getQuestionType();

            obj.put("question_type", question_type);

            if (question_type.equals("none")) {
                Logger.getLogger(ExternalKnowledgeDemoMain.class.getName()).log(Level.INFO, "===== Answer: {0}", "Unrecognized type of question. No answer found!");
                // TO CHANGE?
                return null;
            } else {
                // Store the useful words of the question
                Set<String> useful_words = q_analysis.getUsefulWords();

                obj.put("useful_words", useful_words);

                // Store the text of the Named Entities
                Set<String> entities = q_analysis.getQuestionEntities();

                String fact = q_analysis.getFact();

                // ==== Entities Detection Step ====
                // Hashmap to store each entity and the selected URI (the highest scored)
                HashMap<String, String> entity_URI = EntitiesDetection.retrieveMatchingURIs(entities);

                obj.put("retrievedEntities", entity_URI);

                if (entity_URI == null) {
                    obj.put("answer", "No answer found!");
                    obj.put("triple", new JSONObject());
                } else {

                // ==== Answer Extraction Step ====
                JSONObject answer_triple = AnswerExtraction.extractAnswer(useful_words, fact, entity_URI, question_type);

                Logger.getLogger(ExternalKnowledgeDemoMain.class.getName()).log(Level.INFO, "===== Answer: {0}", answer_triple);

                obj.put("answer", answer_triple.get("answer"));
                answer_triple.remove("answer");
                    obj.put("triple", answer_triple);
                }
            }

            return obj;

        } catch (JSONException ex) {
            Logger.getLogger(ExternalKnowledgeDemoMain.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }

}
