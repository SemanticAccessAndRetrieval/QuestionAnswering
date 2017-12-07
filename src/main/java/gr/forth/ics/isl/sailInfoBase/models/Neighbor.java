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

import java.io.Serializable;

/**
 * This class represents the neighbor of some Individual. It is a separate class
 * than individual, since this one can be either a uri or a literal, in contrast
 * with the individuals which are always a uri.
 *
 * @author Sgo
 */
public class Neighbor implements Serializable {

    private String uriOrLiteral;
    private String property;
    private String propertyName;

    public Neighbor(String property, String uriOrLiteral) {
        this.uriOrLiteral = uriOrLiteral;
        this.property = property;
        setPropertyName();
    }

    private void setPropertyName() {
        String[] uriParts = this.getProperty().split("/");
        String[] location = uriParts[uriParts.length - 1].split("#");
        this.propertyName = location[location.length - 1];
    }

    public String getPropertyName() {
        return this.propertyName;
    }

    public String getUriOrLiteral() {
        return this.uriOrLiteral;
    }

    public String getProperty() {
        return this.property;
    }

    @Override
    public String toString() {
        return this.property + " ==> " + this.uriOrLiteral;
    }
}
