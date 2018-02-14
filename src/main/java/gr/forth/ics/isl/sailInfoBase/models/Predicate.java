/*
 *  This code belongs to the Semantic Access and Retrieval (SAR) group of the
 *  Information Systems Laboratory (ISL) of the
 *  Institute of Computer Science (ICS) of the
 *  Foundation for Research and Technology - Hellas (FORTH)
 *  Nobody is allowed to use, copy, distribute, or modify this work.
 *  It is published for reasons of research results reproducibility.
 *  (c) 2017 Semantic Access and Retrieval group, All rights reserved
 */
package gr.forth.ics.isl.sailInfoBase.models;

/**
 *
 * @author Sgo
 */


public class Predicate implements Resource {

    String uri;
    String label;

    public void setUri(String uri) {
        this.uri = uri;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getUri() {
        return this.uri;
    }

    public String getLabel() {
        return this.label;
    }

}
