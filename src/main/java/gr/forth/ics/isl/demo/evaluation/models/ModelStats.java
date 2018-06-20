/*
 *  This code belongs to the Semantic Access and Retrieval (SAR) group of the
 *  Information Systems Laboratory (ISL) of the
 *  Institute of Computer Science (ICS) of the
 *  Foundation for Research and Technology - Hellas (FORTH)
 *  Nobody is allowed to use, copy, distribute, or modify this work.
 *  It is published for reasons of research results reproducibility.
 *  (c) 2017 Semantic Access and Retrieval group, All rights reserved
 */
package gr.forth.ics.isl.demo.evaluation.models;

import gr.forth.ics.isl.demo.evaluation.EvalCollectionManipulator;
import gr.forth.ics.isl.demo.evaluation.EvaluationMetrics;
import gr.forth.ics.isl.demo.models.Model;
import gr.forth.ics.isl.demo.models.WordnetWord2vecModel;
import gr.forth.ics.isl.nlp.models.Comment;
import gr.forth.ics.isl.utilities.Utils;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author Sgo
 */
public class ModelStats implements Serializable {

    //private Model model;
    private String description;
    private double Precision_2;
    private double Precision_R;
    private double Recall;
    private double Fallout;
    private double Avep;
    private double Bpref;
    private double nDCG;
    private double reciprocalRank;
    ArrayList<Integer> testSet;
    ArrayList<Double> scoreSet;
    ArrayList<Double> all_Precisions_R;
    ArrayList<Double> all_Aveps_R;
    ArrayList<Double> all_Bprefs_R;
    ArrayList<Double> all_nDCGs_R;
    int totalNumOfQueries;

    public ModelStats(String description) {
        this.description = description;
    }

    /*
    public ModelStats(Model model) {
        this.model = model;
    }
     */
 /*
    public ModelStats(Model model, double Precision_2, double Precision_R, double Avep, double Bpref, double Recall, double nDCG, ArrayList<Integer> testSet, ArrayList<Double> scoreSet) {
        this.model = model;
        this.Precision_2 = Precision_2;
        this.Precision_R = Precision_R;
        this.Recall = Recall;
        this.Avep = Avep;
        this.Bpref = Bpref;
        this.nDCG = nDCG;
        this.testSet = testSet;
        this.scoreSet = scoreSet;
        this.all_Precisions_R = new ArrayList<>();
        this.all_Aveps_R = new ArrayList<>();
        this.all_Bprefs_R = new ArrayList<>();
    }*/
    public ModelStats(double Precision_2, double Precision_R, double Avep, double Bpref, double Recall, double nDCG, double reciprocalRank, ArrayList<Integer> testSet, ArrayList<Double> scoreSet) {
        this.Precision_2 = Precision_2;
        this.Precision_R = Precision_R;
        this.Recall = Recall;
        this.Avep = Avep;
        this.Bpref = Bpref;
        this.nDCG = nDCG;
        this.testSet = testSet;
        this.scoreSet = scoreSet;
        this.all_Precisions_R = new ArrayList<>();
        this.all_Aveps_R = new ArrayList<>();
        this.all_Bprefs_R = new ArrayList<>();
        this.all_nDCGs_R = new ArrayList<>();
        this.reciprocalRank = reciprocalRank;
    }

//    public int getNumOfRels(ArrayList<Integer> testSet, int relThreshold) {
//        int numOfRels = 0;
//
//        for (int i = 0; i < testSet.size(); i++) {
//            if (testSet.get(i) > relThreshold) {
//                numOfRels++;
//            }
//        }
//        return numOfRels;
//    }
    public void evaluate2(HashMap<String, HashMap<String, EvaluationPair>> gt, int relThreshold, String evalFileName) throws IOException, FileNotFoundException, ClassNotFoundException {

        HashMap<String, ArrayList<Integer>> allQueriesTestSet = (HashMap<String, ArrayList<Integer>>) Utils.getSavedObject("results_" + this.description + "_" + evalFileName);
        int cnt = 0;

        for (String qID : allQueriesTestSet.keySet()) {
            // Get the ground truth for the current query
            HashMap<String, EvaluationPair> evalPairsWithCrntQueryId = gt.get(qID);

            // Set R parameters of evaluation metrics
            int R2 = 2;
            int R = EvalCollectionManipulator.getNumOfRels(evalPairsWithCrntQueryId, relThreshold);
            ArrayList<Integer> testSet = allQueriesTestSet.get(qID);

            //System.out.println("Query: " + qID + "Num of rels: " + R + "Total num of pairs: " + allQueriesTestSet.get(qID).size());
            if (R == 0 || evalPairsWithCrntQueryId.size() - R == 0) {
                cnt++;
            } else {
                // Calculate the Precision@2 of our system's answer
                if (R > R2) {
                    this.Precision_2 += EvaluationMetrics.R_Precision(testSet, R2, relThreshold);
                }
                // Calculate the Precision@R (where R is the num of rels) of our system's answer
                this.Precision_R += EvaluationMetrics.R_Precision(testSet, R, relThreshold);
                // Calculate the Recall of our system's answer
                this.Recall += EvaluationMetrics.Recall(testSet, R, R, relThreshold);
                // Calculate the Fallout of our system's answer
                //if ((allQueriesTestSet.get(qID).size() - R) > 0) {
//            System.out.println("qID: " + qID);
//            System.out.println("R: " + R);
//            System.out.println("num of pairs: " + evalPairsWithCrntQueryId.size());
//            System.out.println("num of nonRels: " + (evalPairsWithCrntQueryId.size() - R));
//            System.out.println("Fallout: " + EvaluationMetrics.Fallout(testSet, R, evalPairsWithCrntQueryId.size() - R, relThreshold));
//            System.out.println("\n");
                this.Fallout += EvaluationMetrics.Fallout(testSet, R, evalPairsWithCrntQueryId.size() - R, relThreshold);
                //}
                // Calculate the AveP of our system's answer
                this.Avep += EvaluationMetrics.AVEP(testSet, R, relThreshold);
                // Calculate the BPREF of our system's answer
                this.Bpref += EvaluationMetrics.BPREF(testSet, R, evalPairsWithCrntQueryId.size() - R, relThreshold);
                // Calculate the nDCG of our system's answer
                this.nDCG += EvaluationMetrics.nDCG(testSet, EvaluationMetrics.getIDCG(evalPairsWithCrntQueryId.values()));
                // Calculate the RR of our system's answer
                this.reciprocalRank += EvaluationMetrics.reciprocalRank(testSet, R, relThreshold);
            }
        }

        // Calculate mean BPREF, R_Precision, AveP, Bpref and nDCG for all queries
        this.Precision_2 /= (allQueriesTestSet.size() - cnt);
        this.Precision_R /= (allQueriesTestSet.size() - cnt);
        this.Recall /= (allQueriesTestSet.size() - cnt);
        this.Fallout /= (allQueriesTestSet.size() - cnt);
        this.Avep /= (allQueriesTestSet.size() - cnt);
        this.Bpref /= (allQueriesTestSet.size() - cnt);
        this.nDCG /= (allQueriesTestSet.size() - cnt);
        this.reciprocalRank /= (allQueriesTestSet.size() - cnt);
        this.totalNumOfQueries = (allQueriesTestSet.size() - cnt);
    }

