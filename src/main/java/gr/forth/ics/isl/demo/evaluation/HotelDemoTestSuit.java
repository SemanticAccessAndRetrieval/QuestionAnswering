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
import static gr.forth.ics.isl.demo.evaluation.EvalCollectionManipulator.readEvaluationSet;
import gr.forth.ics.isl.demo.evaluation.models.EvaluationPair;
import gr.forth.ics.isl.demo.evaluation.models.ModelHyperparameters;
import gr.forth.ics.isl.demo.evaluation.models.ModelStats;
import gr.forth.ics.isl.demo.models.BaselineModel;
import gr.forth.ics.isl.demo.models.Model;
import gr.forth.ics.isl.demo.models.Word2vecModel;
import gr.forth.ics.isl.demo.models.WordnetModel;
import gr.forth.ics.isl.demo.models.WordnetWord2vecModel;
import gr.forth.ics.isl.demo.models.WordnetWord2vecModel_II;
import static gr.forth.ics.isl.main.demo_main.getComments;
import static gr.forth.ics.isl.main.demo_main.getCommentsFromBooking;
import static gr.forth.ics.isl.main.demo_main.getCommentsFromWebAP;
import gr.forth.ics.isl.nlp.models.Comment;
import gr.forth.ics.isl.sailInfoBase.QAInfoBase;
import gr.forth.ics.isl.sailInfoBase.models.Subject;
import gr.forth.ics.isl.utilities.StringUtils;
import gr.forth.ics.isl.utilities.Utils;
import java.io.File;
import java.io.FileNotFoundException;
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
    //static String evalCollection = "webAP.csv";
    static String evalCollection = "FRUCE_v2.csv";
    //static String evalCollection = "BookingEvalCollection.csv";

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

        HashSet<Subject> hotels = new HashSet<>();
        HashSet<Subject> reviews = new HashSet<>();
        int relThreshold = 0;

        if (evalCollection.contains("FRUCE")) {
            // Retrieve hotels
            hotels = KB.getAllSubjectsOfType("hippalus", "hippalusID");
            relThreshold = 0;
        } else if (evalCollection.contains("webAP")) {
            System.out.println("KB subject retrieval bypassed.");
            relThreshold = 2;
        } else {
            relThreshold = 0;
            reviews = KB.getAllSubjectsOfType("hip", "review");
        }
        timer.end();
        long resourcesTime = timer.getTotalTime();
        System.out.println("Time to load resources: " + resourcesTime);

        System.out.println("External Resources were loaded successfully");
        // Create a List of queries
        //ArrayList<String> queryList = new ArrayList<>();
        //queryList.add("Has anyone reported a problem about noise?");
        //queryList.add("Is this hotel quiet?");

        ArrayList<Comment> comments = new ArrayList<>();
        // Get all the comments
        if (evalCollection.contains("FRUCE")) {
            comments = getComments(hotels, KB);
        } else if (evalCollection.contains("webAP")) {
            System.out.println("Loading sentences");
            comments = getCommentsFromWebAP();
            System.out.println("Loaded sentences");
        } else {
            comments = getCommentsFromBooking(reviews);
        }

        // This structure will contain the ground truth relevance between each
        // query and each comment
        HashMap<String, HashMap<String, EvaluationPair>> gt = new HashMap<>();
        gt = readEvaluationSet(evalCollection);

        System.out.println("Num of Queries: " + gt.size());

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
        //WordnetModel wordnetNoHyp = new WordnetModel("Wordnet model No Hypernyms", dict, new ArrayList<>(wordnetResources.subList(0, 2)), comments);
        WordnetModel wordnet = new WordnetModel("Wordnet model", dict, wordnetResources, comments);
        Word2vecModel word2vec = new Word2vecModel("Word2vec model", wm, vec, comments);
        //WordnetWord2vecModel combinationNoHyp = new WordnetWord2vecModel("Word2vec and WordnetNoHyp", dict, new ArrayList<>(wordnetResources.subList(0, 2)), wm, vec, model_weights, comments);
        WordnetWord2vecModel combination = new WordnetWord2vecModel("Word2vec and Wordnet", dict, wordnetResources, wm, vec, model_weights, comments);
        WordnetWord2vecModel_II combination_II = new WordnetWord2vecModel_II("Word2vec and Wordnet II", dict, wordnetResources, wm, vec, comments);

        // Add models to an ArrayList that contains all models to be tested
        ArrayList<Model> models_to_test = new ArrayList<>();
        models_to_test.add(baseline);
        //models_to_test.add(wordnetNoHyp);
        models_to_test.add(wordnet);
        models_to_test.add(word2vec);
        //models_to_test.add(combinationNoHyp);
        models_to_test.add(combination);
        models_to_test.add(combination_II);

        // for all models
        for (Model tmp_model : models_to_test) {
            ModelStats model_stats = new ModelStats(tmp_model.getDescription());
            System.out.println("Evaluating model");
            model_stats.evaluate(tmp_model, comments, gt, relThreshold);
            System.out.println("Evaluated model");
            //ModelStats model_stats = new ModelStats(tmp_model);
            //model_stats.evaluate(comments, gt);
//            model_stats.getAllMetricsBoundedR(1, 33, comments, gt);
//
            Utils.saveObject(model_stats.getScoreSet(), tmp_model.getDescription() + "_ScoreSet");
            Utils.saveObject(model_stats.getTestSet(), tmp_model.getDescription() + "_TestSet");
//            Utils.saveObject(model_stats.getAllPrecisions_R(), tmp_model.getDescription() + "_all_Precisions_R");
//            Utils.saveObject(model_stats.getAllAveps_R(), tmp_model.getDescription() + "_all_Aveps_R");
//            Utils.saveObject(model_stats.getAllBprefs_R(), tmp_model.getDescription() + "_all_Bprefs_R");

            System.out.println(model_stats);
        }

        overallTimer.end();
        long overallExpTime = overallTimer.getTotalTime();
        System.out.println("Total time for test suit to run: " + overallExpTime);
    }
}
