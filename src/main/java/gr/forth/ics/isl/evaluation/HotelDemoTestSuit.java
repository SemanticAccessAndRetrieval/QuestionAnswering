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

import com.crtomirmajer.wmd4j.WordMovers;
import edu.mit.jwi.Dictionary;
import edu.mit.jwi.IDictionary;
import gr.forth.ics.isl.auxiliaryClasses.Timer;
import gr.forth.ics.isl.evaluation.models.EvaluationComment;
import gr.forth.ics.isl.evaluation.models.EvaluationPair;
import gr.forth.ics.isl.evaluation.models.EvaluationQuery;
import gr.forth.ics.isl.evaluation.models.ModelHyperparameters;
import static gr.forth.ics.isl.main.demo_main.getCommentsFromTextOnlyKB;
import static gr.forth.ics.isl.main.demo_main.getTopKComments;
import gr.forth.ics.isl.nlp.models.Comment;
import gr.forth.ics.isl.sailInfoBase.QAInfoBase;
import gr.forth.ics.isl.sailInfoBase.models.Subject;
import gr.forth.ics.isl.utilities.StringUtils;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import mitos.stemmer.trie.Trie;
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.word2vec.Word2Vec;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.RepositoryException;

/**
 *
 * @author Sgo
 */
public class HotelDemoTestSuit {

    //Number of top comments to retrieve
    static int topK = 2000;//71;
    static String evalCollection = "hotelsTestCollectionB.csv";

    //The paths for the stopWords Files.
    public static String filePath_en = "src/main/resources/stoplists/stopwordsEn.txt";
    public static String filePath_gr = "src/main/resources/stoplists/stopwordsGr.txt";

    //A hashMap that contains two Trie, one for the English stopList and one for the Greek
    public static HashMap<String, Trie> stopLists = new HashMap<>();

