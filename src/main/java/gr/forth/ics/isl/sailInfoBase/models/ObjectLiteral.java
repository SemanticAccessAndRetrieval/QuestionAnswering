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


public class ObjectLiteral extends Object {

    private String xsdType;
    private String xsdValue;
    private String xsdValueLabel;

    public void setXsdType(String xsdType) {
        this.xsdType = xsdType;
    }

    public void setXsdValue(String xsdValue) {
        this.xsdValue = xsdValue;
    }

    public void setXsdValueLabel(String xsdValueLabel) {
        this.xsdValueLabel = xsdValueLabel;
    }

    public String getXsdType() {
        return this.xsdType;
    }

    public String getXsdValue() {
        return this.xsdValue;
    }

    public String getXsdValueLabel() {
        return this.xsdValueLabel;
    }

    @Override
    public String toString() {
        return "xsd:Value: " + this.xsdValue + "\n"
                + "Label: " + super.getLabel() + "\n"
                + "xsd:Types: " + this.xsdType + "\n"
                + "xsdValueLabels: " + this.xsdValueLabel + "\n"
                + "\n";
    }
}
