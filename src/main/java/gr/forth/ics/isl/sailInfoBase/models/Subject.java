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

import java.util.HashMap;
import java.util.HashSet;

/**
 *
 * @author Sgo
 */


public class Subject implements Resource {

    private String uri;
    private String label;
    private HashSet<String> rdfTypes;
    private HashSet<String> rdfTypesLabels;
    private HashMap<String, HashSet<String>> dataTypePropsWithValues;
    private HashMap<String, HashSet<String>> undeclaredDataTypePropsWithValues;
    private HashMap<String, HashSet<String>> objectPropsWithObjectValues;

    public void setUri(String uri) {
        this.uri = uri;
    }

    public void setLabel(String label) {
        this.label = label;
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

    public String getLabel() {
        return this.label;
    }

    public HashSet<String> getRdfTypes() {
        return this.rdfTypes;
    }

    public HashSet<String> getRdfTypesLabels() {
        return this.rdfTypesLabels;
    }

    public void setDataTypePropsWithValues(HashMap<String, HashSet<String>> dataTypePropsWithValues) {
        this.dataTypePropsWithValues = dataTypePropsWithValues;
    }

    public HashMap<String, HashSet<String>> getDataTypePropsWithValues() {
        return this.dataTypePropsWithValues;
    }

    public void setUndeclaredDataTypePropsWithValues(HashMap<String, HashSet<String>> dataTypePropsWithValues) {
        this.undeclaredDataTypePropsWithValues = dataTypePropsWithValues;
    }

    public HashMap<String, HashSet<String>> getUndeclaredDataTypePropsWithValues() {
        return this.undeclaredDataTypePropsWithValues;
    }

    public void setObjectPropsWithObjectValues(HashMap<String, HashSet<String>> objectPropsWithObjectValues) {
        this.objectPropsWithObjectValues = objectPropsWithObjectValues;
    }

    public HashMap<String, HashSet<String>> getObjectPropsWithObjectValues() {
        return this.objectPropsWithObjectValues;
    }

    @Override
    public String toString() {
        return "URI: " + this.uri + "\n"
                + "Label: " + this.label + "\n"
                + "Types: " + this.rdfTypes + "\n"
                + "TypesLabels: " + this.rdfTypesLabels + "\n"
                + "DataTypePropsWithValues: " + this.dataTypePropsWithValues + "\n"
                + "UndeclaredDataTypePropsWithValues: " + this.undeclaredDataTypePropsWithValues + "\n"
                + "ObjectPropsWithObjectValues: " + this.objectPropsWithObjectValues + "\n"
                + "\n";
    }

}
