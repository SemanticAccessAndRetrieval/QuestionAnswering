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
import gr.forth.ics.isl.evaluation.models.EvaluationComment;
import gr.forth.ics.isl.evaluation.models.EvaluationPair;
import gr.forth.ics.isl.evaluation.models.EvaluationQuery;
import gr.forth.ics.isl.evaluation.models.ModelHyperparameters;
import static gr.forth.ics.isl.main.demo_main.getCommentsAsMap;
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

    //static int topK = 10;

    //The paths for the stopWords Files.
    public static String filePath_en = "src/main/resources/stoplists/stopwordsEn.txt";
    public static String filePath_gr = "src/main/resources/stoplists/stopwordsGr.txt";

    //A hashMap that contains two Trie, one for the English stopList and one for the Greek
    public static HashMap<String, Trie> stopLists = new HashMap<>();

    public static void main(String[] args) throws IOException, RepositoryException, MalformedQueryException, QueryEvaluationException {
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
        HashSet<Subject> hotels = KB.getAllSubjectsOfType("hippalus", "hippalusID");

        System.out.println("External Resources were loaded successfully");
        // Create a List of queries
        ArrayList<String> queryList = new ArrayList<>();
        queryList.add("Has anyone reported a problem about noise?");
        queryList.add("Is this hotel quiet?");

        // Get all the comments
        HashMap<String, Comment> comments = getCommentsAsMap(hotels, KB);
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
        HashMap<String, ArrayList<EvaluationPair>> gt = new HashMap<>();
        gt = readEvaluationSet("hotelsTestCollectionB.csv");

        for (float word2vec_w = 0.0f; word2vec_w <= 1.0f; word2vec_w = word2vec_w + 0.1f) {
            for (float wordNet_w = 0.0f; wordNet_w <= 1.0f; wordNet_w = wordNet_w + 0.1f) {

                if (word2vec_w + wordNet_w != 1.0f /*|| word2vec_w == 0.0f || wordNet_w == 0.0f*/) {
                    continue;
                }

                for (float threshold = 0.00f; threshold <= 1.00f; threshold = threshold + 0.01f) {

                    if (threshold == 0.0) {
                        continue;
                    }

                    int cnt = 1;
                    double R_Precision = 0.0;
                    double AVEP = 0.0;
                    double BPREF = 0.0;
                    ArrayList<Integer> resultSet = new ArrayList<>();
                    ArrayList<Integer> testSet = new ArrayList<>();

                    //for each query
                    while (cnt <= queryList.size()) {
                        //Get the user's question
                        String question = queryList.get(cnt - 1);

                        System.out.println("========================");

                        //Get the weights for the scoring
                        System.out.println("word2vec weight: " + word2vec_w);
                        System.out.println("wordNet weight: " + wordNet_w);

                        //Get the threshold for relevancy
                        System.out.println("threshold: " + threshold);

                        double max_dist = Double.MIN_VALUE;
                        //Calculate score for each comment
                        //Also calculate max word mover distance
                        for (String comId : comments.keySet()) {
                            Comment com = comments.get(comId);
                            com.calculateScores(wm, question, vec, dict, word2vec_w, wordNet_w);
                            //com.calculateScores(question, dict, wordNet_w);
                            if (com.getWordScore() >= max_dist) {
                                max_dist = com.getWordScore();
                            }
                            comments.put(comId, com);
                        }

                        //Normalize WordMoverDistance, and update comments with the final scores
                        for (String comId : comments.keySet()) {
                            Comment com = comments.get(comId);
                            com.calculateWordScore(max_dist);
                            com.calculateScore(word2vec_w, wordNet_w);
                            comments.put(comId, com);
                        }

                        // Get the ground truth for the current query
                        ArrayList<EvaluationPair> evalPairsWithQueryId = gt.get("q" + cnt);
                        for (EvaluationPair p : evalPairsWithQueryId) {

                            // for each comment in the ground truth collection
                            // get that comment from our database
                            Comment resultCom = comments.get(p.getComment().getId());

                            // Choose weather it is relevant with the query or not
                            if (resultCom.getScore() >= threshold) {
                                resultSet.add(1);
                            } else {
                                resultSet.add(0);
                            }
                            // keep truck of it's true relevance value
                            testSet.add(p.getRelevance());
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

                    // Print the results
                    System.out.println("R_Precision: " + R_Precision);
                    System.out.println("AVEP: " + AVEP);
                    System.out.println("BPREF: " + BPREF);
                    System.out.println("========================");

                    // Create current models and store them their associated structures
                    ModelHyperparameters crntModelRPrecision = new ModelHyperparameters(R_Precision, word2vec_w, wordNet_w, threshold);
                    crntModelRPrecision.setR_Precision(R_Precision);
                    crntModelRPrecision.setAveP(AVEP);
                    crntModelRPrecision.setBPREF(BPREF);
                    ModelHyperparameters crntModelAVEP = new ModelHyperparameters(AVEP, word2vec_w, wordNet_w, threshold);
                    crntModelAVEP.setR_Precision(R_Precision);
                    crntModelAVEP.setAveP(AVEP);
                    crntModelAVEP.setBPREF(BPREF);
                    ModelHyperparameters crntModelBPREF = new ModelHyperparameters(BPREF, word2vec_w, wordNet_w, threshold);
                    crntModelBPREF.setR_Precision(R_Precision);
                    crntModelBPREF.setAveP(AVEP);
                    crntModelBPREF.setBPREF(BPREF);

                    allModelsWithRPrecision.add(crntModelRPrecision);
                    allModelsWithAVEP.add(crntModelAVEP);
                    allModelsWithBPREF.add(crntModelBPREF);

                }
            }
        }
        // Save and print the best models
        if (!allModelsWithRPrecision.isEmpty()) {
            System.out.println("==== Best model hyperparams and perfomance ====");
            System.out.println("==== Based on R_Precision ====");
            System.out.println(allModelsWithRPrecision.size());
            Utils.saveObject(Collections.max(allModelsWithRPrecision), "rPrecisionBased_BestModel");
            System.out.println(Collections.max(allModelsWithRPrecision));
            System.out.println("======= Based on AVEP ========");
            System.out.println(allModelsWithAVEP.size());
            Utils.saveObject(Collections.max(allModelsWithAVEP), "AVEPbased_BestModel");
            System.out.println(Collections.max(allModelsWithAVEP));
            System.out.println("======= Based on BPREF ========");
            System.out.println(allModelsWithBPREF.size());
            Utils.saveObject(Collections.max(allModelsWithBPREF), "BPREFbased_BestModel");
            System.out.println(Collections.max(allModelsWithBPREF));
        }
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
    public static HashMap<String, ArrayList<EvaluationPair>> readEvaluationSet(String fileName) throws FileNotFoundException, IOException {
        //ArrayList<EvaluationPair> groundTruth = new ArrayList<>();
        HashMap<String, ArrayList<EvaluationPair>> groundTruth = new HashMap<>();
        String line = "";
        BufferedReader br = new BufferedReader(new FileReader("src/main/resources/evaluation/" + fileName));
        while ((line = br.readLine()) != null) {

            // use comma as separator
            String[] tuple = line.split(",");

            EvaluationQuery evalQuery = new EvaluationQuery(tuple[1]);
            EvaluationComment evalComment = new EvaluationComment(tuple[2], tuple[3]);
            EvaluationPair evalPair = new EvaluationPair(Integer.parseInt(tuple[0]), evalQuery, evalComment, Integer.parseInt(tuple[4]));

            ArrayList<EvaluationPair> pairsWithQueryId = groundTruth.get(tuple[1]);
            if (pairsWithQueryId == null) {
                pairsWithQueryId = new ArrayList<>();
                pairsWithQueryId.add(evalPair);
                groundTruth.put(tuple[1], pairsWithQueryId);
            } else {
                pairsWithQueryId.add(evalPair);
                groundTruth.put(tuple[1], pairsWithQueryId);
            }

        }

        return groundTruth;
    }
}