    public void evaluate(Model model, ArrayList<Comment> comments, HashMap<String, HashMap<String, EvaluationPair>> gt, int relThreshold) {

        HashMap<String, String> queryList = new HashMap<>();
        for (String query_id : gt.keySet()) {
            HashMap<String, EvaluationPair> evalPairs = gt.get(query_id);
            queryList.put(query_id, evalPairs.values().iterator().next().getQuery().getText());
        }

        ArrayList<Integer> testSet; // true binary relevance for a query
        ArrayList<Double> crntCompRelevance = new ArrayList<>(); // floating point relevance calculated for a query

        this.testSet = new ArrayList<>(); // true binary relevance for all queries
        this.scoreSet = new ArrayList<>(); // floating point relevance calculated for all queries

        float word2vec_w = 0.0f;
        float wordNet_w = 0.0f;

        if (model instanceof WordnetWord2vecModel) {
            word2vec_w = ((WordnetWord2vecModel) model).getModelWeights().get("word2vec");
            wordNet_w = ((WordnetWord2vecModel) model).getModelWeights().get("wordnet");
        }

        //for each query
        for (String qID : queryList.keySet()) {
            // Get the ground truth for the current query
            HashMap<String, EvaluationPair> evalPairsWithCrntQueryId = gt.get(qID);

            // Set R parameters of evaluation metrics
            int R2 = 2;
            int R = EvalCollectionManipulator.getNumOfRels(evalPairsWithCrntQueryId, relThreshold);
            System.out.println("Query: " + qID + "Num of rels: " + R + "Total num of pairs: " + evalPairsWithCrntQueryId.size());

            // init test set for the current query
            testSet = new ArrayList<>();

            //Get the user's question
            String question = queryList.get(qID);

            model.scoreComments(question);
            ArrayList<Comment> rankedComments = model.getTopComments(comments.size());
            //Retrieve the relevant comments
            //ArrayList<Comment> rankedComments = getTopkRelevantComments(comments, word2vec_w, wordNet_w, question, wm, vec, dict);

            // for all retrieved comments
            for (Comment resultCom : rankedComments) {
                // keep truck of comment's true and calculated relevance value
                // if comment is unjudged skip it
                EvaluationPair p = evalPairsWithCrntQueryId.get(resultCom.getId());
                if (p != null) {
                    if (resultCom.getScore() > 0.0f) {
                        testSet.add(p.getRelevance()); // true binarry relevance
                        //System.out.println(p.getRelevance() + ", " + resultCom.getId());
                        //System.out.println(p.getComment().getId() + " -- " + resultCom.getId());
                    } else {
                        testSet.add(0); // true binarry relevance
                    }

                    this.testSet.add(p.getRelevance()); // keep truck of all (ci,qi) pairs true relevance
                    this.scoreSet.add(resultCom.getScore()); // keep truck of all (ci,qi) pairs calc relevance
                }
            }

            // Calculate the Precision@2 of our system's answer
            if (R > R2) {
                this.Precision_2 += EvaluationMetrics.R_Precision(testSet, R2, relThreshold);
            }
            // Calculate the Precision@R (where R is the num of rels) of our system's answer
            this.Precision_R += EvaluationMetrics.R_Precision(testSet, R, relThreshold);
            // Calculate the Recall of our system's answer
            this.Recall += EvaluationMetrics.Recall(testSet, R, R, relThreshold);
            // Calculate the Fallout of our system's answer
            if ((evalPairsWithCrntQueryId.size() - R) > 0) {
                this.Fallout += EvaluationMetrics.Fallout(testSet, R, evalPairsWithCrntQueryId.size() - R, relThreshold);
            }
            // Calculate the AveP of our system's answer
            this.Avep += EvaluationMetrics.AVEP(testSet, R, relThreshold);
            // Calculate the BPREF of our system's answer
            this.Bpref += EvaluationMetrics.BPREF(testSet, R, evalPairsWithCrntQueryId.size() - R, relThreshold);

            // Calculate the nDCG of our system's answer
            this.nDCG += EvaluationMetrics.nDCG(testSet, EvaluationMetrics.getIDCG(evalPairsWithCrntQueryId.values()));

            this.reciprocalRank += EvaluationMetrics.reciprocalRank(testSet, R, relThreshold);
        }

        // Calculate mean BPREF, R_Precision, AveP, Bpref and nDCG for all queries
        this.Precision_2 /= queryList.size();
        this.Precision_R /= queryList.size();
        this.Recall /= queryList.size();
        this.Fallout /= queryList.size();
        this.Avep /= queryList.size();
        this.Bpref /= queryList.size();
        this.nDCG /= queryList.size();
        this.reciprocalRank /= queryList.size();
        //System.out.println(this);
    }

