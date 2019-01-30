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
public class EvaluationResult {

    private long pairId;
    private int queryId;
    private String queryText;
    private int passageId;
    private String passageText;
    private int pairRelevance;
    private double score;
    private String bestSentence;

    public EvaluationResult(long pairId, int queryId, String queryText, int passageId, String passageText, double score, String bestSentence) {
        this.pairId = pairId;
        this.queryId = queryId;
        this.queryText = queryText;
        this.passageId = passageId;
        this.passageText = passageText;
        this.score = score;
        this.bestSentence = bestSentence;
    }

    public EvaluationResult(long pairId, int queryId, String queryText, int passageId, String passageText, int pairRelevance, double score, String bestSentence) {
        this.pairId = pairId;
        this.queryId = queryId;
        this.queryText = queryText;
        this.passageId = passageId;
        this.passageText = passageText;
        this.pairRelevance = pairRelevance;
        this.score = score;
        this.bestSentence = bestSentence;
    }

    public double getScore() {
        return this.score;
    }

    public String getBestSentence() {
        return this.bestSentence;
    }

    public long getPairId() {
        return this.pairId;
    }

    public int getQueryId() {
        return this.queryId;
    }

    public String getQueryText() {
        return this.queryText;
    }

    public int getPassageId() {
        return this.passageId;
    }

    public String getPassageText() {
        return this.passageText;
    }

    public int getPairRelevance() {
        return this.pairRelevance;
    }

    public void setPairRelevance(int pairRelevance) {
        this.pairRelevance = pairRelevance;
    }

    @Override
    public String toString() {
        return this.pairId + "\t" + this.queryId + "\t" + this.queryText + "\t"
                + this.passageId + "\t" + this.passageText + "\t" + this.pairRelevance
                + "\t" + this.score + "\t" + this.bestSentence + "\n";
    }
}
