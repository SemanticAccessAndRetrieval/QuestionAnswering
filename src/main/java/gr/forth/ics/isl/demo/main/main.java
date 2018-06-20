/*
 *  This code belongs to the Semantic Access and Retrieval (SAR) group of the 
 *  Information Systems Laboratory (ISL) of the 
 *  Institute of Computer Science (ICS) of the  
 *  Foundation for Research and Technology - Hellas (FORTH)
 *  Nobody is allowed to use, copy, distribute, or modify this work.
 *  It is published for reasons of research results reproducibility.
 *  (c) 2017 Semantic Access and Retrieval group, All rights reserved
 */
package gr.forth.ics.isl.demo.main;

import com.crtomirmajer.wmd4j.WordMovers;
import edu.mit.jwi.Dictionary;
import edu.mit.jwi.IDictionary;
import static gr.forth.ics.isl.demo.evaluation.EvalCollectionManipulator.readEvaluationSet;
import static gr.forth.ics.isl.demo.evaluation.HotelDemoTestSuit2.getQueryList;
import gr.forth.ics.isl.demo.evaluation.models.EvaluationPair;
import gr.forth.ics.isl.demo.models.WordnetWord2vecModel;
import static gr.forth.ics.isl.main.demo_main.getComments;
import gr.forth.ics.isl.nlp.models.Comment;
import gr.forth.ics.isl.sailInfoBase.QAInfoBase;
import gr.forth.ics.isl.sailInfoBase.models.Subject;
import gr.forth.ics.isl.utilities.StringUtils;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
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
 * @author Lefteris Dimitrakis
 */
public class main {

    //Number of top comments to retrieve
    static int topK = 40;

    //The paths for the stopWords Files.
    public static String filePath_en = "src/main/resources/stoplists/stopwordsEn.txt";
    public static String filePath_gr = "src/main/resources/stoplists/stopwordsGr.txt";

    //A hashMap that contains two Trie, one for the English stopList and one for the Greek
    public static HashMap<String, Trie> stopLists = new HashMap<>();

    public static void main(String[] args) throws MalformedURLException, IOException, RepositoryException, MalformedQueryException, QueryEvaluationException {

        //Create the list of stopWords to use
        StringUtils.generateStopLists(filePath_en, filePath_gr);

        File gModel = new File("C:/Users/Sgo/Desktop/Developer/Vector Models/GoogleNews-vectors-negative300.bin.gz");
        Word2Vec vec = WordVectorSerializer.readWord2VecModel(gModel);
        WordMovers wm = WordMovers.Builder().wordVectors(vec).build();

        String wnhome = System.getenv("WNHOME");
        String path = wnhome + File.separator + "dict";
        URL url = new URL("file", null, path);
        // construct the dictionary object and open it
        IDictionary dict = new Dictionary(url);
        dict.open();

        QAInfoBase KB = new QAInfoBase();
        HashSet<Subject> hotels = KB.getAllSubjectsOfType("hippalus", "hippalusID");

        System.out.println("External Resources were loaded successfully");

        // Get all the comments
        ArrayList<Comment> comments = getComments(hotels, KB);

        ArrayList<String> wordnetResources = new ArrayList<>();
        wordnetResources.add("synonyms");
        wordnetResources.add("antonyms");
        wordnetResources.add("hypernyms");

        HashMap<String, Float> model_weights = new HashMap<>();
        model_weights.put("wordnet", 0.6f);
        model_weights.put("word2vec", 0.4f);

//        HashMap<String, Float> model_weights2 = new HashMap<>();
//        model_weights2.put("wordnet", 0.0f);
//        model_weights2.put("word2vec", 1.0f);
//        BaselineModel baseline = new BaselineModel("Baseline model (Jaccard Similarity)", comments);
//        WordnetModel wordnet = new WordnetModel("Wordnet model", dict, wordnetResources, comments);
//        Word2vecModel word2vec = new Word2vecModel("Word2vec model", wm, vec, comments);
        WordnetWord2vecModel combination = new WordnetWord2vecModel("Word2vec and Wordnet", dict, wordnetResources, wm, vec, model_weights, comments);
//        WordnetWord2vecModel combination2 = new WordnetWord2vecModel("Word2vec and Wordnet", dict, wordnetResources, wm, vec, model_weights2, comments);

        // This structure will contain the ground truth relevance between each
        // query and each comment
        HashMap<String, HashMap<String, EvaluationPair>> gt = readEvaluationSet("FRUCE_v2.csv");
        // retrieve list of queries
        HashMap<String, String> queryList = getQueryList(gt);

        //for each query
        for (String qID : queryList.keySet()) {
            // Get the ground truth for the current query
            HashMap<String, EvaluationPair> evalPairsWithCrntQueryId = gt.get(qID);
            //Get the user's question
            String question = queryList.get(qID);
            System.out.println(question);
            /*
            System.out.println("=========Baseline START==========");
            baseline.scoreComments(question);
            ArrayList<Comment> baseline_rank = baseline.getTopComments(topK);
            for (Comment c : baseline_rank) {
                System.out.println(c.getText());
                System.out.println(c.getScore());
            }
            System.out.println("=========Baseline END==========");
             */
//            System.out.println("=========Wordnet START==========");
//            wordnet.scoreComments(question);
//            ArrayList<Comment> wordnet_rank = wordnet.getTopComments(topK);
//            for (Comment c : wordnet_rank) {
//                System.out.println(c.getText());
//                System.out.println(c.getScore());
//            }
//            System.out.println("=========Wordnet END==========");
//
//            System.out.println("=========Word2vec START==========");
//            word2vec.scoreComments(question);
//            ArrayList<Comment> word2vec_rank = word2vec.getTopComments(topK);
//            for (Comment c : word2vec_rank) {
//                System.out.println(c.getText());
//                System.out.println(c.getScore());
//            }
//            System.out.println("=========Word2vec END==========");
//
            System.out.println("=========Combination START==========");
            combination.scoreComments(question);
            ArrayList<Comment> combination_rank = combination.getTopComments(topK);
            for (Comment c : combination_rank) {
                //System.out.println(gt.get(qID).containsValue(c));
                if (gt.get(qID).get(c.getId()) != null) {
                    System.out.println(c.getText());
                    System.out.println(c.getScore());
                    System.out.println(c.getId());
                    System.out.println(gt.get(qID).get(c.getId()).getRelevance());
                }
            }
            System.out.println("=========Combination END==========");
//
//            System.out.println("=========Combination2 START==========");
//            combination2.scoreComments(question);
//            ArrayList<Comment> combination_rank2 = combination2.getTopComments(topK);
//            for (Comment c : combination_rank2) {
//                System.out.println(c.getText());
//                System.out.println(c.getScore());
//            }
//            System.out.println("=========Combination2 END==========");
//
        }
    }

}
