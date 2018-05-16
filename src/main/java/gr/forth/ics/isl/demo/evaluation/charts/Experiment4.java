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

import gr.forth.ics.isl.demo.evaluation.models.ModelStats;
import gr.forth.ics.isl.utilities.Utils;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author Sgo
 */


public class Experiment4 {

    public static void main(String[] args) throws IOException, FileNotFoundException, FileNotFoundException, ClassNotFoundException {
        String collectionName = "FRUCE_v2";
        //String collectionName = "BookingEvalCollection";
        //String collectionName = "webAP";

        ModelStats Word2vecWordnet = new ModelStats("Word2vec and Wordnet");
        HashMap<String, ArrayList<Integer>> allQueriesTestSet = (HashMap<String, ArrayList<Integer>>) Utils.getSavedObject("results_" + Word2vecWordnet.getDescription() + "_" + collectionName);

        for (String qID : allQueriesTestSet.keySet()) {
            System.out.println(qID + ": " + allQueriesTestSet.get(qID));
        }

    }

}
