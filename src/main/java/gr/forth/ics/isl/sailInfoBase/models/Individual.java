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
import java.util.ArrayList;
import java.util.HashSet;

/**
 *
 * @author Sgo
 */
public class Individual implements Serializable {

    private String uri;
    private String name;
    //ArrayList<String> neighbors;
    ArrayList<Neighbor> neighbors;
    double[] vectorRepresentation;
    HashSet<String> bagOfWordsRepresentation = new HashSet<>();

    public Individual(String uri) {
        this.uri = uri.replace(" ", "_");
        this.setName();
    }

    public void setBagOfWordsRepresentation(HashSet<String> bagOfWordsRepresentation) {
        this.bagOfWordsRepresentation = bagOfWordsRepresentation;
    }

    public HashSet<String> getbagOfWordsRepresentation() {
        return this.bagOfWordsRepresentation;
    }

    public void setNeighbors(ArrayList<Neighbor> neighbors) {
        this.neighbors = neighbors;
    }

    public void setVectorRepresentation(double[] vecRep) {
        this.vectorRepresentation = vecRep;
    }

    public double[] getVectorRepresentation() {
        return this.vectorRepresentation;
    }

    public ArrayList<Neighbor> getNeighbors() {
        return this.neighbors;
    }

    private void setName() {
        String[] uriParts = this.getURI().split("/");
        String[] location = uriParts[uriParts.length - 1].split("#");
        this.name = location[location.length - 1];
    }

    public String getURI() {
        return this.uri;
    }

    public String getName() {
        return this.name;
    }

    @Override
    public String toString() {
        return this.uri + " < " + this.name + ",  " + this.vectorRepresentation + " > ";
    }

    public static void main(String[] args) {
        String cs252 = "http://www.csd.uoc.gr/CS-252";
        Individual individual = new Individual(cs252);

        System.out.println(individual.getURI());
        //System.out.println(individual.getName());
    }

}
