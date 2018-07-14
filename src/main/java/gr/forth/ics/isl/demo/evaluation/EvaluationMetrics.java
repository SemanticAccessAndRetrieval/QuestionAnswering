/*
 *  This code belongs to the Semantic Access and Retrieval (SAR) group of the
 *  Information Systems Laboratory (ISL) of the
 *  Institute of Computer Science (ICS) of the
 *  Foundation for Research and Technology - Hellas (FORTH)
 *  Nobody is allowed to use, copy, distribute, or modify this work.
 *  It is published for reasons of research results reproducibility.
 *  (c) 2017 Semantic Access and Retrieval group, All rights reserved
 */
package gr.forth.ics.isl.demo.evaluation;

import gr.forth.ics.isl.demo.evaluation.models.EvaluationPair;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.TreeMap;

/**
 *
 * @author Sgo
 */


public class EvaluationMetrics {

    public static double R_Precision(ArrayList<Integer> groundTruth, int R, int relThreshold) {

        int truePred = 0;

        for (int i = 0; i < R; i++) {
            if (groundTruth.get(i) > relThreshold) {
                truePred++;
            }
        }
        return (double) truePred / R;
    }

    public static double Recall(ArrayList<Integer> groundTruth, int R, int numOfRels, int relThreshold) {

        int truePred = 0;

        for (int i = 0; i < R; i++) {
            if (groundTruth.get(i) > relThreshold) {
                truePred++;
            }
        }
        return (double) truePred / numOfRels;
    }

    public static double Fallout(ArrayList<Integer> groundTruth, int R, int numOfNonRels, int relThreshold) {

        if (numOfNonRels == 0) {
            return 0.0;
        }

        int falsePred = 0;

        for (int i = 0; i < R; i++) {
            if (groundTruth.get(i) <= relThreshold) {
                falsePred++;
            }
        }
        return (double) falsePred / numOfNonRels;
    }

    public static double reciprocalRank(ArrayList<Integer> groundTruth, int R, int relThreshold) {

        double recipRank = 0.0;

        for (int i = 0; i < R; i++) {
            if (groundTruth.get(i) > relThreshold) {
                recipRank = (double) 1 / (i + 1);
                return recipRank;
            }
        }

        return recipRank;
    }

    public static double AVEP(ArrayList<Integer> groundTruth, int R, int relThreshold) {
        double Sum = 0.0;
        int rels = 0;

        for (int i = 0; i < R; i++) {
            if (groundTruth.get(i) > relThreshold) {
                rels++;
                Sum += ((double) rels) / (i + 1);
            }
        }

        return Sum / R;
    }

    public static double BPREF(ArrayList<Integer> groundTruth, int R, int N, int relThreshold) {
        double Sum = 0.0;
        int nonRels = 0;

        for (int i = 0; i < R; i++) {
            if (groundTruth.get(i) > relThreshold) {
                Sum += 1.0 - ((double) nonRels) / Math.min(R, N);
            } else {
                nonRels++;
            }
        }

        return Sum / R;
    }

    public static double nDCG(ArrayList<Integer> groundTruth, double idealScore) {
        double Sum = 0.0;

        int i = 1;
        for (int rel : groundTruth) {
            Sum += (Math.pow(2.0, (double) rel) - 1) / (Math.log(i + 1) / Math.log(2));
            i++;
        }

        return Sum / idealScore;
    }

    public static double getIDCG(Collection<EvaluationPair> evalPairs) {
        TreeMap<Integer, Integer> relevance_score = new TreeMap<>();

        int tmp_relevance;
        for (EvaluationPair p : evalPairs) {
            tmp_relevance = p.getRelevance();

            if (tmp_relevance != 0) {
                if (!relevance_score.containsKey(tmp_relevance)) {
                    relevance_score.put(tmp_relevance, 1);
                } else {
                    relevance_score.replace(tmp_relevance, relevance_score.get(tmp_relevance) + 1);
                }
            }
        }

        double ideal_score = 0.0f;
        int position = 1;

        for (int rel : relevance_score.descendingKeySet()) {
            int num_of_comments = relevance_score.get(rel);
            for (int i = position; i < (position + num_of_comments); i++) {
                ideal_score += (Math.pow(2.0, (double) rel) - 1) / (Math.log(i + 1) / Math.log(2));
            }
            position += num_of_comments;
        }
        return ideal_score;
    }

    public static double nDCG_R(ArrayList<Integer> groundTruth, double idealScore, int R) {
        double Sum = 0.0;

        int i = 1;
        for (int rel : groundTruth) {
            Sum += (Math.pow(2.0, (double) rel) - 1) / (Math.log(i + 1) / Math.log(2));
            if (i == R) {
                break;
            }
            i++;
        }

        if (idealScore == 0.0f) {
            return 0.0f;
        }

        return Sum / idealScore;
    }

    public static double getIDCG_R(Collection<EvaluationPair> evalPairs, int R) {

        TreeMap<Integer, Integer> relevance_score = new TreeMap<>();

        int tmp_relevance;
        for (EvaluationPair p : evalPairs) {
            tmp_relevance = p.getRelevance();

            if (tmp_relevance != 0) {
                if (!relevance_score.containsKey(tmp_relevance)) {
                    relevance_score.put(tmp_relevance, 1);
                } else {
                    relevance_score.replace(tmp_relevance, relevance_score.get(tmp_relevance) + 1);
                }
            }
        }

        double ideal_score = 0.0f;
        int position = 1;
        int pos_count = 1;

        for (int rel : relevance_score.descendingKeySet()) {
            int num_of_comments = relevance_score.get(rel);
            for (int i = position; i < (position + num_of_comments); i++) {
                ideal_score += (Math.pow(2.0, (double) rel) - 1) / (Math.log(i + 1) / Math.log(2));

                if (pos_count == R) {
                    return ideal_score;
                }
                pos_count++;
            }
            position += num_of_comments;
        }
        return ideal_score;
    }

    public static void main(String[] args) throws IOException, FileNotFoundException, ClassNotFoundException {
        //ModelHyperparameters m1 = (ModelHyperparameters) getSavedObject("AVEPbased_BestModel");
        //ModelHyperparameters m2 = (ModelHyperparameters) getSavedObject("rPrecisionbased_BestModel");

        //System.out.println("======AVEP=======");
        //System.out.println(m1);
        //System.out.println("===R_Precision===");
        //System.out.println(m2);
        ArrayList<Integer> groundTruth = new ArrayList<>();
        groundTruth.add(0);
        groundTruth.add(0);
        groundTruth.add(0);
        groundTruth.add(0);
        groundTruth.add(0);
        groundTruth.add(0);
        groundTruth.add(0);
        groundTruth.add(1);
        groundTruth.add(1);
        groundTruth.add(0);

        System.out.println(R_Precision(groundTruth, 6, 1));
        System.out.println(AVEP(groundTruth, 6, 1));

        ArrayList<Integer> groundTruthMVRG = new ArrayList<>();
        groundTruthMVRG.add(4);
        groundTruthMVRG.add(3);
        groundTruthMVRG.add(3);
        groundTruthMVRG.add(1);
        groundTruthMVRG.add(3);
        groundTruthMVRG.add(2);
        groundTruthMVRG.add(2);
        groundTruthMVRG.add(1);
        groundTruthMVRG.add(1);
        groundTruthMVRG.add(1);

        System.out.println(R_Precision(groundTruthMVRG, 4, 3));
        System.out.println(AVEP(groundTruthMVRG, 4, 3));

    }
}
