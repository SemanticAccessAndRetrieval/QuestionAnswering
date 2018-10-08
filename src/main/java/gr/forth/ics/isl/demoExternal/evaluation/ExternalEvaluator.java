/*
 *  This code belongs to the Semantic Access and Retrieval (SAR) group of the 
 *  Information Systems Laboratory (ISL) of the 
 *  Institute of Computer Science (ICS) of the  
 *  Foundation for Research and Technology - Hellas (FORTH)
 *  Nobody is allowed to use, copy, distribute, or modify this work.
 *  It is published for reasons of research results reproducibility.
 *  (c) 2017 Semantic Access and Retrieval group, All rights reserved
 */
package gr.forth.ics.isl.demoExternal.evaluation;

import gr.forth.ics.isl.demoExternal.main.ExternalKnowledgeDemoMain;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 *
 * @author Lefteris Dimitrakis
 */
public class ExternalEvaluator {

    public static void main(String[] args) {

        try {
            ExternalKnowledgeDemoMain.initializeToolsAndResources("WNHOME");

            TreeMap<Integer, String> questionId_question;

            questionId_question = readQuestionsFile("questions");

            TreeMap<Integer, JSONObject> questionId_answer = evaluatePipeline(questionId_question);

            try {
                writeSystemAnswersToFile(questionId_answer, "system_answers");
            } catch (JSONException ex) {
                Logger.getLogger(ExternalEvaluator.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (IOException ex) {
            Logger.getLogger(ExternalEvaluator.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public static TreeMap<Integer, JSONObject> evaluatePipeline(TreeMap<Integer, String> questionId_question) {

        TreeMap<Integer, JSONObject> questionId_systemAnswer = new TreeMap<>();
        JSONObject tmp_answer;

        for (int question_id : questionId_question.keySet()) {
            tmp_answer = ExternalKnowledgeDemoMain.getAnswerAsJson(questionId_question.get(question_id));
            questionId_systemAnswer.put(question_id, tmp_answer);

            // TO REMOVE IN ORDER TO EVALUATE ALL QUESTIONS
            if (question_id == 2) {
                break;
            }
        }
        return questionId_systemAnswer;
    }

    public static void writeSystemAnswersToFile(TreeMap<Integer, JSONObject> questionId_systemAnswer, String filename) throws JSONException {
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new FileWriter("src/main/resources/external/evaluation/" + filename + ".txt", false));
            StringBuilder sb;
            JSONObject system_answer;
            for (int question_id : questionId_systemAnswer.keySet()) {
                sb = new StringBuilder();
                sb.append("q").append(question_id).append("\t");

                system_answer = new JSONObject(questionId_systemAnswer.get(question_id).getString("triple"));
                if (system_answer.length() != 0) {
                    sb.append(system_answer.getString("subject")).append("\t").append(system_answer.getString("predicate")).append("\t").append(system_answer.getString("object")).append("\n");
                }

                bw.write(sb.toString());

            }
        } catch (IOException ex) {
            Logger.getLogger(ExternalEvaluator.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                bw.close();
            } catch (IOException ex) {
                Logger.getLogger(ExternalEvaluator.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public static TreeMap<Integer, String> readQuestionsFile(String filename) throws FileNotFoundException, IOException {

        BufferedReader br = new BufferedReader(new FileReader("src/main/resources/external/evaluation/" + filename + ".txt"));

        TreeMap<Integer, String> questionId_question = new TreeMap<>();
        String[] id_question;
        try {
            String line = br.readLine();
            while (line != null) {

                if (!line.trim().isEmpty()) {
                    id_question = line.split("\t");
                    //keep only the number of the question id e.g. q1 -> 1
                    questionId_question.put(Integer.parseInt(id_question[0].replaceAll("q", "")), id_question[1]);
                }
                line = br.readLine();
            }
        } finally {
            br.close();
        }
        return questionId_question;
    }

}