    public static void main(String[] args) throws IOException, RepositoryException, MalformedQueryException, QueryEvaluationException {
        Timer overallTimer = new Timer();
        overallTimer.start();
        //Intance of class timer, for time measurements
        Timer timer = new Timer();

        // Create the list of stopWords to use
        StringUtils.generateStopLists(filePath_en, filePath_gr);

        // Create Word2Vec model
        File gModel = new File("C:/Users/Sgo/Desktop/Developer/Vector Models/GoogleNews-vectors-negative300.bin.gz");

        timer.start();
        Word2Vec vec = WordVectorSerializer.readWord2VecModel(gModel);
        WordMovers wm = WordMovers.Builder().wordVectors(vec).build();
        timer.end();
        long word2vecTime = timer.getTotalTime();
        System.out.println("Time to load word2vec: " + word2vecTime);

        timer.start();
        // Create WodNet Dictionary
        String wnhome = System.getenv("WNHOME");
        String path = wnhome + File.separator + "dict";
        URL url = new URL("file", null, path);
        // construct the dictionary object and open it
        IDictionary dict = new Dictionary(url);
        // open WordNet dictionary
        dict.open();
        timer.end();
        long wordNetTime = timer.getTotalTime();
        System.out.println("Time to load WordNet: " + wordNetTime);

        timer.start();
        // Create hotel database
        QAInfoBase KB = new QAInfoBase();

        // Retrieve hotels
        // Uncomment this for hybrid data set
        //HashSet<Subject> hotels = KB.getAllSubjectsOfType("hippalus", "hippalusID");
        // Uncomment this for reviews only data set
        HashSet<Subject> reviews = KB.getAllSubjectsOfType("hip", "review");
        timer.end();
        long resourcesTime = timer.getTotalTime();
        System.out.println("Time to load resources: " + resourcesTime);

        System.out.println("External Resources were loaded successfully");
        // Create a List of queries
        ArrayList<String> queryList = new ArrayList<>();
        queryList.add("Has anyone reported a problem about noise?");
        queryList.add("Is this hotel quiet?");

        // Get all the comments
        //HashMap<String, Comment> comments = getCommentsAsMap(hotels, KB);
        // uncomment this for hybrid data set
        //ArrayList<Comment> comments = getComments(reviews, KB);
        // uncomment this for revies only data set
        ArrayList<Comment> comments = getCommentsFromTextOnlyKB(reviews);
        // This structure will contain all hyperparams along with the
        // R_Precision that they achieved
        ArrayList<ModelHyperparameters> allModelsWithRPrecision = new ArrayList<>();
        // This structure will contain all hyperparams along with the
        // AveP that they achieved
        ArrayList<ModelHyperparameters> allModelsWithAVEP = new ArrayList<>();
        // This structure will contain all hyperparams along with the
        // BPREF that they achieved
        ArrayList<ModelHyperparameters> allModelsWithBPREF = new ArrayList<>();

        // This structure will contain the ground truth relevance between each
        // query and each comment
        HashMap<String, HashMap<String, EvaluationPair>> gt = new HashMap<>();
        gt = readEvaluationSet(evalCollection);

        System.out.println(gt.get("q1").size());
        System.out.println(gt.get("q2").size());
        //uncomment this for train weights
        //===================================================================================
        /*for (float word2vec_w = 0.0f; word2vec_w <= 1.0f; word2vec_w = word2vec_w + 0.1f) {
            for (float wordNet_w = 0.0f; wordNet_w <= 1.0f; wordNet_w = wordNet_w + 0.1f) {

                word2vec_w = Math.round(word2vec_w * 10.0f) / 10.0f;
                wordNet_w = Math.round(wordNet_w * 10.0f) / 10.0f;

                if (word2vec_w + wordNet_w != 1.0f) {
                    continue;
                }*/
        //===================================================================================
        //for (float threshold = 0.00f; threshold <= 1.00f; threshold = threshold + 0.01f) {
        //threshold = Math.round(threshold * 100.0f) / 100.0f;
        //if (threshold == 0.0f) {
        //    continue;
        //}

        float word2vec_w = 0.3f;
        float wordNet_w = 0.7f;

        int R = 0; // default R (num of relevants for each query)
        int R33 = 0;
        int R2 = 0;
        int cnt = 1; // query counter

        double R_Precision_33 = 0.0; // 33-Precision
        double R_Precision_2 = 0.0; // 2-Precision

        double R_Precision = 0.0; // R-Precision
        double AVEP = 0.0; // Avep
        double BPREF = 0.0; // Bpref

        ArrayList<Integer> resultSet = new ArrayList<>(); // binary relevance calculated for  query
        ArrayList<Integer> testSet = new ArrayList<>(); // true binary relevance for a query
        ArrayList<Double> crntCompRelevance = new ArrayList<>(); // floating point relevance calculated for a query

        ArrayList<Integer> wholeTestSet = new ArrayList<>(); // true binary relevance for all queries
        ArrayList<Double> wholeScoreSet = new ArrayList<>(); // floating point relevance calculated for all queries


        //for each query
        while (cnt <= queryList.size()) {

            // Get the ground truth for the current query
            HashMap<String, EvaluationPair> evalPairsWithCrntQueryId = gt.get("q" + cnt);

            // Set R parameters of evaluation metrics
            R33 = 33;
            R2 = 2;
            R = getNumOfRels(evalPairsWithCrntQueryId);
            System.out.println(R);

            // init result and test set for the current query
            resultSet = new ArrayList<>();
            testSet = new ArrayList<>();

            //Get the user's question
            String question = queryList.get(cnt - 1);

            System.out.println("========================");

            //Print the weights for the scoring
            System.out.println("word2vec weight: " + word2vec_w);
            System.out.println("wordNet weight: " + wordNet_w);

            //Get the threshold for relevancy
            //System.out.println("threshold: " + threshold);
            timer.start();

            //Retrieve the relevant comments
            ArrayList<Comment> rankedComments = getTopkRelevantComments(comments, word2vec_w, wordNet_w, question, wm, vec, dict);

            timer.end();
            long calculateAllScores = timer.getTotalTime();
            System.out.println("Average Time to calculate score (for all reviews): " + calculateAllScores);
            System.out.println("Average Time to calculate score (for one review): " + calculateAllScores / comments.size());

            // for all retrieved comments
            for (Comment resultCom : rankedComments) {
                // keep truck of comment's true and calculated relevance value
                // if comment is unjudged skip it
                EvaluationPair p = evalPairsWithCrntQueryId.get(resultCom.getId());
                if (p != null) {
                    resultSet.add(1); // estimated binary relevance
                    testSet.add(p.getRelevance()); // true binarry relevance

//                    if (p.getRelevance() == 1) {
//                        R++;
//                    }
                    System.out.println(p.getRelevance() + ", " + resultCom.getId());

                    //System.out.println(p.getComment().getId() + " -- " + resultCom.getId());
                    // keep truck of all (ci,qi) pairs relevance
                    wholeTestSet.add(p.getRelevance());
                    wholeScoreSet.add(resultCom.getScore());
                    crntCompRelevance.add(resultCom.getScore());
                }
            }

            System.out.println(testSet);
            cnt++; // go to the next query

            // Calculate the R_Precision of our system's answer
            R_Precision_33 += EvaluationMetrics.R_Precision(resultSet, testSet, R33);
            R_Precision_2 += EvaluationMetrics.R_Precision(resultSet, testSet, R2);
            R_Precision += EvaluationMetrics.R_Precision(resultSet, testSet, R);
            // Calculate the AveP of our system's answer
            AVEP += EvaluationMetrics.AVEP(resultSet, testSet, R);
            // Calculate the BPREF of our system's answer
            BPREF += EvaluationMetrics.BPREF(resultSet, testSet, R);
        }

        // Calculate mean BPREF, R_Precision and AveP for all queries
        R_Precision_33 /= queryList.size();
        R_Precision_2 /= queryList.size();
        R_Precision /= queryList.size();
        AVEP /= queryList.size();
        BPREF /= queryList.size();

        // Print the results
        System.out.println("R_Precision_33: " + R_Precision_33);
        System.out.println("R_Precision_2: " + R_Precision_2);
        System.out.println("R_Precision_default: " + R_Precision);
        System.out.println("AVEP: " + AVEP);
        System.out.println("BPREF: " + BPREF);
        System.out.println("TrueSet: " + wholeTestSet);
        //System.out.println("ResultSet: " + resultSet);
        System.out.println("ComputedRelevance: " + wholeScoreSet);
        System.out.println("TrueSet size: " + wholeTestSet.size());
        System.out.println("ComputedRelevance size: " + wholeScoreSet.size());
        System.out.println("========================");

        // Create current models and store them their associated structures
        ModelHyperparameters crntModelRPrecision = new ModelHyperparameters(R_Precision, word2vec_w, wordNet_w);
        crntModelRPrecision.setR_Precision(R_Precision);
        crntModelRPrecision.setAveP(AVEP);
        crntModelRPrecision.setBPREF(BPREF);
        crntModelRPrecision.setTrueSet(wholeTestSet);
        crntModelRPrecision.setResultSet(resultSet);
        crntModelRPrecision.setScoreSet(wholeScoreSet);
        ModelHyperparameters crntModelAVEP = new ModelHyperparameters(AVEP, word2vec_w, wordNet_w);
        crntModelAVEP.setR_Precision(R_Precision);
        crntModelAVEP.setAveP(AVEP);
        crntModelAVEP.setBPREF(BPREF);
        crntModelAVEP.setTrueSet(wholeTestSet);
        crntModelAVEP.setResultSet(resultSet);
        crntModelAVEP.setScoreSet(wholeScoreSet);
        ModelHyperparameters crntModelBPREF = new ModelHyperparameters(BPREF, word2vec_w, wordNet_w);
        crntModelBPREF.setR_Precision(R_Precision);
        crntModelBPREF.setAveP(AVEP);
        crntModelBPREF.setBPREF(BPREF);
        crntModelBPREF.setTrueSet(wholeTestSet);
        crntModelBPREF.setResultSet(resultSet);
        crntModelBPREF.setScoreSet(wholeScoreSet);

        allModelsWithRPrecision.add(crntModelRPrecision);
        allModelsWithAVEP.add(crntModelAVEP);
        allModelsWithBPREF.add(crntModelBPREF);

        //}
        //}
        //}
        // Save and print the best models
        // uncomment the saveObject command when train weights in order to override previously best model
        if (!allModelsWithRPrecision.isEmpty()) {
            System.out.println("==== Best model hyperparams and perfomance ====");
            System.out.println("==== Based on R_Precision ====");
            System.out.println(allModelsWithRPrecision.size());
            //Utils.saveObject(Collections.max(allModelsWithRPrecision), "rPrecisionBased_BestModel");
            System.out.println(Collections.max(allModelsWithRPrecision));
            System.out.println("======= Based on AVEP ========");
            System.out.println(allModelsWithAVEP.size());
            //Utils.saveObject(Collections.max(allModelsWithAVEP), "AVEPbased_BestModel");
            System.out.println(Collections.max(allModelsWithAVEP));
            System.out.println("======= Based on BPREF ========");
            System.out.println(allModelsWithBPREF.size());
            //Utils.saveObject(Collections.max(allModelsWithBPREF), "BPREFbased_BestModel");
            System.out.println(Collections.max(allModelsWithBPREF));
        }
        overallTimer.end();
        long overallExpTime = overallTimer.getTotalTime();
        System.out.println("Total time for test suit to run: " + overallExpTime);
    }

