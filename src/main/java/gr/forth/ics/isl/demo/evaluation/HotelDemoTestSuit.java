/*
 *  This code belongs to the Semantic Access and Retrieval (SAR) group of the
 *  Information Systems Laboratory (ISL) of the
 *  Institute of Computer Science (ICS) of the
 *  Foundation for Research and Technology - Hellas (FORTH)
 *  Nobody is allowed to use, copy, distribute, or modify this work.
 *  It is published for reasons of research results reproducibility.
 *  (c) 2017 Semantic Access and Retrieval group, All rights reserved
 */
package gr.forth.ics.isl.demo.evaluation;

import com.crtomirmajer.wmd4j.WordMovers;
import edu.mit.jwi.Dictionary;
import edu.mit.jwi.IDictionary;
import gr.forth.ics.isl.demo.auxiliaryClasses.Timer;
import gr.forth.ics.isl.demo.evaluation.models.EvaluationComment;
import gr.forth.ics.isl.demo.evaluation.models.EvaluationPair;
import gr.forth.ics.isl.demo.evaluation.models.EvaluationQuery;
import gr.forth.ics.isl.demo.evaluation.models.ModelHyperparameters;
import gr.forth.ics.isl.demo.models.BaselineModel;
import gr.forth.ics.isl.demo.models.Model;
import gr.forth.ics.isl.demo.models.Word2vecModel;
import gr.forth.ics.isl.demo.models.WordnetModel;
import gr.forth.ics.isl.demo.models.WordnetWord2vecModel;
import static gr.forth.ics.isl.main.demo_main.getComments;
import gr.forth.ics.isl.nlp.models.Comment;
import gr.forth.ics.isl.sailInfoBase.QAInfoBase;
import gr.forth.ics.isl.sailInfoBase.models.Subject;
import gr.forth.ics.isl.utilities.StringUtils;
import gr.forth.ics.isl.utilities.Utils;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
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
    //static int topK = 40;//2000;
    static String evalCollection = "hotelsTestCollectionA.csv";

    //The paths for the stopWords Files.
    public static String filePath_en = "src/main/resources/stoplists/stopwordsEn.txt";
    public static String filePath_gr = "src/main/resources/stoplists/stopwordsGr.txt";

    //A hashMap that contains two Trie, one for the English stopList and one for the Greek
    public static HashMap<String, Trie> stopLists = new HashMap<>();

    public static void main(String[] args) throws IOException, RepositoryException, MalformedQueryException, QueryEvaluationException, FileNotFoundException, ClassNotFoundException {
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
        HashSet<Subject> hotels = KB.getAllSubjectsOfType("hippalus", "hippalusID");
        // Uncomment this for reviews only data set
        //HashSet<Subject> reviews = KB.getAllSubjectsOfType("hip", "review");
        timer.end();
        long resourcesTime = timer.getTotalTime();
        System.out.println("Time to load resources: " + resourcesTime);

        System.out.println("External Resources were loaded successfully");
        // Create a List of queries
        ArrayList<String> queryList = new ArrayList<>();
        queryList.add("Has anyone reported a problem about noise?");
        queryList.add("Is this hotel quiet?");

        // Get all the comments
        // uncomment this for hybrid data set
        ArrayList<Comment> comments = getComments(hotels, KB);
        // uncomment this for revies only data set
        //ArrayList<Comment> comments = getCommentsFromTextOnlyKB(reviews);

        // This structure will contain the ground truth relevance between each
        // query and each comment
        HashMap<String, HashMap<String, EvaluationPair>> gt = new HashMap<>();
        gt = readEvaluationSet(evalCollection);

        System.out.println(gt.get("q1").size());
        System.out.println(gt.get("q2").size());

        // Retrieve hyperparameters
        ModelHyperparameters bestModel = (ModelHyperparameters) Utils.getSavedObject("AVEPbased_BestModel");
        float word2vec_w = bestModel.getWord2vecWeight();
        float wordNet_w = bestModel.getWordNetWeight();

        // Choose wordnet sources to be used
        ArrayList<String> wordnetResources = new ArrayList<>();
        wordnetResources.add("synonyms");
        wordnetResources.add("antonyms");
        wordnetResources.add("hypernyms");

        // Choose weights to be used in model IV
        HashMap<String, Float> model_weights = new HashMap<>();
        model_weights.put("wordnet", wordNet_w);
        model_weights.put("word2vec", word2vec_w);

        // Instantiate models
        BaselineModel baseline = new BaselineModel("Baseline model (Jaccard Similarity)", comments);
        WordnetModel wordnet = new WordnetModel("Wordnet model", dict, wordnetResources, comments);
        Word2vecModel word2vec = new Word2vecModel("Word2vec model", wm, vec, comments);
        WordnetWord2vecModel combination = new WordnetWord2vecModel("Word2vec and Wordnet", dict, wordnetResources, wm, vec, model_weights, comments);

        // Add models to an ArrayList that contains all models to be tested
        ArrayList<Model> models_to_test = new ArrayList<>();
        models_to_test.add(baseline);
        models_to_test.add(wordnet);
        models_to_test.add(word2vec);
        models_to_test.add(combination);

        // for all models
        for (Model tmp_model : models_to_test) {
            int R = 0; // default R (num of relevants for each query)
            int R2 = 0;
            int cnt = 1; // query counter

            double R_Precision_2 = 0.0; // 2-Precision

            double R_Precision = 0.0; // R-Precision
            double AVEP = 0.0; // Avep
            double BPREF = 0.0; // Bpref

            ArrayList<Integer> testSet = new ArrayList<>(); // true binary relevance for a query
            ArrayList<Double> crntCompRelevance = new ArrayList<>(); // floating point relevance calculated for a query

            ArrayList<Integer> wholeTestSet = new ArrayList<>(); // true binary relevance for all queries
            ArrayList<Double> wholeScoreSet = new ArrayList<>(); // floating point relevance calculated for all queries
            //for each query
            while (cnt <= queryList.size()) {

                // Get the ground truth for the current query
                HashMap<String, EvaluationPair> evalPairsWithCrntQueryId = gt.get("q" + cnt);

                // Set R parameters of evaluation metrics
                R2 = 2;
                R = getNumOfRels(evalPairsWithCrntQueryId);
                System.out.println(R);

                // init test set for the current query
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

                tmp_model.scoreComments(question);
                ArrayList<Comment> rankedComments = tmp_model.getTopComments(comments.size());
                //Retrieve the relevant comments
                //ArrayList<Comment> rankedComments = getTopkRelevantComments(comments, word2vec_w, wordNet_w, question, wm, vec, dict);

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
                        testSet.add(p.getRelevance()); // true binarry relevance

                        //System.out.println(p.getRelevance() + ", " + resultCom.getId());
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
                R_Precision_2 += EvaluationMetrics.R_Precision(testSet, R2);
                R_Precision += EvaluationMetrics.R_Precision(testSet, R);
                // Calculate the AveP of our system's answer
                AVEP += EvaluationMetrics.AVEP(testSet, R);
                // Calculate the BPREF of our system's answer
                BPREF += EvaluationMetrics.BPREF(testSet, R);
            }

            // Calculate mean BPREF, R_Precision and AveP for all queries
            R_Precision_2 /= queryList.size();
            R_Precision /= queryList.size();
            AVEP /= queryList.size();
            BPREF /= queryList.size();

            System.out.println(tmp_model.getDescription());
            // Print the results
            System.out.println("R_Precision_2: " + R_Precision_2);
            System.out.println("R_Precision_default: " + R_Precision);
            System.out.println("AVEP: " + AVEP);
            System.out.println("BPREF: " + BPREF);
            System.out.println("TrueSet: " + wholeTestSet);
            System.out.println("ComputedRelevance: " + wholeScoreSet);
            System.out.println("TrueSet size: " + wholeTestSet.size());
            System.out.println("ComputedRelevance size: " + wholeScoreSet.size());
            System.out.println("========================");
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
     * This method is used to create our evaluation structure based on the
     * evaluation collection.
     *
     * @param fileName
     * @return HashMap<String, ArrayList<EvaluationPair>> groundTruth
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static HashMap<String, HashMap<String, EvaluationPair>> readEvaluationSet(String fileName) throws FileNotFoundException, IOException {
        HashMap<String, HashMap<String, EvaluationPair>> groundTruth = new HashMap<>();
        String line;
        BufferedReader br = new BufferedReader(new FileReader("src/main/resources/evaluation/" + fileName));
        while ((line = br.readLine()) != null) {

            // use comma as separator
            String[] tuple = line.split(",");

            int eval_pair_id = Integer.parseInt(tuple[0]);
            String query_id = tuple[1];
            String comment_id = tuple[2];
            String comment_date = tuple[3];
            int relevance = Integer.parseInt(tuple[4]);


            EvaluationQuery evalQuery = new EvaluationQuery(query_id);
            EvaluationComment evalComment = new EvaluationComment(comment_id, comment_date);
            EvaluationPair evalPair = new EvaluationPair(eval_pair_id, evalQuery, evalComment, relevance);

            HashMap<String, EvaluationPair> pairsWithQueryId = groundTruth.get(query_id);
            if (pairsWithQueryId == null) {
                pairsWithQueryId = new HashMap<>();
                pairsWithQueryId.put(evalPair.getComment().getId(), evalPair);
                groundTruth.put(query_id, pairsWithQueryId);
            } else {
                pairsWithQueryId.put(evalPair.getComment().getId(), evalPair);
                groundTruth.put(query_id, pairsWithQueryId);
            }

        }

        return groundTruth;
    }
}
