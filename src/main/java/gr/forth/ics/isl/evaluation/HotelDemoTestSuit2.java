/*
 *  This code belongs to the Semantic Access and Retrieval (SAR) group of the
 *  Information Systems Laboratory (ISL) of the
 *  Institute of Computer Science (ICS) of the
 *  Foundation for Research and Technology - Hellas (FORTH)
 *  Nobody is allowed to use, copy, distribute, or modify this work.
 *  It is published for reasons of research results reproducibility.
 *  (c) 2017 Semantic Access and Retrieval group, All rights reserved
 */
package gr.forth.ics.isl.evaluation;

import gr.forth.ics.isl.auxiliaryClasses.Timer;
import static gr.forth.ics.isl.evaluation.HotelDemoTestSuit.topK;
import gr.forth.ics.isl.evaluation.models.EvaluationComment;
import gr.forth.ics.isl.evaluation.models.EvaluationPair;
import gr.forth.ics.isl.evaluation.models.EvaluationQuery;
import static gr.forth.ics.isl.main.demo_main.getComments;
import static gr.forth.ics.isl.main.demo_main.getTopKComments;
import gr.forth.ics.isl.nlp.models.Comment;
import gr.forth.ics.isl.sailInfoBase.QAInfoBase;
import gr.forth.ics.isl.sailInfoBase.models.Subject;
import gr.forth.ics.isl.utilities.StringUtils;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import mitos.stemmer.trie.Trie;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.RepositoryException;

/**
 *
 * @author Sgo
 */
public class HotelDemoTestSuit2 {
    //Number of top comments to retrieve

    //static int topK = 10;
    //The paths for the stopWords Files.
    public static String filePath_en = "src/main/resources/stoplists/stopwordsEn.txt";
    public static String filePath_gr = "src/main/resources/stoplists/stopwordsGr.txt";

    //A hashMap that contains two Trie, one for the English stopList and one for the Greek
    public static HashMap<String, Trie> stopLists = new HashMap<>();

