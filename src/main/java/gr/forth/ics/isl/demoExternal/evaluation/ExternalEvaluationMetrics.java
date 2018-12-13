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

import java.util.HashMap;

/**
 *
 * @author Sgo
 */


public class ExternalEvaluationMetrics {

    public static double microPrecision(HashMap<String, Integer[]> results) {
        Integer truePos = 0;
        Integer falsePos = 0;

        //key:qId, value: [truePos, falsePos, falseNeg]
        for (String qId : results.keySet()) {
            truePos += results.get(qId)[0];
            falsePos += results.get(qId)[1];
        }

        return ((double) truePos / (truePos + falsePos));
    }

    public static double macroPrecision(HashMap<String, Integer[]> results) {
        Integer truePos = 0;
        Integer falsePos = 0;
        double precision = 0;

        //key:qId, value: [truePos, falsePos, falseNeg]
        for (String qId : results.keySet()) {
            truePos = results.get(qId)[0];
            falsePos = results.get(qId)[1];
            precision += ((double) truePos / (truePos + falsePos));
        }
        precision /= results.size();

        return precision;
    }

    public static double microRecall(HashMap<String, Integer[]> results) {
        Integer truePos = 0;
        Integer falseNeg = 0;

        //key:qId, value: [truePos, falsePos, falseNeg]
        for (String qId : results.keySet()) {
            truePos += results.get(qId)[0];
            falseNeg += results.get(qId)[2];
        }

        return ((double) truePos / (truePos + falseNeg));
    }

    public static double macroRecall(HashMap<String, Integer[]> results) {
        Integer truePos = 0;
        Integer falseNeg = 0;
        double recall = 0;

        //key:qId, value: [truePos, falsePos, falseNeg]
        for (String qId : results.keySet()) {
            truePos = results.get(qId)[0];
            falseNeg = results.get(qId)[2];
            recall += ((double) truePos / (truePos + falseNeg));
        }
        recall /= results.size();

        return recall;
    }

    public static double microF1_Measure(HashMap<String, Integer[]> results) {
        return (2 * (microPrecision(results) * microRecall(results)) / (microPrecision(results) + microRecall(results)));
    }

    public static double macroF1_Measure(HashMap<String, Integer[]> results) {
        return (2 * (macroPrecision(results) * macroRecall(results)) / (macroPrecision(results) + macroRecall(results)));
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
    }

}
