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

import static gr.forth.ics.isl.demo.evaluation.EvalCollectionManipulator.getNumOfRels;
import gr.forth.ics.isl.demo.evaluation.EvaluationMetrics;
import gr.forth.ics.isl.demo.models.Model;
import gr.forth.ics.isl.demo.models.WordnetWord2vecModel;
import gr.forth.ics.isl.nlp.models.Comment;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author Sgo
 */
public class ModelStats {

    private Model model;
    private double Precision_2;
    private double Precision_R;
    private double Avep;
    private double Bpref;
    ArrayList<Integer> testSet;
    ArrayList<Double> scoreSet;

    public ModelStats(Model model) {
        this.model = model;
    }

    public ModelStats(Model model, double Precision_2, double Precision_R, double Avep, double Bpref, ArrayList<Integer> testSet, ArrayList<Double> scoreSet) {
        this.model = model;
        this.Precision_2 = Precision_2;
        this.Precision_R = Precision_R;
        this.Avep = Avep;
        this.Bpref = Bpref;
        this.testSet = testSet;
        this.scoreSet = scoreSet;
    }

    public void evaluate(ArrayList<Comment> comments, HashMap<String, HashMap<String, EvaluationPair>> gt) {

        int cnt = 1; // query counter

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

        if (this.model instanceof WordnetWord2vecModel) {
            word2vec_w = ((WordnetWord2vecModel) this.model).getModelWeights().get("word2vec");
            wordNet_w = ((WordnetWord2vecModel) this.model).getModelWeights().get("wordnet");
        }

        //for each query
        while (cnt <= queryList.size()) {

            // Get the ground truth for the current query
            HashMap<String, EvaluationPair> evalPairsWithCrntQueryId = gt.get("q" + cnt);

            // Set R parameters of evaluation metrics
            int R2 = 2;
            int R = getNumOfRels(evalPairsWithCrntQueryId);
            //System.out.println(R);

            // init test set for the current query
            testSet = new ArrayList<>();

            //Get the user's question
            String question = queryList.get("q" + cnt);

            this.model.scoreComments(question);
            ArrayList<Comment> rankedComments = this.model.getTopComments(comments.size());
            //Retrieve the relevant comments
            //ArrayList<Comment> rankedComments = getTopkRelevantComments(comments, word2vec_w, wordNet_w, question, wm, vec, dict);

            // for all retrieved comments
            for (Comment resultCom : rankedComments) {
                // keep truck of comment's true and calculated relevance value
                // if comment is unjudged skip it
                EvaluationPair p = evalPairsWithCrntQueryId.get(resultCom.getId());
                if (p != null) {
                    testSet.add(p.getRelevance()); // true binarry relevance

                    //System.out.println(p.getRelevance() + ", " + resultCom.getId());
                    //System.out.println(p.getComment().getId() + " -- " + resultCom.getId());
                    // keep truck of all (ci,qi) pairs relevance
                    this.testSet.add(p.getRelevance());
                    this.scoreSet.add(resultCom.getScore());
                }
            }

            cnt++; // go to the next query

            // Calculate the R_Precision of our system's answer
            this.Precision_2 += EvaluationMetrics.R_Precision(testSet, R2);
            this.Precision_R += EvaluationMetrics.R_Precision(testSet, R);
            // Calculate the AveP of our system's answer
            this.Avep += EvaluationMetrics.AVEP(testSet, R);
            // Calculate the BPREF of our system's answer
            this.Bpref += EvaluationMetrics.BPREF(testSet, R);
        }

        // Calculate mean BPREF, R_Precision and AveP for all queries
        this.Precision_2 /= queryList.size();
        this.Precision_R /= queryList.size();
        this.Avep /= queryList.size();
        this.Bpref /= queryList.size();

        //System.out.println(this);
    }

    public Model getModel() {
        return this.model;
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

    public ArrayList<Integer> getTestSet() {
        return this.testSet;
    }

    public ArrayList<Double> getScoreSet() {
        return this.scoreSet;
    }

    public void setModel(Model model) {
        this.model = model;
    }

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

    public void setTestSet(ArrayList<Integer> testSet) {
        this.testSet = testSet;
    }

    public void getScoreSet(ArrayList<Double> scoreSet) {
        this.scoreSet = scoreSet;
    }

    @Override
    public String toString() {
        if (this.model instanceof WordnetWord2vecModel) {
            WordnetWord2vecModel tmp_model = (WordnetWord2vecModel) this.model;
            return "Model: " + this.model.getDescription() + "\n"
                    + "Wordnet Weight: " + tmp_model.getModelWeights().get("wordnet") + "\n"
                    + "Word2vec Weight: " + tmp_model.getModelWeights().get("word2vec") + "\n"
                    + "Precision_2: " + this.Precision_2 + "\n"
                    + "Precision_R: " + this.Precision_R + "\n"
                    + "Avep: " + this.Avep + "\n"
                    + "Bpref: " + this.Bpref + "\n"
                    + "Test Set:  " + this.testSet + "\n"
                    + "Score Set: " + this.scoreSet;
        } else {
            return "Model: " + this.model.getDescription() + "\n"
                    + "Precision_2: " + this.Precision_2 + "\n"
                    + "Precision_R: " + this.Precision_R + "\n"
                    + "Avep: " + this.Avep + "\n"
                    + "Bpref: " + this.Bpref + "\n"
                    + "Test Set:  " + this.testSet + "\n"
                    + "Score Set: " + this.scoreSet;
        }
    }

}
