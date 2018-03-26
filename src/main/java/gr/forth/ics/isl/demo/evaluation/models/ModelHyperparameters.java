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

/**
 *
 * @author Sgo
 */


public class ModelHyperparameters implements Comparable<ModelHyperparameters>, java.io.Serializable {
    private double score;
    private float word2vec_w;
    private float wordNet_w;

    public ModelHyperparameters(double score, float word2vec_w, float wordNet_w) {
        this.score = score;
        //this.threshold = threshold;
        this.word2vec_w = word2vec_w;
        this.wordNet_w = wordNet_w;
    }

    public double getScore() {
        return this.score;
    }

    public float getWord2vecWeight() {
        return this.word2vec_w;
    }

    public float getWordNetWeight() {
        return this.wordNet_w;
    }

    @Override
    public String toString() {
        return "Score: " + this.score + "\n"
                + "Word2vec Weight: " + this.word2vec_w + "\n"
                + "WordNet Weight: " + this.wordNet_w;
    }

    @Override
    public int compareTo(ModelHyperparameters model) {
        Double inputModelPrecision = model.getScore();
        return -inputModelPrecision.compareTo(this.getScore());
    }
}