    /**
     * This method computes and return the number of relevant comments for a
     * specified query.
     *
     * @param evalPairsWithQueryId
     * @return
     */
    public static int getNumOfRels(HashMap<String, EvaluationPair> evalPairsWithQueryId) {
        int R = 0;
        for (EvaluationPair ep : evalPairsWithQueryId.values()) {
            if (ep.getRelevance() == 1) {
                R++;
            }
        }
        return R;
    }

    /**
     * This method computes and returns the top-K most relevant comments based
     * on our system.
     *
     * @param comments
     * @param word2vec_w
     * @param wordNet_w
     * @param question
     * @param wm
     * @param vec
     * @param dict
     * @return
     * @throws IOException
     */
    public static ArrayList<Comment> getTopkRelevantComments(ArrayList<Comment> comments, float word2vec_w, float wordNet_w, String question, WordMovers wm, Word2Vec vec, IDictionary dict) throws IOException {
        double max_dist = Double.MIN_VALUE;
        //Calculate score for each comment
        //Also calculate max word mover distance
        for (Comment com : comments) {
            //Comment com = comments.get(comId);
            if (word2vec_w == 1.0f && wordNet_w == 0.0f) {
                com.calculateWord2Score(wm, question, vec);
            } else if (word2vec_w == 0.0f && wordNet_w == 1.0f) {
                com.calculateWordNetScore(question, dict);
            } else {
                com.calculateScores(wm, question, vec, dict, word2vec_w, wordNet_w);
            }

            //com.calculateScores(question, dict, wordNet_w);
            if (com.getWordScore() >= max_dist) {
                max_dist = com.getWordScore();
            }
            // Get the best comments based on their score (currently all of them)
            //comments.put(comId, com);
        }

        if (word2vec_w != 0.0f && wordNet_w != 1.0f) {
            //Normalize WordMoverDistance, and update comments with the final scores
            for (Comment com : comments) {
                com.calculateWordScore(max_dist);
                com.calculateScore(word2vec_w, wordNet_w);
            }
        }

        ArrayList<Comment> rankedComments = getTopKComments(comments, topK);

        return rankedComments;
    }

    /**
     * This method is used to create our evaluation structure based on the
     * evaluation collection.
     *
     * @param fileName
     * @return HashMap<String, ArrayList<EvaluationPair>> groundTruth
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static HashMap<String, HashMap<String, EvaluationPair>> readEvaluationSet(String fileName) throws FileNotFoundException, IOException {
        //ArrayList<EvaluationPair> groundTruth = new ArrayList<>();
        HashMap<String, HashMap<String, EvaluationPair>> groundTruth = new HashMap<>();
        String line;
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