    public void getAllMetricsBoundedR(int lowerBound, int upperBound, HashMap<String, HashMap<String, EvaluationPair>> gt, int relThreshold, String evalFileName) throws IOException, FileNotFoundException, ClassNotFoundException {

        this.all_Precisions_R = new ArrayList<>();
        this.all_Aveps_R = new ArrayList<>();
        this.all_Bprefs_R = new ArrayList<>();
        this.all_nDCGs_R = new ArrayList<>();
        HashMap<String, ArrayList<Integer>> allQueriesTestSet = (HashMap<String, ArrayList<Integer>>) Utils.getSavedObject("results_" + this.description + "_" + evalFileName);

        for (int R = lowerBound; R <= upperBound; R++) {
            Double tmp_Precision_R = 0.0;
            Double tmp_Avep_R = 0.0;
            Double tmp_Bpref_R = 0.0;
            Double tmp_nDCG_R = 0.0;
            int outOfEval = 0;

            //for each query
            for (String qID : allQueriesTestSet.keySet()) {

                // Get the ground truth for the current query
                HashMap<String, EvaluationPair> evalPairsWithCrntQueryId = gt.get(qID);
                if (R > EvalCollectionManipulator.getNumOfRels(evalPairsWithCrntQueryId, relThreshold)
                        || evalPairsWithCrntQueryId.size() - R == 0) {
                    outOfEval++;
                    continue;
                }

                // init test set for the current query
                ArrayList<Integer> testSet = allQueriesTestSet.get(qID);

                // Calculate the R_Precision of our system's answer
                tmp_Precision_R += EvaluationMetrics.R_Precision(testSet, R, relThreshold);
                // Calculate the AveP of our system's answer
                tmp_Avep_R += EvaluationMetrics.AVEP(testSet, R, relThreshold);
                // Calculate the BPREF of our system's answer
                tmp_Bpref_R += EvaluationMetrics.BPREF(testSet, R, evalPairsWithCrntQueryId.size() - R, relThreshold);
                // Calculate the nDCG of our system's answer
                tmp_nDCG_R += EvaluationMetrics.AVEP(testSet, R, relThreshold);
            }

            int normFactor = allQueriesTestSet.size() - outOfEval;
            System.out.println("Num of Queries at R=" + R + ": " + normFactor);
            // Calculate mean BPREF, R_Precision and AveP for all queries
            all_Precisions_R.add(tmp_Precision_R /= normFactor);
            all_Aveps_R.add(tmp_Avep_R /= normFactor);
            all_Bprefs_R.add(tmp_Bpref_R /= normFactor);
            all_nDCGs_R.add(tmp_nDCG_R /= normFactor);
        }
    }

