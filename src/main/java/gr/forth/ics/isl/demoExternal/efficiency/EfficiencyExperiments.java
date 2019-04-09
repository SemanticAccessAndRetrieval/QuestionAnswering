
package gr.forth.ics.isl.demoExternal.efficiency;



import gr.forth.ics.isl.demoExternal.core.AnswerExtraction;
import gr.forth.ics.isl.demoExternal.core.EntitiesDetection;
import gr.forth.ics.isl.demoExternal.core.ModulesErrorHandling;
import gr.forth.ics.isl.demoExternal.core.QuestionAnalysis;
import gr.forth.ics.isl.demoExternal.main.ExternalKnowledgeDemoMain;
import static gr.forth.ics.isl.demoExternal.main.ExternalKnowledgeDemoMain.initializeToolsAndResources;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
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
 * @author micha
 */


public class EfficiencyExperiments {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
         try {
            initializeToolsAndResources("WNHOME");
        } catch (IOException ex) {
            Logger.getLogger(ExternalKnowledgeDemoMain.class.getName()).log(Level.SEVERE, null, ex);
        }
        EfficiencyExperiments efex=new EfficiencyExperiments();
        efex.experimentsForEvaluationCollection("test.csv", "executionTimes.csv");
    }
    
    
    /**
     * Reads a file containing lines, where each line contains a question id and a question in natural language
     * separated with tab
     *
     * @param input the input questions
     * @param output the file that will be created
     */
    
    
    public void experimentsForEvaluationCollection(String input,String output)  throws FileNotFoundException, IOException {
        BufferedReader br = new BufferedReader(new FileReader(input));
        BufferedWriter bw = new BufferedWriter(new FileWriter(output));
        String s;
        bw.write("Query,Question Analysis Time,Entities Detection Time,Query Expansion Time,Candidate Triples Time,Final Answer Time, Whole Time\n");                
        while ((s = br.readLine()) != null) {
            String[] split = s.split("\t");
            if (split.length > 1) {
                String time = this.getExecutionTimeOfAnswer(split[0], split[1], "plain");
                bw.write(time + "\n");
            }  
           // i++;
        }
        br.close();
        bw.close();
    }
        
    
    
    
    /**
     * Function for submitting a question in Natural Language and retrieve an
     * its execution timefunction executes the whole QA pipeline, to
     * analyze the input question and retrieve an answer. The JSON contains
     * information like: question type, question entities, answer triple,
     * provenance, confidence score etc.
     *
     *
     * @param query
     * @param format
     * @return execution time of query
     */
    public  String getExecutionTimeOfAnswer(String id, String query, String format) {
        try {
            JSONObject obj = new JSONObject();

            obj.put("source", "external");
            long startTime = System.currentTimeMillis();
            // ==== Question Analysis Step ====
            QuestionAnalysis q_analysis = new QuestionAnalysis();
            q_analysis.analyzeQuestion(query);

            String question = q_analysis.getQuestion();

            obj.put("question", question);

            JSONObject q_aErrorHandling = ModulesErrorHandling.questionAnalysisErrorHandling(q_analysis);

            if (q_aErrorHandling.getString("status").equalsIgnoreCase("error")) {
                return "error";
            }

            String question_type = q_analysis.getQuestionType();

            long stopTime = System.currentTimeMillis();
            long elapsedTime = stopTime - startTime;
            double QuestionAnalysis = (double) elapsedTime / (1000);

            // ==== Entities Detection Step ====
            EntitiesDetection entities_detection = new EntitiesDetection();
            String NEtool = "both";
            // identify NamedEntities in the question using SCNLP and Spotlight
            startTime = System.currentTimeMillis();
            try{
            entities_detection.identifyNamedEntities(question, NEtool);
            }
            catch (Exception E){
            
            }
            HashMap<String, String> entity_URI = entities_detection.extractEntitiesWithUris(question, NEtool);

            JSONObject e_dErrorHandling = ModulesErrorHandling.entitiesDetectionErrorHandling(entities_detection);

            if (e_dErrorHandling.getString("status").equalsIgnoreCase("error")) {
                obj.put("question_type", question_type);
                return "error";
            }

            obj.put("question_entities", entity_URI.keySet());
            obj.put("retrievedEntities", entity_URI);

            stopTime = System.currentTimeMillis();
            elapsedTime = stopTime - startTime;
            double EntitiesDetection = (double) elapsedTime / (1000);

            // ==== Answer Extraction Step ====
            AnswerExtraction answer_extraction = new AnswerExtraction();
            startTime = System.currentTimeMillis();

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

            stopTime = System.currentTimeMillis();
            elapsedTime = stopTime - startTime;
            double QuestionExpansion = (double) elapsedTime / (1000);

            startTime = System.currentTimeMillis();

            answer_extraction.retrieveCandidateTriplesOptimized(question_type, entity_URI, fact, useful_words.size(), "min");

            JSONObject a_eErrorHandling = ModulesErrorHandling.answerExtractionErrorHandling(answer_extraction, question_type, entity_URI);

            if (a_eErrorHandling.getString("status").equalsIgnoreCase("error")) {
                return "error";
            }

            stopTime = System.currentTimeMillis();
            elapsedTime = stopTime - startTime;
            double LODsyndesis = (double) elapsedTime / (1000);

            startTime = System.currentTimeMillis();
            obj.put("useful_words", useful_words);

            JSONObject answer_triple = answer_extraction.extractAnswer(useful_words, fact, entity_URI, question_type);

            Logger.getLogger(ExternalKnowledgeDemoMain.class.getName()).log(Level.INFO, "===== Answer: {0}", answer_triple);

            String answer = answer_triple.getString("answer");
            obj.put("plain_answer", AnswerExtraction.getSuffixOfURI(answer));
            obj.put("answer", answer_triple.get("answer"));
            answer_triple.remove("answer");
            obj.put("triple", answer_triple);

            stopTime = System.currentTimeMillis();
            elapsedTime = stopTime - startTime;
            double finalAnswer = (double) elapsedTime / (1000);

            double wholeTime = EntitiesDetection + QuestionExpansion + QuestionAnalysis + LODsyndesis + finalAnswer;
            String times = id + "," + QuestionAnalysis + "," + EntitiesDetection + "," + QuestionExpansion + ","
                    + LODsyndesis + "," + finalAnswer + "," + wholeTime;
            return times;

        } catch (JSONException ex) {
            Logger.getLogger(ExternalKnowledgeDemoMain.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }

    
    
    
}
