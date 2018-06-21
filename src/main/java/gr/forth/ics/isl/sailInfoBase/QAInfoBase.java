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
import gr.forth.ics.isl.sailInfoBase.models.ObjectUri;
import gr.forth.ics.isl.sailInfoBase.models.Subject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;
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

    public HashMap<String, HashSet<String>> getAllObjectPropertiesWithObjectValuesOf(String individual) throws RepositoryException, MalformedQueryException, QueryEvaluationException {
//        String getAllObjectPropertiesWithObjectValuesOfAsString = "SELECT DISTINCT ?p ?o\n"
//                + "WHERE { \n"
//                + "<" + individual + "> ?p ?o .\n"
//                + "?p a owl:DatatypeProperty .\n"
//                + "}\n";

        String getAllObjectPropertiesWithObjectValuesOfAsString = "SELECT ?p ?o\n"
                + "WHERE {\n"
                + "<" + individual + "> ?p ?o .\n"
                + "filter isURI(?o) "
                + "minus {<" + individual + "> a ?o .}}\n";
        HashSet<ArrayList<String>> answerSet = queryRepo(getAllObjectPropertiesWithObjectValuesOfAsString);
        //System.out.println(answerSet);

        HashMap<String, HashSet<String>> objectPropsWithValues = new HashMap<>();
        HashSet<String> values = new HashSet<>();
        int cnt = 0;
        if (answerSet != null) {
            for (ArrayList<String> ans : answerSet) {
                String propName = "";
                for (String value : ans) {
                    if (cnt == 0) {
                        propName = value;
                        cnt++;
                    } else {
                        values.add(value);
                    }
                }
                if (objectPropsWithValues.containsKey(propName)) {
                    HashSet<String> crntValues = objectPropsWithValues.get(propName);
                    crntValues.addAll(values);
                    objectPropsWithValues.put(propName, crntValues);
                } else {
                    objectPropsWithValues.put(propName, values);
                }

                cnt = 0;
                values = new HashSet<>();
            }
        }
        //System.out.println(objectPropsWithValues);
        return objectPropsWithValues;
    }

    /**
     * Retrieves all datatype properties with their associated values of the
     * specified individual.
     *
     * @param individual
     * @return
     * @throws RepositoryException
     * @throws MalformedQueryException
     * @throws QueryEvaluationException
     */
    public HashMap<String, HashSet<String>> getAllDataTypePropertiesWithValuesOf(String individual) throws RepositoryException, MalformedQueryException, QueryEvaluationException {
        String getAllDataTypePropertiesWithValuesOfAsStrings = "SELECT ?p ?o\n"
                + "WHERE {\n"
                + "<" + individual + "> ?p ?o .\n"
                + "?p  a  owl:DatatypeProperty }\n";

        HashSet<ArrayList<String>> answerSet = queryRepo(getAllDataTypePropertiesWithValuesOfAsStrings);
        //System.out.println(answerSet);

        HashMap<String, HashSet<String>> dataTypePropsWithValues = new HashMap<>();
        HashSet<String> values = new HashSet<>();
        int cnt = 0;
        if (answerSet != null) {
            for (ArrayList<String> ans : answerSet) {
                String propName = "";
                for (String value : ans) {
                    if (cnt == 0) {
                        propName = value;
                        cnt++;
                    } else {
                        values.add(value);
                    }
                }
                if (dataTypePropsWithValues.containsKey(propName)) {
                    HashSet<String> crntValues = dataTypePropsWithValues.get(propName);
                    crntValues.addAll(values);
                    dataTypePropsWithValues.put(propName, crntValues);
                } else {
                    dataTypePropsWithValues.put(propName, values);
                }
                cnt = 0;
                values = new HashSet<>();
            }
        }
        return dataTypePropsWithValues;
    }

    /**
     * Retrieves all datatype properties with their associated values of the
     * specified individual when not having the property declarations.
     *
     * @param individual
     * @return
     * @throws RepositoryException
     * @throws MalformedQueryException
     * @throws QueryEvaluationException
     */
    public HashMap<String, HashSet<String>> getAllUndeclaredDataTypePropertiesWithValuesOf(String individual) throws RepositoryException, MalformedQueryException, QueryEvaluationException {
        String getAllUndeclaredDataTypePropertiesWithValuesOfAsStrings = "SELECT ?p ?o\n"
                + "WHERE {\n"
                + "<" + individual + "> ?p ?o .\n"
                + "filter isLiteral(?o) }\n";

        HashSet<ArrayList<String>> answerSet = queryRepo(getAllUndeclaredDataTypePropertiesWithValuesOfAsStrings);
        //System.out.println(answerSet);

        HashMap<String, HashSet<String>> dataTypePropsWithValues = new HashMap<>();
        HashSet<String> values = new HashSet<>();
        int cnt = 0;
        if (answerSet != null) {
            for (ArrayList<String> ans : answerSet) {
                String propName = "";
                for (String value : ans) {
                    if (cnt == 0) {
                        propName = value;
                        cnt++;
                    } else {
                        values.add(value);
                    }
                }
                if (dataTypePropsWithValues.containsKey(propName)) {
                    HashSet<String> crntValues = dataTypePropsWithValues.get(propName);
                    crntValues.addAll(values);
                    dataTypePropsWithValues.put(propName, crntValues);
                } else {
                    dataTypePropsWithValues.put(propName, values);
                }
                cnt = 0;
                values = new HashSet<>();
            }
        }
        return dataTypePropsWithValues;
    }

    /**
     * Retrieves all neighbors of the specified individual.
     *
     * @param individual
     * @return
     * @throws RepositoryException
     * @throws MalformedQueryException
     * @throws QueryEvaluationException
     */
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

    /**
     * Retrieves all individuals.
     *
     * @return
     * @throws RepositoryException
     * @throws MalformedQueryException
     * @throws QueryEvaluationException
     */
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

    /**
     * Retrieves all individuals of the specified type.
     *
     * @param prefix
     * @param type
     * @return
     * @throws RepositoryException
     * @throws MalformedQueryException
     * @throws QueryEvaluationException
     */
    public HashSet<ArrayList<String>> getAllIndividualsOfType(String prefix, String type) throws RepositoryException, MalformedQueryException, QueryEvaluationException {

        String getAllIndividualsOfTypeAsString = "SELECT ?individual\n"
                + "WHERE { \n"
                + "    ?individual rdf:type " + prefix + ":" + type + ".\n"
                + "}\n";

        HashSet<ArrayList<String>> answerSet = queryRepo(getAllIndividualsOfTypeAsString);

        return answerSet;
    }

    /**
     * Retrieves all subjects of the input type.
     *
     * @param prefix
     * @param type
     * @return
     * @throws RepositoryException
     * @throws MalformedQueryException
     * @throws QueryEvaluationException
     */
    public HashSet<Subject> getAllSubjectsOfType(String prefix, String type) throws RepositoryException, MalformedQueryException, QueryEvaluationException {

        String getAllSubjectsOfTypeAsString = "SELECT ?sub\n"
                + "WHERE { \n"
                + "    ?sub rdf:type " + prefix + ":" + type + ".\n"
                + "}\n";

        HashSet<ArrayList<String>> answerSet = queryRepo(getAllSubjectsOfTypeAsString);
        HashSet<Subject> subjects = new HashSet<>();

        for (ArrayList<String> ans : answerSet) {
            for (String sbj : ans) {
                Subject subject = new Subject();
                subject.setUri(sbj);
                subject.setLabel(getLabel(sbj));
                subject.setRdfTypes(getRdfTypes(sbj));
                subject.setRdfTypesLabels(getRdfTypesLabels(sbj));
                subject.setUndeclaredDataTypePropsWithValues(getAllUndeclaredDataTypePropertiesWithValuesOf(subject.getUri()));
                subject.setDataTypePropsWithValues(getAllDataTypePropertiesWithValuesOf(subject.getUri()));
                subject.setObjectPropsWithObjectValues(getAllObjectPropertiesWithObjectValuesOf(subject.getUri()));
                subjects.add(subject);
            }
        }

        //System.out.println(subjects);
        return subjects;
    }

    /**
     * Retrieves all subjects of the input type.
     *
     * @param prefix
     * @param type
     * @return
     * @throws RepositoryException
     * @throws MalformedQueryException
     * @throws QueryEvaluationException
     */
    public HashSet<Subject> getAllSubjectsOfTypeWithURIs(String prefix, String type, ArrayList<String> uris) throws RepositoryException, MalformedQueryException, QueryEvaluationException {

        String filterTemplate = "FILTER (";
        int cnt = 0;

        for (String uri : uris) {

            filterTemplate += "regex(STR(?sub), '" + uri + "')";
            cnt++;

            if (cnt < uris.size()) {
                filterTemplate += " || ";
            } else {
                filterTemplate += ").\n";
            }
        }

        String getAllSubjectsOfTypeAsString = "SELECT ?sub\n"
                + "WHERE { \n"
                + "    ?sub rdf:type " + prefix + ":" + type + ".\n"
                + filterTemplate
                + "}\n";

        HashSet<ArrayList<String>> answerSet = queryRepo(getAllSubjectsOfTypeAsString);
        HashSet<Subject> subjects = new HashSet<>();

        for (ArrayList<String> ans : answerSet) {
            for (String sbj : ans) {
                Subject subject = new Subject();
                subject.setUri(sbj);
                subject.setLabel(getLabel(sbj));
                subject.setRdfTypes(getRdfTypes(sbj));
                subject.setRdfTypesLabels(getRdfTypesLabels(sbj));
                subject.setUndeclaredDataTypePropsWithValues(getAllUndeclaredDataTypePropertiesWithValuesOf(subject.getUri()));
                subject.setDataTypePropsWithValues(getAllDataTypePropertiesWithValuesOf(subject.getUri()));
                subject.setObjectPropsWithObjectValues(getAllObjectPropertiesWithObjectValuesOf(subject.getUri()));
                subjects.add(subject);
            }
        }

        //System.out.println(subjects);
        return subjects;
    }

    /**
     * Retrieves the label of the input URI.
     *
     * @param uri
     * @return
     * @throws RepositoryException
     * @throws MalformedQueryException
     * @throws QueryEvaluationException
     */
    public String getLabel(String uri) throws RepositoryException, MalformedQueryException, QueryEvaluationException {
        //Cannot work yet, we need to add labels to the resources.
//        String getLabelAsString
//                = "select distinct ?label where {\n"
//                + "<" + uri + "> rdfs:label ?label. \n"
//                + "}";
//
//        HashSet<ArrayList<String>> answerSet = queryRepo(getLabelAsString);
//        System.out.println(answerSet);
        String[] uriParts = uri.split("/");
        String[] location = uriParts[uriParts.length - 1].split("#");
        String label = location[location.length - 1];
        return label;
    }

    /**
     * Retrieves all labels of parent classes of the input URI.
     *
     * @param uri
     * @return
     * @throws RepositoryException
     * @throws QueryEvaluationException
     */
    public HashSet<String> getRdfTypesLabels(String uri) throws RepositoryException, QueryEvaluationException {
        String getRdfTypeAsString
                = "select distinct ?class where {\n"
                + "<" + uri + "> rdf:type ?class. \n"
                + "}";

        HashSet<ArrayList<String>> answerSet;
        try {
            answerSet = queryRepo(getRdfTypeAsString);
        } catch (MalformedQueryException ex) {
            answerSet = null;
            Logger.getLogger(QAInfoBase.class.getName()).log(Level.SEVERE, null, ex);
        }

        HashSet<String> typesLabels = new HashSet<>();
        if (answerSet != null) {
            for (ArrayList<String> ans : answerSet) {
                for (String type : ans) {
                    try {
                        typesLabels.add(getLabel(type));
                    } catch (MalformedQueryException ex) {
                        typesLabels.add(null);
                        Logger.getLogger(QAInfoBase.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }
        //System.out.println(uri);
        //System.out.println(types);

        return typesLabels;
    }

    /**
     * Retrieves all parent classes of the input URI.
     *
     * @param uri
     * @return
     * @throws RepositoryException
     * @throws QueryEvaluationException
     */
    public HashSet<String> getRdfTypes(String uri) throws RepositoryException, QueryEvaluationException {
        String getRdfTypeAsString
                = "select distinct ?class where {\n"
                + "<" + uri + "> rdf:type ?class. \n"
                + "}";

        HashSet<ArrayList<String>> answerSet;
        try {
            answerSet = queryRepo(getRdfTypeAsString);
        } catch (MalformedQueryException ex) {
            answerSet = null;
            Logger.getLogger(QAInfoBase.class.getName()).log(Level.SEVERE, null, ex);
        }

        HashSet<String> types = new HashSet<>();
        if (answerSet != null) {
            for (ArrayList<String> ans : answerSet) {
                for (String type : ans) {
                    types.add(type);
                }
            }
        }
        //System.out.println(uri);
        //System.out.println(types);

        return types;
    }

    /**
     * Retrieves all subjects of the repository.
     *
     * @return
     * @throws RepositoryException
     * @throws MalformedQueryException
     * @throws QueryEvaluationException
     */
    public HashSet<Subject> getAllSubjects() throws RepositoryException, MalformedQueryException, QueryEvaluationException {

        String getAllSubjectsAsString
                = "select distinct ?s where {\n"
                + "?s ?p ?o. \n"
                + "minus {?s rdf:type rdf:Property.}\n"
                + "}";

        HashSet<ArrayList<String>> answerSet = queryRepo(getAllSubjectsAsString);

        HashSet<Subject> subjects = new HashSet<>();

        for (ArrayList<String> ans : answerSet) {
            for (String sbj : ans) {
                try {
                    Subject subject = new Subject();
                    subject.setUri(sbj);
                    subject.setLabel(getLabel(sbj));
                    subject.setRdfTypes(getRdfTypes(sbj));
                    subject.setRdfTypesLabels(getRdfTypesLabels(sbj));
                    subject.setDataTypePropsWithValues(getAllDataTypePropertiesWithValuesOf(subject.getUri()));
                    subject.setObjectPropsWithObjectValues(getAllObjectPropertiesWithObjectValuesOf(subject.getUri()));
                    subjects.add(subject);
                } catch (Exception e) {
                    continue;
                }
            }
        }

        //System.out.println(subjects);
        return subjects;
    }

    public HashSet<ArrayList<String>> getCommentsOnFocus(ArrayList<String> uris) throws RepositoryException, MalformedQueryException, QueryEvaluationException {

        String filterFunc = "filter( ?h_id in (";

        //for(String uri : uris){
        for (int i = 0; i < uris.size(); i++) {

            filterFunc += "<" + uris.get(i) + ">";

            if (i == uris.size() - 1) {
                filterFunc += ")).";
            } else {
                filterFunc += ",";
            }
        }

        String getCommentsOnFocus
                = "select ?h_id ?c_id ?c_t ?c_d where{" + "\n"
                + "?h_id a owl:NamedIndividual." + "\n"
                + "?h_id hip:hasReview ?c_id." + "\n"
                + "?c_id a hip:review." + "\n"
                + "?c_id hip:hasText ?c_t." + "\n"
                + "?c_id hip:hasDate ?c_d." + "\n"
                + filterFunc
                + "}";

        //System.out.println(getCommentsOnFocus);

        HashSet<ArrayList<String>> commentTuples = queryRepo(getCommentsOnFocus);

//        for (ArrayList<String> commentTuple : commentTuples) {
//            System.out.println("0" + commentTuple.get(0));
//            System.out.println("1" + commentTuple.get(1));
//            System.out.println("2" + commentTuple.get(2));
//            System.out.println("3" + commentTuple.get(3));
//            System.out.println("");
//        }

        //System.out.println(answerSetUris.size());

        return commentTuples;

    }
    /**
     * Retrieves all objects of the repository.
     *
     * @return
     * @throws RepositoryException
     * @throws MalformedQueryException
     * @throws QueryEvaluationException
     */
    public HashSet<Object> getAllObjects() throws RepositoryException, MalformedQueryException, QueryEvaluationException {

        String getAllObjectsUrisAsString
                = "select distinct ?o where {\n"
                + "?s ?p ?o. \n"
                + "minus {?s rdf:type rdf:Property.}\n"
                + "}";

//        String getAllObjectLiteralsAsString
//                = "select distinct ?literal {\n"
//                + "?s ?p ?literal.\n"
//                + "filter isLiteral(?literal)\n"
//                + "}";
        HashSet<ArrayList<String>> answerSetUris = queryRepo(getAllObjectsUrisAsString);
        //HashSet<ArrayList<String>> answerSetLiterals = queryRepo(getAllObjectLiteralsAsString);

        HashSet<Object> objects = new HashSet<>();

        for (ArrayList<String> ansObj : answerSetUris) {
            for (String obj : ansObj) {
                ObjectUri objectUri = new ObjectUri();
                objectUri.setUri(obj);
                objectUri.setLabel(getLabel(obj));
                objectUri.setRdfTypes(getRdfTypes(obj));
                objectUri.setRdfTypesLabels(getRdfTypesLabels(obj));
                objects.add(objectUri);

            }
        }

//        for (ArrayList<String> ansLit : answerSetLiterals) {
//            for (String obj : ansLit) {
//                System.out.println(obj);
//                ObjectLiteral objectLit = new ObjectLiteral();
//                objectLit.setXsdValue(obj);
//                objectLit.setXsdType(null);
//                objectLit.setXsdValueLabel(null);
//                objects.add(objectLit);
//            }
//        }
        //System.out.println(subjects);
        return objects;
    }

    /*
    public HashSet<ArrayList<String>> getAllClasses() throws RepositoryException, MalformedQueryException, QueryEvaluationException {
        HashSet<ArrayList<String>> answerSet = queryRepo(getAllClasses);
        return answerSet;
    }
     */
    public static void main(String[] args) throws RepositoryException, MalformedQueryException, QueryEvaluationException, IOException {
        QAInfoBase KB = new QAInfoBase();
        //String prefix = "csdT";
        //String type = "Course";

        ArrayList<String> uris = new ArrayList<>();

        uris.add("http://ics.forth.gr/isl/hippalus/#tazuru");
        uris.add("http://ics.forth.gr/isl/hippalus/#arima_onsen_tosen_goshobo");
        uris.add("http://ics.forth.gr/isl/hippalus/#the_b_kobe");

        System.out.println(KB.getCommentsOnFocus(uris));

        //System.out.println(KB.getAllSubjectsOfTypeWithURIs("owl", "NamedIndividual", uris));
        //HashSet<ArrayList<String>> individualsOfTypeX = KB.getAllIndividualsOfType(prefix, type);
        //KB.printAnswer(individualsOfTypeX);
        //ArrayList<Individual> individuals = KB.getAllIndividuals();
        //System.out.println(individuals);
        //KB.printAnswer(individuals);
        //HashSet<Subject> allSubjects = KB.getAllSubjects();
        //System.out.println(allSubjects);
        //System.out.println(KB.getAllObjectPropertiesWithObjectValuesOf("http://ics.forth.gr/isl/hippalus/#airi_hotel"));
        //System.out.println(KB.getAllDataTypePropertiesWithValuesOf("http://ics.forth.gr/isl/hippalus/#airi_hotel"));
        //HashSet<Object> allObjects = KB.getAllObjects();
        //System.out.println(allObjects);
        //HashSet<Subject> allSubjectsOfType = KB.getAllSubjectsOfType("csdP", "AcademicStaff");
        //System.out.println(allSubjectsOfType);

        //String cs252 = "http://www.csd.uoc.gr/CS-252";
        //String wrongIndi = "lalalala";
        //Individual instance = new Individual(cs252);
        //ArrayList<Neighbor> neighbors = KB.getNeighborsOf(instance.getURI().toString());
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
