/*
 *  This code belongs to the Semantic Access and Retrieval (SAR) group of the
 *  Information Systems Laboratory (ISL) of the
 *  Institute of Computer Science (ICS) of the
 *  Foundation for Research and Technology - Hellas (FORTH)
 *  Nobody is allowed to use, copy, distribute, or modify this work.
 *  It is published for reasons of research results reproducibility.
 *  (c) 2017 Semantic Access and Retrieval group, All rights reserved
 */
package gr.forth.ics.isl.demo.evaluation.charts;

import gr.forth.ics.isl.demo.evaluation.EvaluationMetrics;
import gr.forth.ics.isl.demo.evaluation.models.EvaluationResult;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

/**
 *
 * @author Sgo
 */


public class ExperimentWebAP {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        String folderPath = "src/main/resources/evaluation/results/WebAP/";
        String resultPath = "src/main/resources/evaluation/metrics/WebAP/";

        File folder = new File(folderPath);
        File[] listOfModels = folder.listFiles();

        for (int i = 0; i < listOfModels.length; i++) {
            //System.out.println(listOfModels[i].getPath());
            File model = new File(listOfModels[i].getPath());
            File[] listOfQueries = model.listFiles();

            double Precision_2 = 0.0;
            double R_Precision = 0.0;
            double AVEP = 0.0;
            double BPREF = 0.0;
            double nDCG = 0.0;
            double MRR = 0.0;

            for (int j = 0; j < listOfQueries.length; j++) {
                if (listOfQueries[j].isFile()) {
                    //System.out.println(listOfResultsPerQuery[j].getPath());
                    StringBuilder sb = readFile(listOfQueries[j].getPath());
                    //System.out.println(sb.toString());
                    ArrayList<EvaluationResult> crntQueryResults = extractResultFromString(sb);
                    ArrayList<Integer> crntQueryResultsToInt = new ArrayList<>();
                    //System.out.println(R);

                    for (EvaluationResult crntQueryResult : crntQueryResults) {
                        crntQueryResultsToInt.add(crntQueryResult.getPairRelevance());
                    }

                    int threshold = 0;
                    int R = getNumOfRels(crntQueryResultsToInt, threshold);
                    int N = crntQueryResultsToInt.size() - R;

                    //System.out.println(crntQueryResultsToInt);
                    Precision_2 += EvaluationMetrics.R_Precision(crntQueryResultsToInt, 2, threshold);
                    R_Precision += EvaluationMetrics.R_Precision(crntQueryResultsToInt, R, threshold);
                    AVEP += EvaluationMetrics.AVEP(crntQueryResultsToInt, R, threshold);
                    BPREF += EvaluationMetrics.BPREF(crntQueryResultsToInt, R, N, threshold);
                    nDCG += EvaluationMetrics.nDCG(crntQueryResultsToInt, EvaluationMetrics.getIDCGer(crntQueryResults));
                    MRR += EvaluationMetrics.reciprocalRank(crntQueryResultsToInt, R, threshold);
                    //System.out.println(AVEP);

                }
            }

            Precision_2 /= listOfQueries.length;
            R_Precision /= listOfQueries.length;
            AVEP /= listOfQueries.length;
            BPREF /= listOfQueries.length;
            nDCG /= listOfQueries.length;
            MRR /= listOfQueries.length;

            StringBuilder resultToWrite = new StringBuilder();

            resultToWrite.append("Model: " + listOfModels[i].getName() + "\n");

            resultToWrite.append("Precision@2: " + Precision_2 + "\n");
            resultToWrite.append("R-Precision: " + R_Precision + "\n");
            resultToWrite.append("Avep: " + AVEP + "\n");
            resultToWrite.append("Bpref: " + BPREF + "\n");
            resultToWrite.append("nDCG: " + nDCG + "\n");
            resultToWrite.append("MRR: " + MRR + "\n");

            resultToWrite.append("\n\n");

            String path = resultPath + listOfModels[i].getName();
            writeToFile(path, resultToWrite);

        }
    }

    public static void writeToFile(String path, StringBuilder sb) throws IOException {

        File file = new File(path + ".txt");
        BufferedWriter writer;

        if (file.exists()) {
            writer = new BufferedWriter(new FileWriter(file, true));
        } else {
            writer = new BufferedWriter(new FileWriter(file));
        }

        writer.write(sb.toString());

        writer.close();

    }

    public static int getNumOfRels(ArrayList<Integer> results, int threshold) {
        int R = 0;

        for (int result : results) {

            if (result > threshold) {
                R++;
            }

        }

        return R;
    }

    public static StringBuilder readFile(String path) throws FileNotFoundException, IOException {

        String line;
        BufferedReader br = new BufferedReader(new FileReader(path));
        StringBuilder sb = new StringBuilder();

        while ((line = br.readLine()) != null) {
            sb.append(line + "\n");
        }

        return sb;
    }

    public static ArrayList<EvaluationResult> extractResultFromString(StringBuilder sb) {
        String[] resultStrings = sb.toString().split("\n");
        ArrayList<EvaluationResult> results = new ArrayList<>();

        for (String resultString : resultStrings) {

            String[] resultTuple = resultString.split("\t");
            EvaluationResult crntResult = new EvaluationResult(Long.valueOf(resultTuple[0]), Integer.valueOf(resultTuple[1]), resultTuple[2], Integer.valueOf(resultTuple[3]), resultTuple[4], Integer.valueOf(resultTuple[5]), 0.0, "none");
            results.add(crntResult);
            //System.out.println(crntResult);
        }

        return results;
    }
}
