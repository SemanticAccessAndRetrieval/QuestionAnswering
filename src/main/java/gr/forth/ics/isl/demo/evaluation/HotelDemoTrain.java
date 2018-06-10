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
import static gr.forth.ics.isl.demo.evaluation.EvalCollectionManipulator.readEvaluationSet;
import gr.forth.ics.isl.demo.evaluation.models.EvaluationPair;
import gr.forth.ics.isl.demo.evaluation.models.ModelHyperparameters;
import gr.forth.ics.isl.demo.models.WordnetWord2vecModel;
import static gr.forth.ics.isl.main.demo_main.getComments;
import static gr.forth.ics.isl.main.demo_main.getCommentsFromBooking;
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

    //static int topK = 40;
    static String evalCollection = "FRUCE_v2.csv";

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

        HashSet<Subject> hotels = new HashSet<>();
        HashSet<Subject> reviews = new HashSet<>();
        int relThreshold = 0;

        if (evalCollection.contains("FRUCE")) {
            // Retrieve hotels
            // Uncomment this for hybrid data set
            relThreshold = 0;
            hotels = KB.getAllSubjectsOfType("hippalus", "hippalusID");
        } else {
            // Uncomment this for reviews only data set
            reviews = KB.getAllSubjectsOfType("hip", "review");
            relThreshold = 0;
        }

        System.out.println("External Resources were loaded successfully");

        // This structure will contain the ground truth relevance between each
        // query and each comment
        HashMap<String, HashMap<String, EvaluationPair>> gt = new HashMap<>();
        gt = readEvaluationSet(evalCollection);

        // Get all the comments
        // uncomment this for hybrid data set
        ArrayList<Comment> comments = new ArrayList<>();
        if (evalCollection.contains("FRUCE")) {
            comments = getComments(hotels, KB);
        } else {
            // uncomment this for revies only data set
            comments = getCommentsFromBooking(reviews);
        }

        // Choose weights to be used in model IV
        HashMap<String, Float> model_weights = new HashMap<>();
        model_weights.put("wordnet", 0.5f);
        model_weights.put("word2vec", 0.5f);

        // Choose wordnet sources to be used
        ArrayList<String> wordnetResources = new ArrayList<>();
        wordnetResources.add("synonyms");

//        WordnetWord2vecModel modelNoHypNoAnt = new WordnetWord2vecModel("Word2vec and Wordnet", dict, wordnetResources, wm, vec, model_weights, comments);
//        ModelHyperparameters bestModel_NoHyp_NoAnt = modelNoHypNoAnt.train(gt, wordnetResources, model_weights, relThreshold);

        wordnetResources.add("antonyms");

//        WordnetWord2vecModel modelNoHyp = new WordnetWord2vecModel("Word2vec and Wordnet", dict, wordnetResources, wm, vec, model_weights, comments);
//        ModelHyperparameters bestModel_NoHyp = modelNoHyp.train(gt, wordnetResources, model_weights, relThreshold);

        wordnetResources.add("hypernyms");
        WordnetWord2vecModel model = new WordnetWord2vecModel("Word2vec and Wordnet", dict, wordnetResources, wm, vec, model_weights, comments);
        ModelHyperparameters bestModelAVEP = model.train(gt, wordnetResources, model_weights, relThreshold, "AVEP");
        ModelHyperparameters bestModelP2 = model.train(gt, wordnetResources, model_weights, relThreshold, "P@2");

        System.out.println("=== Best Performing Model (AVEP) ===");
        System.out.println(bestModelAVEP);
        System.out.println("=== Best Performing Model (P@2) ===");
        System.out.println(bestModelP2);

        Utils.saveObject(bestModelAVEP, "AVEPbased_BestModel");
        Utils.saveObject(bestModelP2, "P@2based_BestModel");
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
