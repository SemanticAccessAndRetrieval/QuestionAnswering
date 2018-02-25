/*
 *  This code belongs to the Semantic Access and Retrieval (SAR) group of the
 *  Information Systems Laboratory (ISL) of the
 *  Institute of Computer Science (ICS) of the
 *  Foundation for Research and Technology - Hellas (FORTH)
 *  Nobody is allowed to use, copy, distribute, or modify this work.
 *  It is published for reasons of research results reproducibility.
 *  (c) 2017 Semantic Access and Retrieval group, All rights reserved
 */
package gr.forth.ics.isl.evaluation;

import java.util.ArrayList;
import java.util.Objects;

/**
 *
 * @author Sgo
 */


public class EvaluationMetrics {

    public static double R_Precision(ArrayList<Integer> answerRels, ArrayList<Integer> groundTruth, int R) {

        int truePred = 0;

        for (int i = 0; i < R; i++) {
            if (Objects.equals(answerRels.get(i), groundTruth.get(i))) {
                truePred++;
            }
        }
        return (double) truePred / R;
    }

    public static double AVEP(ArrayList<Integer> answerRels, ArrayList<Integer> groundTruth, int R) {
        double Sum = 0.0;
        int rels = 0;

        for (int i = 0; i < Math.min(answerRels.size(), R); i++) {
            if (Objects.equals(answerRels.get(i), groundTruth.get(i))) {
                rels++;
                Sum += ((double) rels) / (i + 1);
            }
        }

        return Sum / R;
    }

    public static void main(String[] args) {
        ArrayList<Integer> answer = new ArrayList<>();
        answer.add(1);
        answer.add(1);
        answer.add(0);
        answer.add(1);
        answer.add(0);
        answer.add(1);
        answer.add(0);
        answer.add(0);
        answer.add(0);
        answer.add(0);
        answer.add(0);
        answer.add(0);
        answer.add(1);
        answer.add(0);

        ArrayList<Integer> gt = new ArrayList<>();
        gt.add(1);
        gt.add(1);
        gt.add(1);
        gt.add(1);
        gt.add(1);
        gt.add(1);
        gt.add(0);
        gt.add(0);
        gt.add(0);
        gt.add(0);
        gt.add(0);
        gt.add(0);
        gt.add(0);
        gt.add(0);

        System.out.println(R_Precision(answer, gt, 6));
    }
}
