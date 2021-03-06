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


public class EvaluationComment {

    private String id;
    private String date;
    private String text;

    public EvaluationComment(String id, String text, String date) {
        this.id = id;
        this.text = text;
        this.date = date;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getText() {
        return this.text;
    }

    public String getId() {
        return this.id;
    }

    public String getDate() {
        return this.date;
    }
}
