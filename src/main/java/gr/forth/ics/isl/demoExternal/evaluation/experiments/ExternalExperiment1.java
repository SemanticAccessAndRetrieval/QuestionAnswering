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

import edu.stanford.nlp.util.Sets;
import gr.forth.ics.isl.demoExternal.evaluation.ExternalEvaluationMetrics;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Sgo
 */


public class ExternalExperiment1 {

    private static final String fileName = "overall_evaluation";
    private static final String fileExtension = ".txt";
    private static final String evalDirName = "combination";
    //private static final String evalDirName = "scnlp";
    //private static final String evalDirName = "spotlight";
    //private static final String evalDirName = "wo";
    //private static final String evalDirName = "woLemma";
    //private static final String evalDirName = "woNoun";
    //private static final String evalDirName = "woVerb";

    private static final String filePath = "src/main/resources/external/evaluation/" + evalDirName + "/";

    public static Integer sumUpTruePositives(Set<String> systemAns, Set<String> goldAns) {
        return Sets.intersection(systemAns, goldAns).size();
    }

    public static Integer sumUpFalsePositives(Set<String> systemAns, Set<String> goldAns) {
        systemAns.removeAll(goldAns);
        return systemAns.size();
    }

    public static Integer sumUpFalseNegatives(Set<String> systemAns, Set<String> goldAns) {
        goldAns.removeAll(systemAns);
        return goldAns.size();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        BufferedReader reader = null;

        //key:qId, value: [truePos, falsePos, falseNeg]
        HashMap<String, Integer[]> results = new HashMap<>();

        try {
            File file = new File(filePath + fileName + fileExtension);
            reader = new BufferedReader(new FileReader(file));

            int cnt = 0;
            String line;
            while ((line = reader.readLine()) != null) {

                String[] tuple = line.split("\t");
                String qId = tuple[0];
                System.out.println(qId);
                Set<String> systemAns = new HashSet<String>(Arrays.asList(tuple[2].split(",")));
                System.out.println(systemAns);
                Set<String> goldAns = new HashSet<String>(Arrays.asList(tuple[3].substring(1, tuple[3].length() - 1).split(",")));
                System.out.println(goldAns);
                System.out.println("");
                Set<String> systemAnsMapped = new HashSet<String>();

                for (String ans : systemAns) {
                    if (ans.equals("Yes!")) {
                        systemAnsMapped.add("true");
                    } else if (ans.equals("No!")) {
                        systemAnsMapped.add("false");
                    } else {
                        systemAnsMapped.add(ans);
                    }
                }
//                System.out.println("SA: " + systemAns);
//                System.out.println("GA: " + goldAns);

                Integer truePos = sumUpTruePositives(systemAnsMapped, goldAns);
                Integer falsePos = sumUpFalsePositives(systemAnsMapped, goldAns);
                Integer falseNeg = sumUpFalseNegatives(systemAnsMapped, goldAns);
                Integer[] stats = {truePos, falsePos, falseNeg};
                results.put(qId + cnt, stats);
                cnt++;

            }

            System.out.println("== MICRO ==");
            System.out.println("micro Precision: " + ExternalEvaluationMetrics.microPrecision(results));
            System.out.println("micro Recall: " + ExternalEvaluationMetrics.microRecall(results));
            System.out.println("micro F1-measure: " + ExternalEvaluationMetrics.microF1_Measure(results));

            System.out.println("");

            System.out.println("== MACRO ==");
            System.out.println("macro Precision: " + ExternalEvaluationMetrics.macroPrecision(results));
            System.out.println("macro Recall: " + ExternalEvaluationMetrics.macroRecall(results));
            System.out.println("macro F1-measure: " + ExternalEvaluationMetrics.macroF1_Measure(results));

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
