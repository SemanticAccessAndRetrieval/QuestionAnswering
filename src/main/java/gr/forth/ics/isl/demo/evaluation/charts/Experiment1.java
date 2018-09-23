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

import static gr.forth.ics.isl.demo.evaluation.EvalCollectionManipulator.readEvaluationSet;
import gr.forth.ics.isl.demo.evaluation.models.EvaluationPair;
import gr.forth.ics.isl.demo.evaluation.models.ModelStats;
import gr.forth.ics.isl.utilities.Utils;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;

/**
 *
 * @author Sgo
 */


public class Experiment1 {

    public static void main(String[] args) throws IOException, FileNotFoundException, ClassNotFoundException {

        //String collectionName = "FRUCE_v2";
        //String collectionName = "BookingEvalCollection";
        String collectionName = "webAP";

        int relThreshold = 0;
        int R = 10;

        if (collectionName.contains("FRUCE")) {
            relThreshold = 0;
        } else if (collectionName.contains("webAP")) {
            relThreshold = 3;
        } else {
            relThreshold = 0;
        }

        HashMap<String, HashMap<String, EvaluationPair>> gt = readEvaluationSet(collectionName + ".csv");

        ModelStats baseline = new ModelStats("Baseline model (Jaccard Similarity)");
        baseline.evaluate2(gt, relThreshold, collectionName);
        baseline.getAllMetricsBoundedR(1, R, gt, relThreshold, collectionName);
        Utils.saveObject(baseline.getAllPrecisions_R(), baseline.getDescription() + "_all_Precisions_R_" + collectionName);
        Utils.saveObject(baseline.getAllAveps_R(), baseline.getDescription() + "_all_Aveps_R_" + collectionName);
        Utils.saveObject(baseline.getAllBprefs_R(), baseline.getDescription() + "_all_Bprefs_R_" + collectionName);
        Utils.saveObject(baseline.getAllnDCSs_R(), baseline.getDescription() + "_all_nDCGs_R_" + collectionName);
        System.out.println(baseline + "\n");

        ModelStats Wordnet = new ModelStats("Wordnet model");
        Wordnet.evaluate2(gt, relThreshold, collectionName);
        Wordnet.getAllMetricsBoundedR(1, R, gt, relThreshold, collectionName);
        Utils.saveObject(Wordnet.getAllPrecisions_R(), Wordnet.getDescription() + "_all_Precisions_R_" + collectionName);
        Utils.saveObject(Wordnet.getAllAveps_R(), Wordnet.getDescription() + "_all_Aveps_R_" + collectionName);
        Utils.saveObject(Wordnet.getAllBprefs_R(), Wordnet.getDescription() + "_all_Bprefs_R_" + collectionName);
        Utils.saveObject(Wordnet.getAllnDCSs_R(), Wordnet.getDescription() + "_all_nDCGs_R_" + collectionName);
        System.out.println(Wordnet + "\n");

//        ModelStats Wordnet_II = new ModelStats("Wordnet model II");
//        Wordnet_II.evaluate2(gt, relThreshold, collectionName);
//        Wordnet_II.getAllMetricsBoundedR(1, R, gt, relThreshold, collectionName);
//        Utils.saveObject(Wordnet_II.getAllPrecisions_R(), Wordnet_II.getDescription() + "_all_Precisions_R_" + collectionName);
//        Utils.saveObject(Wordnet_II.getAllAveps_R(), Wordnet_II.getDescription() + "_all_Aveps_R_" + collectionName);
//        Utils.saveObject(Wordnet_II.getAllBprefs_R(), Wordnet_II.getDescription() + "_all_Bprefs_R_" + collectionName);
//        Utils.saveObject(Wordnet_II.getAllnDCSs_R(), Wordnet_II.getDescription() + "_all_nDCGs_R_" + collectionName);
//        System.out.println(Wordnet_II + "\n");

        ModelStats Word2vec = new ModelStats("Word2vec model");
        Word2vec.evaluate2(gt, relThreshold, collectionName);
        Word2vec.getAllMetricsBoundedR(1, R, gt, relThreshold, collectionName);
        Utils.saveObject(Word2vec.getAllPrecisions_R(), Word2vec.getDescription() + "_all_Precisions_R_" + collectionName);
        Utils.saveObject(Word2vec.getAllAveps_R(), Word2vec.getDescription() + "_all_Aveps_R_" + collectionName);
        Utils.saveObject(Word2vec.getAllBprefs_R(), Word2vec.getDescription() + "_all_Bprefs_R_" + collectionName);
        Utils.saveObject(Word2vec.getAllnDCSs_R(), Word2vec.getDescription() + "_all_nDCGs_R_" + collectionName);
        System.out.println(Word2vec + "\n");

        ModelStats Word2vec_CW = new ModelStats("Word2vec model Context Words");
        Word2vec_CW.evaluate2(gt, relThreshold, collectionName);
        Word2vec_CW.getAllMetricsBoundedR(1, R, gt, relThreshold, collectionName);
        Utils.saveObject(Word2vec_CW.getAllPrecisions_R(), Word2vec_CW.getDescription() + "_all_Precisions_R_" + collectionName);
        Utils.saveObject(Word2vec_CW.getAllAveps_R(), Word2vec_CW.getDescription() + "_all_Aveps_R_" + collectionName);
        Utils.saveObject(Word2vec_CW.getAllBprefs_R(), Word2vec_CW.getDescription() + "_all_Bprefs_R_" + collectionName);
        Utils.saveObject(Word2vec_CW.getAllnDCSs_R(), Word2vec_CW.getDescription() + "_all_nDCGs_R_" + collectionName);
        System.out.println(Word2vec_CW + "\n");

//        ModelStats Word2vec_II = new ModelStats("Word2vec model II");
//        Word2vec_II.evaluate2(gt, relThreshold, collectionName);
//        Word2vec_II.getAllMetricsBoundedR(1, R, gt, relThreshold, collectionName);
//        Utils.saveObject(Word2vec_II.getAllPrecisions_R(), Word2vec_II.getDescription() + "_all_Precisions_R_" + collectionName);
//        Utils.saveObject(Word2vec_II.getAllAveps_R(), Word2vec_II.getDescription() + "_all_Aveps_R_" + collectionName);
//        Utils.saveObject(Word2vec_II.getAllBprefs_R(), Word2vec_II.getDescription() + "_all_Bprefs_R_" + collectionName);
//        Utils.saveObject(Word2vec_II.getAllnDCSs_R(), Word2vec_II.getDescription() + "_all_nDCGs_R_" + collectionName);
//        System.out.println(Word2vec_II + "\n");
        ModelStats Word2vec_III = new ModelStats("Word2vec model III");
        Word2vec_III.evaluate2(gt, relThreshold, collectionName);
        Word2vec_III.getAllMetricsBoundedR(1, R, gt, relThreshold, collectionName);
        Utils.saveObject(Word2vec_III.getAllPrecisions_R(), Word2vec_III.getDescription() + "_all_Precisions_R_" + collectionName);
        Utils.saveObject(Word2vec_III.getAllAveps_R(), Word2vec_III.getDescription() + "_all_Aveps_R_" + collectionName);
        Utils.saveObject(Word2vec_III.getAllBprefs_R(), Word2vec_III.getDescription() + "_all_Bprefs_R_" + collectionName);
        Utils.saveObject(Word2vec_III.getAllnDCSs_R(), Word2vec_III.getDescription() + "_all_nDCGs_R_" + collectionName);
        System.out.println(Word2vec_III + "\n");

        ModelStats Word2vec_III_CW = new ModelStats("Word2vec model III Context Words");
        Word2vec_III_CW.evaluate2(gt, relThreshold, collectionName);
        Word2vec_III_CW.getAllMetricsBoundedR(1, R, gt, relThreshold, collectionName);
        Utils.saveObject(Word2vec_III_CW.getAllPrecisions_R(), Word2vec_III_CW.getDescription() + "_all_Precisions_R_" + collectionName);
        Utils.saveObject(Word2vec_III_CW.getAllAveps_R(), Word2vec_III_CW.getDescription() + "_all_Aveps_R_" + collectionName);
        Utils.saveObject(Word2vec_III_CW.getAllBprefs_R(), Word2vec_III_CW.getDescription() + "_all_Bprefs_R_" + collectionName);
        Utils.saveObject(Word2vec_III_CW.getAllnDCSs_R(), Word2vec_III_CW.getDescription() + "_all_nDCGs_R_" + collectionName);
        System.out.println(Word2vec_III_CW + "\n");

        ModelStats Word2vecWordnet = new ModelStats("Word2vec and Wordnet");
        Word2vecWordnet.evaluate2(gt, relThreshold, collectionName);
        Word2vecWordnet.getAllMetricsBoundedR(1, R, gt, relThreshold, collectionName);
        Utils.saveObject(Word2vecWordnet.getAllPrecisions_R(), Word2vecWordnet.getDescription() + "_all_Precisions_R_" + collectionName);
        Utils.saveObject(Word2vecWordnet.getAllAveps_R(), Word2vecWordnet.getDescription() + "_all_Aveps_R_" + collectionName);
        Utils.saveObject(Word2vecWordnet.getAllBprefs_R(), Word2vecWordnet.getDescription() + "_all_Bprefs_R_" + collectionName);
        Utils.saveObject(Word2vecWordnet.getAllnDCSs_R(), Word2vecWordnet.getDescription() + "_all_nDCGs_R_" + collectionName);
        System.out.println(Word2vecWordnet + "\n");

        ModelStats Word2vecWordnet_CW = new ModelStats("Word2vec and Wordnet Context Words");
        Word2vecWordnet_CW.evaluate2(gt, relThreshold, collectionName);
        Word2vecWordnet_CW.getAllMetricsBoundedR(1, R, gt, relThreshold, collectionName);
        Utils.saveObject(Word2vecWordnet_CW.getAllPrecisions_R(), Word2vecWordnet_CW.getDescription() + "_all_Precisions_R_" + collectionName);
        Utils.saveObject(Word2vecWordnet_CW.getAllAveps_R(), Word2vecWordnet_CW.getDescription() + "_all_Aveps_R_" + collectionName);
        Utils.saveObject(Word2vecWordnet_CW.getAllBprefs_R(), Word2vecWordnet_CW.getDescription() + "_all_Bprefs_R_" + collectionName);
        Utils.saveObject(Word2vecWordnet_CW.getAllnDCSs_R(), Word2vecWordnet_CW.getDescription() + "_all_nDCGs_R_" + collectionName);
        System.out.println(Word2vecWordnet_CW + "\n");

        ModelStats Word2vecWordnet_III = new ModelStats("Word2vec and Wordnet III");
        Word2vecWordnet_III.evaluate2(gt, relThreshold, collectionName);
        Word2vecWordnet_III.getAllMetricsBoundedR(1, R, gt, relThreshold, collectionName);
        Utils.saveObject(Word2vecWordnet_III.getAllPrecisions_R(), Word2vecWordnet_III.getDescription() + "_all_Precisions_R_" + collectionName);
        Utils.saveObject(Word2vecWordnet_III.getAllAveps_R(), Word2vecWordnet_III.getDescription() + "_all_Aveps_R_" + collectionName);
        Utils.saveObject(Word2vecWordnet_III.getAllBprefs_R(), Word2vecWordnet_III.getDescription() + "_all_Bprefs_R_" + collectionName);
        Utils.saveObject(Word2vecWordnet_III.getAllnDCSs_R(), Word2vecWordnet_III.getDescription() + "_all_nDCGs_R_" + collectionName);
        System.out.println(Word2vecWordnet_III + "\n");

        ModelStats Word2vecWordnet_III_CW = new ModelStats("Word2vec and Wordnet III Context Words");
        Word2vecWordnet_III_CW.evaluate2(gt, relThreshold, collectionName);
        Word2vecWordnet_III_CW.getAllMetricsBoundedR(1, R, gt, relThreshold, collectionName);
        Utils.saveObject(Word2vecWordnet_III_CW.getAllPrecisions_R(), Word2vecWordnet_III_CW.getDescription() + "_all_Precisions_R_" + collectionName);
        Utils.saveObject(Word2vecWordnet_III_CW.getAllAveps_R(), Word2vecWordnet_III_CW.getDescription() + "_all_Aveps_R_" + collectionName);
        Utils.saveObject(Word2vecWordnet_III_CW.getAllBprefs_R(), Word2vecWordnet_III_CW.getDescription() + "_all_Bprefs_R_" + collectionName);
        Utils.saveObject(Word2vecWordnet_III_CW.getAllnDCSs_R(), Word2vecWordnet_III_CW.getDescription() + "_all_nDCGs_R_" + collectionName);
        System.out.println(Word2vecWordnet_III_CW + "\n");

//        ModelStats Word2vecWordnet_II = new ModelStats("Word2vec and Wordnet II");
//        Word2vecWordnet_II.evaluate2(gt, relThreshold, collectionName);
//        Word2vecWordnet_II.getAllMetricsBoundedR(1, 10, gt, relThreshold, collectionName);
//        Utils.saveObject(Word2vecWordnet_II.getAllPrecisions_R(), Word2vecWordnet_II.getDescription() + "_all_Precisions_R_" + collectionName);
//        Utils.saveObject(Word2vecWordnet_II.getAllAveps_R(), Word2vecWordnet_II.getDescription() + "_all_Aveps_R_" + collectionName);
//        Utils.saveObject(Word2vecWordnet_II.getAllBprefs_R(), Word2vecWordnet_II.getDescription() + "_all_Bprefs_R_" + collectionName);
//        Utils.saveObject(Word2vecWordnet_II.getAllnDCSs_R(), Word2vecWordnet_II.getDescription() + "_all_nDCGs_R_" + collectionName);
//        System.out.println(Word2vecWordnet_II + "\n");


    }
}
