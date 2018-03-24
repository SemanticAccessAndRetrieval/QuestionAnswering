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

import java.util.ArrayList;

/**
 *
 * @author Sgo
 */


public class ModelHyperparameters implements Comparable<ModelHyperparameters>, java.io.Serializable {
    private double score;
    private double AveP;
    private double R_Precision;
    private double BPREF;
    private float word2vec_w;
    private float wordNet_w;
    private float threshold;
    private ArrayList<Integer> resultSet;
    private ArrayList<Integer> trueSet;
    private ArrayList<Double> scoreSet;

    public ModelHyperparameters(double score, float word2vec_w, float wordNet_w) {
        this.score = score;
        //this.threshold = threshold;
        this.word2vec_w = word2vec_w;
        this.wordNet_w = wordNet_w;
    }

    public ArrayList<Integer> getResultSet() {
        return this.resultSet;
    }

    public ArrayList<Integer> getTrueSet() {
        return this.trueSet;
    }

    public ArrayList<Double> getScoreSet() {
        return this.scoreSet;
    }

    public double getAveP() {
        return this.AveP;
    }

    public double getR_Precision() {
        return this.R_Precision;
    }

    public double getBPREF() {
        return this.BPREF;
    }

    public double getScore() {
        return this.score;
    }

    public float getThreshold() {
        return this.threshold;
    }

    public float getWord2vecWeight() {
        return this.word2vec_w;
    }

    public float getWordNetWeight() {
        return this.wordNet_w;
    }

    public void setAveP(double avep) {
        this.AveP = avep;
    }

    public void setBPREF(double bpref) {
        this.BPREF = bpref;
    }

    public void setR_Precision(double rPrecision) {
        this.R_Precision = rPrecision;
    }

    public void setResultSet(ArrayList<Integer> resultSet) {
        this.resultSet = resultSet;
    }

    public void setTrueSet(ArrayList<Integer> trueSet) {
        this.trueSet = trueSet;
    }

    public void setScoreSet(ArrayList<Double> scoreSet) {
        this.scoreSet = scoreSet;
    }

    @Override
    public String toString() {
        return "Score: " + this.score + "\n"
                + "BPREF: " + this.BPREF + "\n"
                + "R_Precision: " + this.R_Precision + "\n"
                + "AveP: " + this.AveP + "\n"
                //+ "Threshold: " + this.threshold + "\n"
                + "Word2vec Weight: " + this.word2vec_w + "\n"
                + "WordNet Weight: " + this.wordNet_w + "\n"
                + "TrueSet: " + this.trueSet + "\n"
                //+ "ResultSet: " + this.resultSet + "\n"
                + "ResultScoreSet: " + this.scoreSet;
    }

    @Override
    public int compareTo(ModelHyperparameters model) {
        Double inputModelPrecision = model.getScore();
        return -inputModelPrecision.compareTo(this.getScore());
    }
}