    /*
    public Model getModel() {
        return this.model;
    }
     */
    public ArrayList<Double> getAllnDCSs_R() {
        return this.all_nDCGs_R;
    }

    public ArrayList<Double> getAllPrecisions_R() {
        return this.all_Precisions_R;
    }

    public ArrayList<Double> getAllAveps_R() {
        return this.all_Aveps_R;
    }

    public ArrayList<Double> getAllBprefs_R() {
        return this.all_Bprefs_R;
    }

    public double getPrecision_2() {
        return this.Precision_2;
    }

    public double getPrecision_R() {
        return this.Precision_R;
    }

    public double getAvep() {
        return this.Avep;
    }

    public double getBpref() {
        return this.Bpref;
    }

    public double getNDCG() {
        return this.nDCG;
    }

    public ArrayList<Integer> getTestSet() {
        return this.testSet;
    }

    public ArrayList<Double> getScoreSet() {
        return this.scoreSet;
    }

    public String getDescription() {
        return this.description;
    }

    /*
    public void setModel(Model model) {
        this.model = model;
    }
     */
    public void setPrecision_2(double Precision_2) {
        this.Precision_2 = Precision_2;
    }

    public void setPrecision_R(double Precision_R) {
        this.Precision_R = Precision_R;
    }

    public void getAvep(double Avep) {
        this.Avep = Avep;
    }

    public void setBpref(double Bpref) {
        this.Bpref = Bpref;
    }

    public void setNDCG(double nDCG) {
        this.nDCG = nDCG;
    }

    public void setTestSet(ArrayList<Integer> testSet) {
        this.testSet = testSet;
    }

    public void setScoreSet(ArrayList<Double> scoreSet) {
        this.scoreSet = scoreSet;
    }

    /*
    @Override
    public String toString() {
        if (this.model instanceof WordnetWord2vecModel) {
            WordnetWord2vecModel tmp_model = (WordnetWord2vecModel) this.model;
            return "Model: " + this.model.getDescription() + "\n"
                    + "Wordnet Weight: " + tmp_model.getModelWeights().get("wordnet") + "\n"
                    + "Word2vec Weight: " + tmp_model.getModelWeights().get("word2vec") + "\n"
                    + "Precision_2: " + this.Precision_2 + "\n"
                    + "Precision_R: " + this.Precision_R + "\n"
                    + "Recall: " + this.Recall + "\n"
                    + "Fallout: " + this.Fallout + "\n"
                    + "Avep: " + this.Avep + "\n"
                    + "Bpref: " + this.Bpref + "\n"
                    + "nDCG: " + this.nDCG + "\n"
                    + "Test Set:  " + this.testSet + "\n"
                    + "Score Set: " + this.scoreSet;
        } else {
            return "Model: " + this.model.getDescription() + "\n"
                    + "Precision_2: " + this.Precision_2 + "\n"
                    + "Precision_R: " + this.Precision_R + "\n"
                    + "Recall: " + this.Recall + "\n"
                    + "Fallout: " + this.Fallout + "\n"
                    + "Avep: " + this.Avep + "\n"
                    + "Bpref: " + this.Bpref + "\n"
                    + "nDCG: " + this.nDCG + "\n"
                    + "Test Set:  " + this.testSet + "\n"
                    + "Score Set: " + this.scoreSet;
        }
    }
     */
    @Override
    public String toString() {
        return "Model: " + this.description + "\n"
                + "Total num of Queries: " + this.totalNumOfQueries + "\n"
                + "Precision_2: " + this.Precision_2 + "\n"
                + "Precision_R: " + this.Precision_R + "\n"
                + "Recall: " + this.Recall + "\n"
                + "Fallout: " + this.Fallout + "\n"
                + "Avep: " + this.Avep + "\n"
                + "Bpref: " + this.Bpref + "\n"
                + "nDCG: " + this.nDCG + "\n"
                + "MRR: " + this.reciprocalRank + "\n"
                + "Test Set:  " + this.testSet + "\n"
                + "Score Set: " + this.scoreSet + "\n"
                + "Avep untill R: " + this.all_Aveps_R + "\n"
                + "Bpref untill R: " + this.all_Bprefs_R + "\n"
                + "Precision untill R: " + this.all_Precisions_R + "\n"
                + "nDCG untill R: " + this.all_nDCGs_R + "\n";
    }
}
