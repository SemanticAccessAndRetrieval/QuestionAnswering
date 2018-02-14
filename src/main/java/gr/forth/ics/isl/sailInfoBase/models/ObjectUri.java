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

import java.util.HashSet;

/**
 *
 * @author Sgo
 */


public class ObjectUri extends Object {

    private String uri;
    private HashSet<String> rdfTypes;
    private HashSet<String> rdfTypesLabels;

    public void setUri(String uri) {
        this.uri = uri;
    }

    public void setRdfTypes(HashSet<String> rdfTypes) {
        this.rdfTypes = rdfTypes;
    }

    public void setRdfTypesLabels(HashSet<String> rdfTypesLabels) {
        this.rdfTypesLabels = rdfTypesLabels;
    }

    public String getUri() {
        return this.uri;
    }

    public HashSet<String> getRdfTypesLabels() {
        return this.rdfTypesLabels;
    }

    @Override
    public String toString() {
        return "URI: " + this.uri + "\n"
                + "Label: " + super.getLabel() + "\n"
                + "Types: " + this.rdfTypes + "\n"
                + "TypesLabels: " + this.rdfTypesLabels + "\n"
                + "\n";
    }

}
