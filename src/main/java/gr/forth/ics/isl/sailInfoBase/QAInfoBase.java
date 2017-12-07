/* 
 *  This code belongs to the Semantic Access and Retrieval (SAR) group of the 
 *  Information Systems Laboratory (ISL) of the 
 *  Institute of Computer Science (ICS) of the  
 *  Foundation for Research and Technology - Hellas (FORTH)
 *  Nobody is allowed to use, copy, distribute, or modify this work.
 *  It is published for reasons of research results reproducibility.
 *  (c) 2017 Semantic Access and Retrieval group, All rights reserved
 */









package gr.forth.ics.isl.sailInfoBase;

import gr.forth.ics.isl.sailInfoBase.models.Individual;
import gr.forth.ics.isl.sailInfoBase.models.Neighbor;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.RepositoryException;

/**
 *
 * @author Sgo
 */
public class QAInfoBase extends SailInfoBase {

    /*private final String getAllClasses = "SELECT ?class"
            + "WHERE{\n"
            + "    ?class rdfs:subClassOf csdT:Course."
            //+ "    ?someClass rdf:type owl:Class .\n"
            //+ "    ?class rdfs:label ?label ."
            + "}";
     */
    public QAInfoBase() throws RepositoryException, IOException {

    }

    public ArrayList<Neighbor> getNeighborsOf(String individual) throws RepositoryException, MalformedQueryException, QueryEvaluationException {

        String getNeighborsOfIndividualAsString = "\nSELECT ?someProperty ?neighbor\n"
                + "WHERE { \n"
                // inverse ?someProperty does not seem to work correctly need to check
                //+ "    ?neighbor ?someProperty <" + individual + ">.\n"
                + "    <" + individual + "> ?someProperty ?neighbor.\n"
                // seems I need only the english but then I loose some fields
                + "    filter(langMatches(lang(?neighbor),\"EN\"))."
                + "}\n";

        HashSet<ArrayList<String>> answerSet = queryRepo(getNeighborsOfIndividualAsString);
        //System.out.println(answerSet);
        ArrayList<Neighbor> neighbors = new ArrayList<>();
        if (answerSet != null) {
            for (ArrayList<String> answer : answerSet) {
                //for (String ind : answer) {
                //System.out.println(new Neighbor(answer.get(0), answer.get(1)));
                neighbors.add(new Neighbor(answer.get(0), answer.get(1)));
                //}
            }

            return neighbors;
        } else {
            return null;
        }
    }

    public ArrayList<Individual> getAllIndividuals() throws RepositoryException, MalformedQueryException, QueryEvaluationException {

        String getAllIndividualsAsString = "\nSELECT ?individual\n"
                + "WHERE { \n"
                + "    ?individual rdf:type ?class.\n"
                + "    ?class rdf:type rdfs:Class"
                + "}\n";

        HashSet<ArrayList<String>> answerSet = queryRepo(getAllIndividualsAsString);
        //System.out.println(answerSet);
        ArrayList<Individual> allIndividuals = new ArrayList<>();
        for (ArrayList<String> answer : answerSet) {
            for (String ind : answer) {
                if (ind.contains("http://") || ind.contains("http://")) {
                    Individual individual = new Individual(ind);
                    allIndividuals.add(individual);
                }
            }

        }

        return allIndividuals;
    }

    public HashSet<ArrayList<String>> getAllIndividualsOfType(String prefix, String type) throws RepositoryException, MalformedQueryException, QueryEvaluationException {

        String getAllIndividualsOfTypeAsString = "SELECT ?individual\n"
                + "WHERE { \n"
                + "    ?individual rdf:type " + prefix + ":" + type + ".\n"
                + "}\n";

        HashSet<ArrayList<String>> answerSet = queryRepo(getAllIndividualsOfTypeAsString);

        return answerSet;
    }

    /*
    public HashSet<ArrayList<String>> getAllClasses() throws RepositoryException, MalformedQueryException, QueryEvaluationException {
        HashSet<ArrayList<String>> answerSet = queryRepo(getAllClasses);
        return answerSet;
    }
     */
    public static void main(String[] args) throws RepositoryException, MalformedQueryException, QueryEvaluationException, IOException {
        QAInfoBase KB = new QAInfoBase();
        String prefix = "csdT";
        String type = "Course";

        //HashSet<ArrayList<String>> individualsOfTypeX = KB.getAllIndividualsOfType(prefix, type);
        //KB.printAnswer(individualsOfTypeX);
        ArrayList<Individual> individuals = KB.getAllIndividuals();
        //System.out.println(individuals);
        //KB.printAnswer(individuals);

        String cs252 = "http://www.csd.uoc.gr/CS-252";
        //String wrongIndi = "lalalala";
        Individual instance = new Individual(cs252);
        ArrayList<Neighbor> neighbors = KB.getNeighborsOf(instance.getURI().toString());
        //System.out.println(neighbors);

        /*
        // read some text in the text variable
        String text = "What is the email of Yiannis Tzitzikas?";

        ArrayList<Word> annotatedText = NlpAnalyzer.getWordsWithPosNer(text);

        for (Word w : annotatedText) {
            System.out.println(w.getText());
            System.out.println(w.getNer());
        }

        System.out.println(annotatedText);
         */
    }
}
