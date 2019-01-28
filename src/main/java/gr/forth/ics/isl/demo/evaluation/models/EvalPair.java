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


public class EvalPair {

    private String pairID;
    private String queryID;
    private String passageID;
    private int relevance;

    public EvalPair(String queryID, String passageID, int relevance) {
        this.pairID = queryID + "-" + passageID;
        this.queryID = queryID;
        this.passageID = passageID;
        this.relevance = relevance;
    }

    public String getPairId() {
        return this.pairID;
    }

    public String getPairId(String queryID, String passageID) {
        return this.pairID + "-" + this.passageID;
    }

    public String getPassageId() {
        return this.passageID;
    }

    public String getQueryId() {
        return this.queryID;
    }

    public int getRelevance() {
        return this.relevance;
    }

}