    public static void main(String[] args) throws IOException, RepositoryException, MalformedQueryException, QueryEvaluationException {
        //Intance of class timer, for time measurements
        Timer timer = new Timer();

        // Create the list of stopWords to use
        StringUtils.generateStopLists(filePath_en, filePath_gr);
        /*
        // Create Word2Vec model
        File gModel = new File("C:/Users/Sgo/Desktop/Developer/Vector Models/GoogleNews-vectors-negative300.bin.gz");

        timer.start();
        Word2Vec vec = WordVectorSerializer.readWord2VecModel(gModel);
        WordMovers wm = WordMovers.Builder().wordVectors(vec).build();
        timer.end();
        long word2vecTime = timer.getTotalTime();
        System.out.println("Time to load word2vec: " + word2vecTime);

        // Create WodNet Dictionary
        String wnhome = System.getenv("WNHOME");
        String path = wnhome + File.separator + "dict";
        URL url = new URL("file", null, path);
        // construct the dictionary object and open it
        IDictionary dict = new Dictionary(url);
        // open WordNet dictionary
        dict.open();
         */
        // Create hotel database
        QAInfoBase KB = new QAInfoBase();

        timer.start();
        // Retrieve hotels
        HashSet<Subject> hotels = KB.getAllSubjectsOfType("hippalus", "hippalusID");
        timer.end();
        long resourcesTime = timer.getTotalTime();
        System.out.println("Time to load resources: " + resourcesTime);

        System.out.println("External Resources were loaded successfully");
        // Create a List of queries
        ArrayList<String> queryList = new ArrayList<>();
        queryList.add("Has anyone reported a problem about noise?");
        queryList.add("Is this hotel quiet?");

        // Get all the comments
        ArrayList<Comment> comments = getComments(hotels, KB);

        // This structure will contain the ground truth relevance between each
        // query and each comment
        HashMap<String, HashMap<String, EvaluationPair>> gt;
        gt = readEvaluationSet("hotelsTestCollectionA.csv");

        int cnt = 1;
        int maxIter = 1000;
        double R_Precision = 0.0;
        double AVEP = 0.0;
        double BPREF = 0.0;
        double meanR_Precision = 0.0;
        double meanAVEP = 0.0;
        double meanBPREF = 0.0;
        ArrayList<Integer> resultSet = new ArrayList<>();
        ArrayList<Integer> testSet = new ArrayList<>();

        for (int i = 0; i < maxIter; i++) {

            //for each query
            while (cnt <= queryList.size()) {
                //Get the user's question
                String question = queryList.get(cnt - 1);

                System.out.println("========================");

                timer.start();
                //Calculate score for each comment
                //Also calculate max word mover distance
                for (Comment com : comments) {

                    com.calculateBaseScore(question);

                }

                ArrayList<Comment> rankedComments = getTopKComments(comments, topK);

                for (Comment c : rankedComments) {
                    System.out.println(c.getText());
                    System.out.println(c.getScore());
                }

                timer.end();
                long calculateAllScores = timer.getTotalTime();
                System.out.println("Average Time to calculate score (for all reviews): " + calculateAllScores);
                System.out.println("Average Time to calculate score (for one review): " + calculateAllScores / comments.size());

                // Get the ground truth for the current query
                HashMap<String, EvaluationPair> evalPairsWithQueryId = gt.get("q" + cnt);
                //for (EvaluationPair p : evalPairsWithQueryId) {
                for (Comment resultCom : rankedComments) {
                    //Comment resultCom = comments.get(comId);
                    // for each comment in the ground truth collection
                    // get that comment from our database
                    //Comment resultCom = comments.get(p.getComment().getId());

                    // Choose weather it is relevant with the query or not
                    //if (resultCom.getScore() >= threshold) {
                    resultSet.add(1);
                    //} else {
                    //    resultSet.add(0);
                    //}

                    // keep truck of it's true relevance value
                    EvaluationPair p = evalPairsWithQueryId.get(resultCom.getId());
                    testSet.add(p.getRelevance());

                    System.out.println(p.getRelevance());
                    System.out.println(resultCom.getId());

                }

                // go to the next query
                cnt++;
                // Calculate the R_Precision of our system's answer
                R_Precision += EvaluationMetrics.R_Precision(resultSet, testSet, 10);
                // Calculate the AveP of our system's answer
                AVEP += EvaluationMetrics.AVEP(resultSet, testSet, 10);
                // Calculate the BPREF of our system's answer
                BPREF += EvaluationMetrics.BPREF(resultSet, testSet, 10);
            }

            // Calculate mean BPREF, R_Precision and AveP for all queries
            R_Precision /= queryList.size();
            AVEP /= queryList.size();
            BPREF /= queryList.size();

            meanR_Precision += R_Precision;
            meanAVEP += AVEP;
            meanBPREF += BPREF;

            R_Precision = 0.0;
            AVEP = 0.0;
            BPREF = 0.0;
        }

        meanR_Precision /= maxIter;
        meanAVEP /= maxIter;
        meanBPREF /= maxIter;

        // Print the results
        System.out.println("R_Precision: " + meanR_Precision);
        System.out.println("AVEP: " + meanAVEP);
        System.out.println("BPREF: " + meanBPREF);
        System.out.println("========================");

    }

    public static HashMap<String, HashMap<String, EvaluationPair>> readEvaluationSet(String fileName) throws FileNotFoundException, IOException {
        //ArrayList<EvaluationPair> groundTruth = new ArrayList<>();
        HashMap<String, HashMap<String, EvaluationPair>> groundTruth = new HashMap<>();
        String line = "";
        BufferedReader br = new BufferedReader(new FileReader("src/main/resources/evaluation/" + fileName));
        while ((line = br.readLine()) != null) {

            // use comma as separator
            String[] tuple = line.split(",");

            EvaluationQuery evalQuery = new EvaluationQuery(tuple[1]);
            EvaluationComment evalComment = new EvaluationComment(tuple[2], tuple[3]);
            EvaluationPair evalPair = new EvaluationPair(Integer.parseInt(tuple[0]), evalQuery, evalComment, Integer.parseInt(tuple[4]));

            HashMap<String, EvaluationPair> pairsWithQueryId = groundTruth.get(tuple[1]);
            if (pairsWithQueryId == null) {
                pairsWithQueryId = new HashMap<>();
                pairsWithQueryId.put(evalPair.getComment().getId(), evalPair);
                groundTruth.put(tuple[1], pairsWithQueryId);
            } else {
                pairsWithQueryId.put(evalPair.getComment().getId(), evalPair);
                groundTruth.put(tuple[1], pairsWithQueryId);
            }

        }

        return groundTruth;
    }
}
