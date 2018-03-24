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
import gr.forth.ics.isl.demo.models.BaselineModel;
import gr.forth.ics.isl.demo.models.Model;
import gr.forth.ics.isl.demo.models.Word2vecModel;
import gr.forth.ics.isl.demo.models.WordnetModel;
import gr.forth.ics.isl.demo.models.WordnetWord2vecModel;
import static gr.forth.ics.isl.main.demo_main.getCommentsFromTextOnlyKB;
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
 * @author Sgo
 */


public class HotelDemoEfficiencyTestSuit {

    //Number of top comments to retrieve
    static int topK = 10;

    //The paths for the stopWords Files.
    public static String filePath_en = "src/main/resources/stoplists/stopwordsEn.txt";
    public static String filePath_gr = "src/main/resources/stoplists/stopwordsGr.txt";

    //A hashMap that contains two Trie, one for the English stopList and one for the Greek
    public static HashMap<String, Trie> stopLists = new HashMap<>();

    public static Word2Vec w2_vector;
    public static WordMovers word_movers;
    public static IDictionary wordnet_dict;
    public static ArrayList<Comment> all_comments;

    public static void main(String[] args) throws RepositoryException, MalformedQueryException, QueryEvaluationException, IOException {

        // Create the list of stopWords to use
        StringUtils.generateStopLists(filePath_en, filePath_gr);

        //Measure Init time
        measureInitTime();

        //Get the user's question
        String question = "Has anyone reported a problem about noise?";

        ArrayList<Model> models = getModelsToTest();

        // Measure execution time for all models
        measureExecutionTime(models, question);

    }

    public static ArrayList<Model> getModelsToTest() {
        //Define models to test for Execution time
        ArrayList<String> wordnetResources = new ArrayList<>();
        wordnetResources.add("synonyms");
        wordnetResources.add("antonyms");
        wordnetResources.add("hypernyms");

        HashMap<String, Float> model_weights = new HashMap<>();
        model_weights.put("wordnet", 0.6f);
        model_weights.put("word2vec", 0.4f);

        BaselineModel baseline = new BaselineModel("Baseline model (Jaccard Similarity)", all_comments);
        WordnetModel wordnet = new WordnetModel("Wordnet model", wordnet_dict, wordnetResources, all_comments);
        Word2vecModel word2vec = new Word2vecModel("Word2vec model", word_movers, w2_vector, all_comments);
        WordnetWord2vecModel combination = new WordnetWord2vecModel("Word2vec and Wordnet", wordnet_dict, wordnetResources, word_movers, w2_vector, model_weights, all_comments);

        ArrayList<Model> models = new ArrayList<>();
        models.add(baseline);
        models.add(wordnet);
        models.add(word2vec);
        models.add(combination);

        return models;
    }

    public static void measureInitTime() throws MalformedURLException, IOException, RepositoryException, MalformedQueryException, QueryEvaluationException {

        //Intance of class timer, for time measurements
        Timer timer = new Timer();

        // Create the list of stopWords to use
        StringUtils.generateStopLists(filePath_en, filePath_gr);

        // Create Word2Vec model
        File gModel = new File("C:/Users/Sgo/Desktop/Developer/Vector Models/GoogleNews-vectors-negative300.bin.gz");

        timer.start();
        Word2Vec vec = WordVectorSerializer.readWord2VecModel(gModel);
        w2_vector = vec;
        WordMovers wm = WordMovers.Builder().wordVectors(vec).build();
        word_movers = wm;
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
        wordnet_dict = dict;
        // open WordNet dictionary
        dict.open();
        timer.end();
        long wordNetTime = timer.getTotalTime();
        System.out.println("Time to load WordNet: " + wordNetTime);

        timer.start();
        QAInfoBase KB = new QAInfoBase();
        // Retrieve hotels
        HashSet<Subject> reviews = KB.getAllSubjectsOfType("hip", "review");

        ArrayList<Comment> comments = getCommentsFromTextOnlyKB(reviews);
        all_comments = comments;
        timer.end();
        long resourcesTime = timer.getTotalTime();
        System.out.println("Time to load resources: " + resourcesTime);
    }

    public static void measureExecutionTime(ArrayList<Model> models, String question) throws MalformedURLException, IOException, RepositoryException, MalformedQueryException, QueryEvaluationException {
        //Intance of class timer, for time measurements
        Timer timer = new Timer();

        wordnet_dict.open();

        for (Model model : models) {
            timer.start();
            model.scoreComments(question);
            timer.end();
            long calculateAllScores = timer.getTotalTime();
            System.out.println("Execution time for model: " + model.getDescription());
            System.out.println("Total Time to calculate score (for all reviews): " + calculateAllScores);
            System.out.println("Average Time to calculate score (for one review): " + calculateAllScores / all_comments.size());
        }

    }
}
