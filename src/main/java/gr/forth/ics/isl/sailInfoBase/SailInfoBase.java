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

import com.bigdata.journal.Options;
import com.bigdata.rdf.sail.BigdataSail;
import com.bigdata.rdf.sail.BigdataSailRepository;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import org.openrdf.OpenRDFException;
import org.openrdf.model.Namespace;
import org.openrdf.query.Binding;
import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.rio.RDFFormat;

/**
 * This class represents the KB of the system. Allows automatized initialization 
 * of the KB and accepts sparql queries.
 * @author Sgo
 */
public class SailInfoBase {

    final Properties props; // infobase properties instance 
    final BigdataSail sail; // infobase instance
    final Repository repo; // repository instance
    static String NAMESPACE = ""; // prefixes of dataset

    
    /**
     * Constructor of SailInfoBase. It initializes the sail and repository
     * instances and properties that they have
     *
     * @throws RepositoryException
     * @throws IOException
     */
    public SailInfoBase() throws RepositoryException, IOException {
        props = new Properties();
        props.put(Options.BUFFER_MODE, "DiskRW"); // persistent file system located journal

        final File journalFile = File.createTempFile("bigdata", ".jnl"); // creates temporary journal file
        journalFile.deleteOnExit();
        props.setProperty(BigdataSail.Options.FILE, journalFile
                .getAbsolutePath());

        sail = new BigdataSail(props); // instantiate a sail with the defined properties
        repo = new BigdataSailRepository(sail); // create a Bigdata Sail repository

        repo.initialize();
        RepositoryConnection cxn = repo.getConnection(); // open connection with repository

        loadDataToRepo(cxn); // load the data set to the repository
        setNamespace(cxn); // set the appropriate prefixes of the dataset
        
    }

