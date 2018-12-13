/*
 *  This code belongs to the Semantic Access and Retrieval (SAR) group of the
 *  Information Systems Laboratory (ISL) of the
 *  Institute of Computer Science (ICS) of the
 *  Foundation for Research and Technology - Hellas (FORTH)
 *  Nobody is allowed to use, copy, distribute, or modify this work.
 *  It is published for reasons of research results reproducibility.
 *  (c) 2017 Semantic Access and Retrieval group, All rights reserved
 */
package gr.forth.ics.isl.demoExternal.evaluation.models;

import java.util.ArrayList;

/**
 *
 * @author Sgo
 */


public class QaldEvalUnit {

    private int id;
    private String question;
    private ArrayList<String> answers;
    private String answertype;
    private boolean isHybrid;
    private boolean needsOnlyDbo;
    private boolean needsAggregation;

    public QaldEvalUnit(int id, String question) {
        this.id = id;
        this.question = question;
    }

    public void setHybrid(boolean hyb) {
        this.isHybrid = hyb;
    }

    public boolean isHybrid() {
        return this.isHybrid;
    }

    public void setOnlyDbo(boolean dbo) {
        this.needsOnlyDbo = dbo;
    }

    public boolean isOnlyDbo() {
        return this.needsOnlyDbo;
    }

    public void setAggregation(boolean agg) {
        this.needsAggregation = agg;
    }

    public boolean isAggregation() {
        return this.needsAggregation;
    }

    public void setAnswers(ArrayList<String> answers) {
        this.answers = answers;
    }

    public ArrayList<String> getAnswers() {
        return this.answers;
    }

    public void setAnswerType(String answertype) {
        this.answertype = answertype;
    }

    public String getAnswerType() {
        return this.answertype;
    }

    public String getQuestion() {
        return this.question;
    }

    public int getId() {
        return this.id;
    }

    @Override
    public String toString() {
        return this.id + "\t" + this.question + "\t" + this.answertype + "\t" + this.answers;
    }

    public String toStringExtended() {
        return this.id + "\t" + this.isHybrid + "\t" + this.needsOnlyDbo + "\t" + this.needsAggregation + "\t" + this.question + "\t" + this.answertype + "\t" + this.answers;
    }
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
    }

}
