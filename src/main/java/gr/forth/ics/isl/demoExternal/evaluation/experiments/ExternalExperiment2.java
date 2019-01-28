/*
 *  This code belongs to the Semantic Access and Retrieval (SAR) group of the
 *  Information Systems Laboratory (ISL) of the
 *  Institute of Computer Science (ICS) of the
 *  Foundation for Research and Technology - Hellas (FORTH)
 *  Nobody is allowed to use, copy, distribute, or modify this work.
 *  It is published for reasons of research results reproducibility.
 *  (c) 2017 Semantic Access and Retrieval group, All rights reserved
 */
package gr.forth.ics.isl.demoExternal.evaluation.experiments;

import gr.forth.ics.isl.demo.evaluation.EvaluationMetrics;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author Sgo
 */
public class ExternalExperiment2 {

    private static String evalPath = "src/main/resources/external/evaluation/";
    private static String folderName = "simpleQuestions";

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        HashMap<String, HashMap<String, ArrayList<Integer>>> evalModels = readEvalFolder(evalPath, folderName);

        for (String model : evalModels.keySet()) {
            double microPrecision = 0.0;
            double macroPrecision = 0.0;
            ArrayList<Integer> allAnswers = new ArrayList<>();
            HashMap<String, ArrayList<Integer>> queryAnswerPairs = evalModels.get(model);
            System.out.println("===================================");
            System.out.println("RESULTS for model: " + model);

            for (String qID : queryAnswerPairs.keySet()) {
                // get microPrecision input
                ArrayList<Integer> crntAnswer = queryAnswerPairs.get(qID);
                ArrayList<Integer> crntAnswerClean = new ArrayList<>();
                for (int i = 0; i < crntAnswer.size(); i++) {
                    if (crntAnswer.get(i) == -1) {
                        crntAnswerClean.add(0);
                    } else {
                        crntAnswerClean.add(crntAnswer.get(i));
                    }
                }

                // calcutale Precision for the current answer
                microPrecision += EvaluationMetrics.R_Precision(crntAnswerClean, 1, 0);

                // get macroPrecision input
                allAnswers.addAll(crntAnswerClean);
            }

            // calcutale micro Precision by avg of all precisions for each answer
            microPrecision /= queryAnswerPairs.size();

            // calcutale macro Precision
            macroPrecision = EvaluationMetrics.R_Precision(allAnswers, allAnswers.size(), 0);

            System.out.println("Micro-Precision: " + microPrecision);
            System.out.println("Macro-Precision: " + macroPrecision);
            System.out.println("===================================");
        }
    }

    public static HashMap<String, HashMap<String, ArrayList<Integer>>> readEvalFolder(String path, String folderName) throws IOException {
        HashMap<String, HashMap<String, ArrayList<Integer>>> evalModels = new HashMap<>();

        String folderPath = path + folderName + "/";
        File folder = new File(folderPath);
        File[] listOfFiles = folder.listFiles();

        for (File file : listOfFiles) {
            evalModels.put(file.getName(), readEvalFile(folderPath, file.getName()));
        }

        return evalModels;
    }

    public static HashMap<String, ArrayList<Integer>> readEvalFile(String path, String fileName) throws FileNotFoundException, IOException {

        HashMap<String, ArrayList<Integer>> queryAnswerPairs = new HashMap<>();

        String line;
        BufferedReader br = new BufferedReader(new FileReader(path + fileName));
        StringBuilder sb = new StringBuilder();

        while ((line = br.readLine()) != null) {
            String[] pair = line.split("\t");
            String queryId = pair[0];
            ArrayList<Integer> answerRelevanceList = new ArrayList<>();
            Integer answerRelevance = Integer.valueOf(pair[1]);
            if (answerRelevance == -1) {
                answerRelevance = 0;
            }

            answerRelevanceList.add(answerRelevance);
            queryAnswerPairs.put(queryId, answerRelevanceList);
        }

        return queryAnswerPairs;
    }
}
