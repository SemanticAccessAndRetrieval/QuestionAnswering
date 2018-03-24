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


public class EvaluationPair {

    private int id;
    private EvaluationQuery query;
    private EvaluationComment comment;
    private int relevance;

    public EvaluationPair(int id, EvaluationQuery query, EvaluationComment comment, int relevance) {
        this.id = id;
        this.query = query;
        this.comment = comment;
        this.relevance = relevance;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setQuery(EvaluationQuery query) {
        this.query = query;
    }

    public void setComment(EvaluationComment comment) {
        this.comment = comment;
    }

    public EvaluationQuery getQuery() {
        return this.query;
    }

    public EvaluationComment getComment() {
        return this.comment;
    }

    public int getId() {
        return this.id;
    }

    public int getRelevance() {
        return this.relevance;
    }
}
