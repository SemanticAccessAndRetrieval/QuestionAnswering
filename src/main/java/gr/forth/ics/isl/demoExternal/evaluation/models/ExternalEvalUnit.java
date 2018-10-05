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

import java.util.Arrays;

/**
 *
 * @author Sgo
 */


public class ExternalEvalUnit {

    private String question;
    private String gtSubject;
    private String gtObject;
    private String gtPredicate;
    private String[] gtTriple;
    private String subject;
    private String object;
    private String predicate;
    private String[] triple;

    public ExternalEvalUnit(String[] tripleAndQuestion) {
        this.gtTriple = Arrays.copyOfRange(tripleAndQuestion, 0, tripleAndQuestion.length - 1);
        this.gtSubject = tripleAndQuestion[0].trim();
        this.gtPredicate = tripleAndQuestion[1].trim();
        this.gtObject = tripleAndQuestion[2].trim();
        this.question = tripleAndQuestion[3].trim();
    }

    public void setTriple(String[] triple) {
        this.triple = triple;
    }

    public void setObject(String obj) {
        this.object = obj;
    }

    public void setSubject(String sbj) {
        this.subject = sbj;
    }

    public void setPredicate(String prd) {
        this.predicate = prd;
    }

    public String[] getTriple() {
        return this.triple;
    }

    public String getObject() {
        return this.object;
    }

    public String getSubject() {
        return this.subject;
    }

    public String getPredicate() {
        return this.predicate;
    }

    public String[] getGtTriple() {
        return this.gtTriple;
    }

    public String getGtObject() {
        return this.gtObject;
    }

    public String getGtSubject() {
        return this.gtSubject;
    }

    public String getGtPredicate() {
        return this.gtPredicate;
    }

    @Override
    public String toString() {
        return "{" + this.question + ": <" + this.gtSubject + ", " + this.gtPredicate + ", " + this.gtObject + ">}";
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
    }

}
