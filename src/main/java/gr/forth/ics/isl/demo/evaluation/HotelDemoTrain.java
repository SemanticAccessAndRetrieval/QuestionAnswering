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
import static gr.forth.ics.isl.demo.evaluation.HotelDemoTestSuit.getNumOfRels;
import static gr.forth.ics.isl.demo.evaluation.HotelDemoTestSuit.readEvaluationSet;
import gr.forth.ics.isl.demo.evaluation.models.EvaluationPair;
import gr.forth.ics.isl.demo.evaluation.models.ModelHyperparameters;
import gr.forth.ics.isl.demo.models.WordnetWord2vecModel;
import static gr.forth.ics.isl.main.demo_main.getComments;
import gr.forth.ics.isl.nlp.models.Comment;
import gr.forth.ics.isl.sailInfoBase.QAInfoBase;
import gr.forth.ics.isl.sailInfoBase.models.Subject;
import gr.forth.ics.isl.utilities.StringUtils;
import gr.forth.ics.isl.utilities.Utils;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
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
public class HotelDemoTrain {
//Number of top comments to retrieve

    static int topK = 40;//71;
    static String evalCollection = "hotelsTestCollectionA.csv";

    //The paths for the stopWords Files.
    public static String filePath_en = "src/main/resources/stoplists/stopwordsEn.txt";
    public static String filePath_gr = "src/main/resources/stoplists/stopwordsGr.txt";

    //A hashMap that contains two Trie, one for the English stopList and one for the Greek
    public static HashMap<String, Trie> stopLists = new HashMap<>();

    public static void main(String[] args) throws MalformedURLException, IOException, RepositoryException, MalformedQueryException, QueryEvaluationException {
        // Create the list of stopWords to use
        StringUtils.generateStopLists(filePath_en, filePath_gr);

        // Create Word2Vec model
        File gModel = new File("C:/Users/Sgo/Desktop/Developer/Vector Models/GoogleNews-vectors-negative300.bin.gz");

        Word2Vec vec = WordVectorSerializer.readWord2VecModel(gModel);
        WordMovers wm = WordMovers.Builder().wordVectors(vec).build();

        // Create WodNet Dictionary
        String wnhome = System.getenv("WNHOME");
        String path = wnhome + File.separator + "dict";
        URL url = new URL("file", null, path);
        // construct the dictionary object and open it
        IDictionary dict = new Dictionary(url);
        // open WordNet dictionary
        dict.open();

        // Create hotel database
        QAInfoBase KB = new QAInfoBase();

        // Retrieve hotels
        // Uncomment this for hybrid data set
        HashSet<Subject> hotels = KB.getAllSubjectsOfType("hippalus", "hippalusID");
        // Uncomment this for reviews only data set
        //HashSet<Subject> reviews = KB.getAllSubjectsOfType("hip", "review");

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

        ArrayList<ModelHyperparameters> models_to_test = new ArrayList<>();

        System.out.println(gt.get("q1").size());
        System.out.println(gt.get("q2").size());

        for (float word2vec_w = 0.0f; word2vec_w <= 1.0f; word2vec_w = word2vec_w + 0.1f) {
            for (float wordNet_w = 0.0f; wordNet_w <= 1.0f; wordNet_w = wordNet_w + 0.1f) {

                word2vec_w = Math.round(word2vec_w * 10.0f) / 10.0f;
                wordNet_w = Math.round(wordNet_w * 10.0f) / 10.0f;

                if (word2vec_w + wordNet_w != 1.0f) {
                    continue;
                }

                ArrayList<String> wordnetResources = new ArrayList<>();
                wordnetResources.add("synonyms");
                wordnetResources.add("antonyms");
                wordnetResources.add("hypernyms");

                HashMap<String, Float> model_weights = new HashMap<>();
                model_weights.put("wordnet", wordNet_w);
                model_weights.put("word2vec", word2vec_w);

                WordnetWord2vecModel combination = new WordnetWord2vecModel("Word2vec and Wordnet", dict, wordnetResources, wm, vec, model_weights, comments);

                int cnt = 1; // query counter
                double AVEP = 0.0; // Avep
                ArrayList<Integer> testSet = new ArrayList<>(); // true binary relevance for a query

                //for each query
                while (cnt <= queryList.size()) {
                    // Get the ground truth for the current query
                    HashMap<String, EvaluationPair> evalPairsWithCrntQueryId = gt.get("q" + cnt);
                    int R = getNumOfRels(evalPairsWithCrntQueryId);
                    System.out.println(R);

                    // init test set for the current query
                    testSet = new ArrayList<>();

                    //Get the user's question
                    String question = queryList.get(cnt - 1);

                    System.out.println("========================");

                    //Print the weights for the scoring
                    System.out.println("word2vec weight: " + word2vec_w);
                    System.out.println("wordNet weight: " + wordNet_w);

                    combination.scoreComments(question);
                    ArrayList<Comment> rankedComments = combination.getTopComments(topK);

                    // for all retrieved comments
                    for (Comment resultCom : rankedComments) {
                        // keep truck of comment's true and calculated relevance value
                        // if comment is unjudged skip it
                        EvaluationPair p = evalPairsWithCrntQueryId.get(resultCom.getId());
                        if (p != null) {
                            testSet.add(p.getRelevance()); // true binarry relevance

                            System.out.println(p.getRelevance() + ", " + resultCom.getId());
                            //System.out.println(p.getComment().getId() + " -- " + resultCom.getId());
                        }
                    }

                    System.out.println(testSet); // print test set with the order of result set
                    cnt++; // go to the next query

                    // Calculate the AveP of our system's answer
                    AVEP += EvaluationMetrics.AVEP(testSet, R);


                }
                // Calculate mean AveP for all queries
                AVEP /= queryList.size();
                // Add mean Avep score to the current model
                ModelHyperparameters crntModel = new ModelHyperparameters(AVEP, word2vec_w, wordNet_w);
                models_to_test.add(crntModel);
            }
        }

        // Choose best model
        ModelHyperparameters bestModel = getBestPerformingModel(models_to_test);
        // Save the best model
        Utils.saveObject(bestModel, "AVEPbased_BestModel");

    }

    public static ModelHyperparameters getBestPerformingModel(ArrayList<ModelHyperparameters> models_to_test) {

        ModelHyperparameters bestModel = null;

        // Choose best model
        // Save and print the best models
        if (!models_to_test.isEmpty()) {
            System.out.println("======= Based on AVEP ========");
            System.out.println(models_to_test.size());
            bestModel = Collections.max(models_to_test);
            System.out.println(bestModel);
        }

        return bestModel;
    }

}
