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

import gr.forth.ics.isl.demo.evaluation.EvalCollectionManipulator;
import gr.forth.ics.isl.demo.evaluation.EvaluationMetrics;
import static gr.forth.ics.isl.demo.evaluation.WebAPTestSuit.getQueryList;
import gr.forth.ics.isl.demo.evaluation.models.EvaluationPair;
import gr.forth.ics.isl.demo.evaluation.models.EvaluationResult;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 *
 * @author Sgo
 */
public class ExperimentWebAP {

    public static HashMap<String, String> getQueryCharPais(String evalFile) throws IOException {

        HashMap<String, String> queryCharsPairsGT = new HashMap<>();

        // ground truth relevance between each query-passage pair
        HashMap<String, HashMap<String, EvaluationPair>> gt = EvalCollectionManipulator.readEvaluationSetExternal(evalFile);
        HashMap<String, String> queryList = getQueryList(gt); // retrieve list of queries

        for (String queryID : queryList.keySet()) {
            for (String pairID : gt.get(queryID).keySet()) {
                if (gt.get(queryID).get(pairID).equals("null")) {
                    continue;
                }
                EvaluationPair crntPair = gt.get(queryID).get(pairID);
                if (queryCharsPairsGT.containsKey(queryID)) {
                    String tmpPassageChars = queryCharsPairsGT.get(queryID);
                    tmpPassageChars += crntPair.getComment().getText();
                    queryCharsPairsGT.put(queryID, tmpPassageChars);
                } else {
                    queryCharsPairsGT.put(queryID, queryCharsPairsGT.get(queryID));
                }
            }
            String cleanPassageChars = queryCharsPairsGT.get(queryID);
            if (cleanPassageChars == null) {
                cleanPassageChars = "";
            } else {
                cleanPassageChars = cleanPassageChars.replaceAll("null", "");
                cleanPassageChars = cleanPassageChars.replaceAll(" ", "");
            }
            queryCharsPairsGT.put(queryID, cleanPassageChars);
            //System.out.println(queryID + " - " + queryCharsPairsGT.get(queryID));
        }

        return queryCharsPairsGT;
    }

    public static String removeDuplicateChars(String string) {
        char[] chars = string.toCharArray();
        Set<Character> charSet = new LinkedHashSet<Character>();
        for (char c : chars) {
            charSet.add(c);
        }

        StringBuilder sb = new StringBuilder();
        for (Character character : charSet) {
            sb.append(character);
        }
        return sb.toString();
    }