    /**
     * This method returns the data files that are about to be loaded in the
     * repo. The data files should be placed in src/main/resources/warehouse
     *
     * @return
     */
    public ArrayList<String> readData() {
        File folder = new File("src/main/resources/warehouse/");
        File[] listOfFiles = folder.listFiles();
        ArrayList<String> fileNames = new ArrayList<>();

        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile()) {
                fileNames.add(listOfFiles[i].getName());
                //System.out.println("File " + listOfFiles[i].getName() + " accepted");
            } else if (listOfFiles[i].isDirectory()) {
                System.out.println("Directory " + listOfFiles[i].getName());
            }
        }
        return fileNames;
    }

    /**
     * Loads data to from specified files to repository.
     *
     * @throws RepositoryException
     */
    public void loadDataToRepo(RepositoryConnection cxn) throws RepositoryException {

        try {
            // open repository connection
            //RepositoryConnection cxn = this.repo.getConnection();
            try {
                ArrayList<String> files = readData(); // get files from warehouse
                //System.out.println(files);
                String fileName = null; // name of the file
                String fileExtention = null; // extention of the file
                cxn.begin(); // start connection

                // for each file of the warehouse load the triples
                for (String file : files) {
                    String[] fileAsArray = file.split("\\.");
                    fileName = fileAsArray[0];
                    fileExtention = fileAsArray[1];

                    switch (fileExtention) {
                        case "ttl":
                            cxn.add(new File("src/main/resources/warehouse/" + fileName + "." + fileExtention), "base:", RDFFormat.TURTLE);
                            break;
                        case "rdf":
                            cxn.add(new File("src/main/resources/warehouse/" + fileName + "." + fileExtention), "base:", RDFFormat.RDFXML);
                            break;
                        case "nt":
                            cxn.add(new File("src/main/resources/warehouse/" + fileName + "." + fileExtention), "base:", RDFFormat.NTRIPLES);
                            break;
                        case "owl":
                            cxn.add(new File("src/main/resources/warehouse/" + fileName + "." + fileExtention), "base:", RDFFormat.RDFXML);
                            break;
                        default:
                            System.out.println(fileExtention + " is an unrecognized file extention.");
                            break;
                    }
                }

                cxn.commit(); // commit loaded triples to repo

            } catch (OpenRDFException ex) {
                cxn.rollback();
                throw ex;
            } finally {
                // close the repository connection
                cxn.close();
            }

        } catch (Exception e) {
            // if anything goes wrong shut down the repo.
            System.out.println("Shuting down repository");
            this.repo.shutDown();
        }
    }

    /**
     * Evaluate sparql query to repository. It first prepares the query via
     * prepareQuery method.
     *
     * @param namespace
     * @param query
     * @return
     * @throws RepositoryException
     * @throws MalformedQueryException
     * @throws QueryEvaluationException
     */
    public HashSet<ArrayList<String>> queryRepo(String query) throws RepositoryException, MalformedQueryException, QueryEvaluationException {

        HashSet<ArrayList<String>> answerSet = new HashSet<>(); // keeps all sets of answers

        query = prepareQuery(query); // prepare sparql query by setting the prefixes of the data set.

        // create conecction instance
        RepositoryConnection cxn = null;
        //evaluate sparql query
        try {
            // open connection
            if (this.repo instanceof BigdataSailRepository) {
                cxn = ((BigdataSailRepository) this.repo).getReadOnlyConnection();
            } else {
                cxn = this.repo.getConnection();
            }

            //System.out.println("Preparing query...");
            // prepare sparql query that is about to be sent
            final TupleQuery tupleQuery = cxn
                    .prepareTupleQuery(QueryLanguage.SPARQL,
                            query);

            //System.out.println("Evaluating query...");
            // evaluate query
            TupleQueryResult result = tupleQuery.evaluate();

            // parse result set
            if (result.hasNext()) {
                try {
                    //System.out.println("Iterating answer set...");
                    while (result.hasNext()) {
                        ArrayList<String> crntAns = new ArrayList<>();
                        BindingSet bindingSet = result.next();
                        Iterator<Binding> itr = bindingSet.iterator();
                        while (itr.hasNext()) {
                            Binding binding = itr.next();
                            //String[] resourceWithPrefix = binding.getValue().stringValue().split("/");
                            //String resource = resourceWithPrefix[resourceWithPrefix.length - 1];
                            //System.out.println(resource);
                            crntAns.add(binding.getValue().stringValue()); // add result for a specific binding sparql variable
                        }
                        answerSet.add(crntAns); // add result set for all binding sparql vars in the answerset

                    }
                } finally {
                    result.close();
                }

                return answerSet; // return the set of all available answers
            }

        } finally {
            // close the repository connection
            cxn.close();
        }
        // if everything has failed return null
        return null;
    }

    /**
     * Prepares the query to be evaluated over the infobase.
     *
     * @param namespace
     * @param query
     * @return
     */
    public String prepareQuery(String query) {
        query = NAMESPACE + query;
        return query;
    }

    /**
     * Prints the result of the sparql query.
     *
     * @param result
     * @throws QueryEvaluationException
     */
    public void printAnswer(HashSet<ArrayList<String>> ansSet) throws QueryEvaluationException {
        for (ArrayList<String> crntAns : ansSet) {
            System.out.println(crntAns);
        }
    }

    public void setNamespace(RepositoryConnection cxn) throws RepositoryException {
        //RepositoryConnection cxn = ((BigdataSailRepository) this.repo).getReadOnlyConnection();
        RepositoryResult<Namespace> namespaces = cxn.getNamespaces();
                while (namespaces.hasNext()) {
                    Namespace namespace = namespaces.next();
                    String prefix = namespace.getPrefix();
                    String name = namespace.getName();
                    NAMESPACE += "prefix " + prefix + ": <" + name + "> ";
                }
    }

    public static void main(String[] args) throws RepositoryException, QueryEvaluationException, MalformedQueryException, IOException {

        // Initialize KB
        System.out.println("Loading data...");
        SailInfoBase IB = new SailInfoBase();
        System.out.println("Loading completed");

        // String representation of query to be asked in KB
        String q1 = "select  ?p ?x ?z "
                + "where {"
                + "?p a csdT:Course  ."
                + "?p csdT:academicYear  ?x ."
                + "optional{"
                + "?p csdT:courseRequired  ?z ."
                + "}"
                + "}order by asc(?x)";

        System.out.println("Evaluating Query...");
        HashSet<ArrayList<String>> q1Ans = IB.queryRepo(q1); // Evaluate query
        IB.printAnswer(q1Ans); // print answer

    }

    public Repository getRepo() {
        return this.repo;
    }
}
