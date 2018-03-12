/*
 *  This code belongs to the Semantic Access and Retrieval (SAR) group of the
 *  Information Systems Laboratory (ISL) of the
 *  Institute of Computer Science (ICS) of the
 *  Foundation for Research and Technology - Hellas (FORTH)
 *  Nobody is allowed to use, copy, distribute, or modify this work.
 *  It is published for reasons of research results reproducibility.
 *  (c) 2017 Semantic Access and Retrieval group, All rights reserved
 */
package gr.forth.ics.isl.evaluation.models;

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

    public ModelHyperparameters(double score, float word2vec_w, float wordNet_w, float threshold) {
        this.score = score;
        this.threshold = threshold;
        this.word2vec_w = word2vec_w;
        this.wordNet_w = wordNet_w;
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

    @Override
    public String toString() {
        return "Score: " + this.score + "\n"
                + "BPREF: " + this.BPREF + "\n"
                + "R_Precision: " + this.R_Precision + "\n"
                + "AveP: " + this.AveP + "\n"
                + "Threshold: " + this.threshold + "\n"
                + "Word2vec Weight: " + this.word2vec_w + "\n"
                + "WordNet Weight: " + this.wordNet_w;
    }

    @Override
    public int compareTo(ModelHyperparameters model) {
        Double inputModelPrecision = model.getScore();
        return -inputModelPrecision.compareTo(this.getScore());
    }
}