    public static double charPrecision(HashMap<String, EvaluationPair> crntQueryPairs, ArrayList<EvaluationResult> crntQueryResults, int threshold, int topPassages) {

        int cnt = 0;
        double crntResultP = 0.0;
        for (EvaluationResult crntQueryResult : crntQueryResults) {
            if (cnt < topPassages) {

                for (EvaluationPair ep : crntQueryPairs.values()) {

                    if (ep.getRelevance() <= threshold) {
                        continue;
                    }
                    if (ep.getComment().getText().contains(crntQueryResult.getPassageText())) {
                        String passageString = removeDuplicateChars(ep.getComment().getText().replaceAll(" ", ""));
                        String resultStrng = removeDuplicateChars(crntQueryResult.getPassageText().replaceAll(" ", ""));

                        crntResultP = ((double) resultStrng.length() / passageString.length());

                        return crntResultP;

                    }
                }
                cnt++;
            }
        }
        return 0.0;

    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        String folderPath = "src/main/resources/evaluation/results/WebAPHQ/";
        String resultPath = "src/main/resources/evaluation/metrics/WebAP/train/";

        //HashMap<String, String> queryCharsPairsGT = getQueryCharPais("webAP.csv");
        //HashMap<String, String> queryCharsPairs = new HashMap<String, String>();
        HashMap<String, HashMap<String, EvaluationPair>> gt = EvalCollectionManipulator.readEvaluationSetExternal("webAP.csv");
        //HashMap<String, HashMap<String, EvaluationPair>> gt = EvalCollectionManipulator.readEvaluationSet("webAP.csv");

        File folder = new File(folderPath);
        File[] listOfModels = folder.listFiles();

        for (int i = 0; i < listOfModels.length; i++) {
            System.out.println(listOfModels.length);
            //System.out.println(listOfModels[i].getPath());
            File model = new File(listOfModels[i].getPath());
            File[] listOfQueries = model.listFiles();

            for (int threshold = 0; threshold < 4; threshold++) {
                int numOfFails = 0;
                int filteredQueries = 0;
                double Precision_1 = 0.0;
                double Precision_2 = 0.0;
                double R_Precision = 0.0;
                double AVEP = 0.0;
                double BPREF = 0.0;
                double nDCG = 0.0;
                double MRR = 0.0;
                double charPrecision_1 = 0.0;
                //double charPrecision_10 = 0.0;

                for (int j = 0; j < listOfQueries.length; j++) {

                    if (listOfQueries[j].isFile()) {
                        //System.out.println(listOfResultsPerQuery[j].getPath());
                        StringBuilder sb = readFile(listOfQueries[j].getPath());
                        //System.out.println(sb.toString());
                        ArrayList<EvaluationResult> crntQueryResults = extractResultFromString(sb);
                        System.out.println(listOfQueries[j].getName().replaceAll(".tsv", "") + "    " + crntQueryResults.size());
                        ArrayList<Integer> crntQueryResultsToInt = new ArrayList<>();
                        ArrayList<Integer> crntQueryResultsToIntTrueRel = new ArrayList<>();
                        //System.out.println(R);

                        for (EvaluationResult crntQueryResult : crntQueryResults) {

                            //if (crntQueryResult.getBestSentence() == "none") {
                            if (crntQueryResult.getScore() <= 0.0001f) {
                                numOfFails++;

                                crntQueryResultsToIntTrueRel.add(crntQueryResult.getPairRelevance());
                                crntQueryResultsToInt.add(0);
                            } else {
                                crntQueryResultsToIntTrueRel.add(crntQueryResult.getPairRelevance());
                                crntQueryResultsToInt.add(crntQueryResult.getPairRelevance());
                            }
                        }

                        HashMap<String, EvaluationPair> crntQueryPairs = gt.get(listOfQueries[j].getName().replaceAll(".tsv", ""));

                        int R = EvalCollectionManipulator.getNumOfRels(crntQueryPairs, threshold);
                        //int R = getNumOfRels(crntQueryResultsToIntTrueRel, threshold);
                        int N = crntQueryResultsToInt.size() - R;

                        if (R == 0 || N == 0) {
                            filteredQueries++;
                            continue;
                        }

                        //////////////////////////////////////////////////////////////////////////
                        charPrecision_1 += charPrecision(crntQueryPairs, crntQueryResults, threshold, 1);
                        //charPrecision_10 += charPrecision(crntQueryPairs, crntQueryResults, threshold, 10);
                        //////////////////////////////////////////////////////////////////////////
                        if (crntQueryResultsToInt.size() > 1) {
                            Precision_1 += EvaluationMetrics.R_Precision(crntQueryResultsToInt, 1, threshold);
                        }
                        if (crntQueryResultsToInt.size() > 2) {
                            Precision_2 += EvaluationMetrics.R_Precision(crntQueryResultsToInt, 2, threshold);
                        }
                        R_Precision += EvaluationMetrics.R_Precision(crntQueryResultsToInt, R, threshold);
                        AVEP += EvaluationMetrics.AVEP(crntQueryResultsToInt, R, threshold);
                        BPREF += EvaluationMetrics.BPREF(crntQueryResultsToInt, R, N, threshold);
                        nDCG += EvaluationMetrics.nDCG(crntQueryResultsToInt, EvaluationMetrics.getIDCGer(crntQueryResults));
                        MRR += EvaluationMetrics.reciprocalRank(crntQueryResultsToInt, R, threshold);
                        //System.out.println(AVEP);

                    }
                }

                int denominator = listOfQueries.length - filteredQueries;
                Precision_1 /= denominator;
                Precision_2 /= denominator;
                R_Precision /= denominator;
                AVEP /= denominator;
                BPREF /= denominator;
                nDCG /= denominator;
                MRR /= denominator;
                charPrecision_1 /= denominator;
                //charPrecision_10 /= denominator;

                StringBuilder resultToWrite = new StringBuilder();

                resultToWrite.append("Threshold: " + threshold + "\n");

                resultToWrite.append("Model: " + listOfModels[i].getName() + "\n");

                resultToWrite.append("Precision@1: " + Precision_1 + "\n");
                resultToWrite.append("Precision@2: " + Precision_2 + "\n");
                resultToWrite.append("R-Precision: " + R_Precision + "\n");
                resultToWrite.append("Avep: " + AVEP + "\n");
                resultToWrite.append("Bpref: " + BPREF + "\n");
                resultToWrite.append("nDCG: " + nDCG + "\n");
                resultToWrite.append("MRR: " + MRR + "\n");
                resultToWrite.append("charPrecision@1: " + charPrecision_1 + "\n");
                //resultToWrite.append("charPrecision@10: " + charPrecision_10 + "\n");

                resultToWrite.append("\n\n");

                String path = resultPath + listOfModels[i].getName();
                writeToFile(path, resultToWrite);
                System.out.println("threshold: " + threshold);
                System.out.println(listOfModels[i].getName() + " failed to score passage " + numOfFails + " times.");
                System.out.println("num of filterd queries:" + filteredQueries);
                System.out.println("");
            }
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
        int cnt = 0;

        for (String resultString : resultStrings) {

            String[] resultTuple = resultString.split("\t");

            try {
                EvaluationResult crntResult = new EvaluationResult(Long.valueOf(resultTuple[0]), Integer.valueOf(resultTuple[1]), resultTuple[2], Integer.valueOf(resultTuple[3]), resultTuple[4], Integer.valueOf(resultTuple[5]), Double.valueOf(resultTuple[6]), resultTuple[7]);
                results.add(crntResult);
            } catch (ArrayIndexOutOfBoundsException e) {
                EvaluationResult crntResult = new EvaluationResult(Long.valueOf(resultTuple[0]), Integer.valueOf(resultTuple[1]), resultTuple[2], Integer.valueOf(resultTuple[3]), resultTuple[4], Integer.valueOf(resultTuple[5]), Double.valueOf(resultTuple[6]), "none");
                results.add(crntResult);
            }
            //System.out.println(crntResult);
        }

        //System.out.println(cnt);
        return results;
    }
}
