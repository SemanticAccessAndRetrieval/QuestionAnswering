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
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import gr.forth.ics.isl.demo.auxiliaryClasses.Timer;
import gr.forth.ics.isl.demo.evaluation.models.EvaluationPair;
import gr.forth.ics.isl.demo.evaluation.models.ModelHyperparameters;
import gr.forth.ics.isl.demo.models.Model;
import gr.forth.ics.isl.demo.models.Word2vecModel;
import gr.forth.ics.isl.demo.models.Word2vecModel_III;
import gr.forth.ics.isl.demo.models.WordnetModel;
import gr.forth.ics.isl.demo.models.WordnetWord2vecModel;
import gr.forth.ics.isl.demo.models.WordnetWord2vecModel_III;
import gr.forth.ics.isl.demoCombined.main.combinedDemoMain;
import static gr.forth.ics.isl.main.demo_main.getCommentsFromBooking;
import gr.forth.ics.isl.nlp.models.Comment;
import gr.forth.ics.isl.sailInfoBase.QAInfoBase;
import gr.forth.ics.isl.sailInfoBase.models.Subject;
import gr.forth.ics.isl.utilities.StringUtils;
import gr.forth.ics.isl.utilities.Utils;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Properties;
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

    public static void main(String[] args) throws RepositoryException, MalformedQueryException, QueryEvaluationException, IOException, FileNotFoundException, ClassNotFoundException {

        StringUtils.generateStopLists(StringUtils.filePath_en, StringUtils.filePath_gr);
        QAInfoBase KB = new QAInfoBase();

        HashSet<Subject> reviews = KB.getAllSubjectsOfType("hip", "review");
        ArrayList<Comment> comments = getCommentsFromBooking(reviews);
        System.out.println(comments.size());

        File gModel = new File("C:/Users/Sgo/Desktop/Developer/Vector Models/GoogleNews-vectors-negative300.bin.gz");
        Word2Vec vec = WordVectorSerializer.readWord2VecModel(gModel);
        WordMovers wm = WordMovers.Builder().wordVectors(vec).build();

        // Retrieve hyperparameters
        ModelHyperparameters bestModel = (ModelHyperparameters) Utils.getSavedObject("AVEPbased_BestModel");
        float word2vec_w = bestModel.getWord2vecWeight();
        float wordNet_w = bestModel.getWordNetWeight();
        // Choose weights to be used in model IV
        HashMap<String, Float> model_weights = new HashMap<>();
        model_weights.put("wordnet", wordNet_w);
        model_weights.put("word2vec", word2vec_w);

        Properties props = new Properties();
        props.put("annotators", "tokenize, ssplit, pos, lemma");
        props.put("tokenize.language", "en");
        combinedDemoMain.pipeline = new StanfordCoreNLP(props);

        ArrayList<String> contextWords = new ArrayList<>();
        contextWords.add("problem");
        contextWords.add("issue");
        contextWords.add("report");
        contextWords.add("hotel");
        contextWords.add("complaint");
        contextWords.add("anyone");
        contextWords.add("complain");

        // Create WodNet Dictionary
        String wnhome = System.getenv("WNHOME");
        String path = wnhome + File.separator + "dict";
        URL url = new URL("file", null, path);
        // construct the dictionary object and open it
        IDictionary dict = new Dictionary(url);
        // open WordNet dictionary
        dict.open();

        // Choose wordnet sources to be used
        ArrayList<String> wordnetResources = new ArrayList<>();
        wordnetResources.add("synonyms");
        wordnetResources.add("antonyms");
        wordnetResources.add("hypernyms");

        HashMap<String, HashMap<String, EvaluationPair>> gt = EvalCollectionManipulator.readEvaluationSet("FRUCE_v2.csv");

        WordnetModel wordnet = new WordnetModel("Wordnet", dict, wordnetResources, comments);
        Word2vecModel word2vec = new Word2vecModel("Word2vec model", wm, vec, comments);
        Word2vecModel word2vec_cw = new Word2vecModel("Word2vec model Context Words", wm, vec, comments, contextWords);
        WordnetWord2vecModel combination = new WordnetWord2vecModel("Word2vec and Wordnet", dict, wordnetResources, wm, vec, model_weights, comments);
        WordnetWord2vecModel combination_cw = new WordnetWord2vecModel("Word2vec and Wordnet Context Words", dict, wordnetResources, wm, vec, model_weights, comments, contextWords);
        Word2vecModel_III word2vec_III = new Word2vecModel_III("Word2vec model III", wm, vec, comments);
        Word2vecModel_III word2vec_III_cw = new Word2vecModel_III("Word2vec model III Context Words", wm, vec, comments, contextWords);
        WordnetWord2vecModel_III combination_III = new WordnetWord2vecModel_III("Word2vec and Wordnet III", dict, wordnetResources, wm, vec, model_weights, comments);
        WordnetWord2vecModel_III combination_III_cw = new WordnetWord2vecModel_III("Word2vec and Wordnet III Context Words", dict, wordnetResources, wm, vec, model_weights, comments, contextWords);

        ArrayList<Model> models = new ArrayList<>();
        models.add(wordnet);
        models.add(word2vec);
        models.add(word2vec_cw);
        models.add(combination);
        models.add(combination_cw);
        models.add(word2vec_III);
        models.add(word2vec_III_cw);
        models.add(combination_III);
        models.add(combination_III_cw);

        ArrayList<String> queryList = new ArrayList<>();
        queryList.add("Has anyone reported a problem about noise?");
        queryList.add("Is this hotel quiet?");
        queryList.add("Has anyone reported a problem about cleanliness?");
        queryList.add("Has anyone complained about the bed linen?");
        queryList.add("Is the personnel polite?");
        queryList.add("Is the hotel staff helpful?");

        // Measure execution time for all models
        measureExecutionTime(models, queryList);

    }

    public static void measureExecutionTime(ArrayList<Model> models, ArrayList<String> queryList) throws MalformedURLException, IOException, RepositoryException, MalformedQueryException, QueryEvaluationException {
        //Intance of class timer, for time measurements
        Timer timer = new Timer();

        for (Model model : models) {

            ArrayList<Long> timesPerQuery = new ArrayList<>();

            for (String query : queryList) {
                timer.start();
                model.scoreComments(query);
                timer.end();
                timesPerQuery.add(timer.getTotalTime());
            }

            Long totalTime = 0L;
            for (Long time : timesPerQuery) {
                totalTime += time;
            }

            //long calculateAllScores = timer.getTotalTime();
            System.out.println("Model: " + model.getDescription());
            System.out.println("Total Time to calculate score for all reviews over all queries: " + totalTime);
            System.out.println("Average Time to calculate score for all reviews over one query: " + totalTime / queryList.size());
            System.out.println("Average Time to calculate score for one review over one query: " + (totalTime / queryList.size()) / model.getComments().size());
        }

    }
}
