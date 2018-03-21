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

import gr.forth.ics.isl.evaluation.models.ModelHyperparameters;
import static gr.forth.ics.isl.utilities.Utils.getSavedObject;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

/**
 *
 * @author Sgo
 */


public class EvaluationMetrics {

    public static double R_Precision(ArrayList<Integer> answerRels, ArrayList<Integer> groundTruth, int R) {

        int truePred = 0;

        for (int i = 0; i < Math.min(answerRels.size(), R); i++) {
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

    public static double BPREF(ArrayList<Integer> answerRels, ArrayList<Integer> groundTruth, int R) {
        double Sum = 0.0;
        int nonRels = 0;

        for (int i = 0; i < Math.min(answerRels.size(), R); i++) {
            if (Objects.equals(answerRels.get(i), groundTruth.get(i))) {
                Sum += 1.0 - ((double) nonRels) / R;
            } else {
                nonRels++;
            }
        }

        return Sum / R;
    }

    public static void main(String[] args) throws IOException, FileNotFoundException, ClassNotFoundException {
        ModelHyperparameters m1 = (ModelHyperparameters) getSavedObject("AVEPbased_BestModel");
        ModelHyperparameters m2 = (ModelHyperparameters) getSavedObject("rPrecisionbased_BestModel");

        System.out.println("======AVEP=======");
        System.out.println(m1);
        System.out.println("===R_Precision===");
        System.out.println(m2);

        ArrayList<Integer> answerRels = new ArrayList<>();
        answerRels.add(1);
        answerRels.add(1);
        answerRels.add(1);
        answerRels.add(1);
        answerRels.add(1);
        answerRels.add(1);
        answerRels.add(1);
        answerRels.add(1);
        answerRels.add(1);
        answerRels.add(1);
        ArrayList<Integer> groundTruth = new ArrayList<>();
        groundTruth.add(1);
        groundTruth.add(1);
        groundTruth.add(0);
        groundTruth.add(1);
        groundTruth.add(1);
        groundTruth.add(0);
        groundTruth.add(0);
        groundTruth.add(1);
        groundTruth.add(1);
        groundTruth.add(0);

        System.out.println(R_Precision(answerRels, groundTruth, 10));
        System.out.println(AVEP(answerRels, groundTruth, 10));
    }
}
