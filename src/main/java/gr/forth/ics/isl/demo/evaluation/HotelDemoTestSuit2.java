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
import static gr.forth.ics.isl.demo.evaluation.EvalCollectionManipulator.readEvaluationSet;
import gr.forth.ics.isl.demo.evaluation.models.EvaluationPair;
import gr.forth.ics.isl.demo.evaluation.models.ModelHyperparameters;
import gr.forth.ics.isl.demo.models.Model;
import gr.forth.ics.isl.demo.models.Word2vecModel_IV;
import gr.forth.ics.isl.demoCombined.main.combinedDemoMain;
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
import java.util.Properties;
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
public class HotelDemoTestSuit2 {

    //static String evalFileName = "FRUCE_v2";
    static String evalFileName = "webAP";
    //static String evalFileName = "BookingEvalCollection";
    static String evalCollection = evalFileName + ".csv";

    //The paths for the stopWords Files.
    public static String filePath_en = "src/main/resources/stoplists/stopwordsEn.txt";
    public static String filePath_gr = "src/main/resources/stoplists/stopwordsGr.txt";

    //A hashMap that contains two Trie, one for the English stopList and one for the Greek
    public static HashMap<String, Trie> stopLists = new HashMap<>();

    public static void main(String[] args) throws RepositoryException, MalformedQueryException, QueryEvaluationException, IOException, FileNotFoundException, ClassNotFoundException {

        Timer overallTimer = new Timer();
        overallTimer.start();
        //Intance of class timer, for time measurements
        Timer timer = new Timer();

        // Create the list of stopWords to use
        StringUtils.generateStopLists(filePath_en, filePath_gr);

        Properties props = new Properties();
        props.put("annotators", "tokenize, ssplit, pos, lemma");
        props.put("tokenize.language", "en");
        combinedDemoMain.pipeline = new StanfordCoreNLP(props);
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
            relThreshold = 3;
        } else {
            relThreshold = 0;
            reviews = KB.getAllSubjectsOfType("hip", "review");
        }
        timer.end();
        long resourcesTime = timer.getTotalTime();
        System.out.println("Time to load resources: " + resourcesTime);

        System.out.println("External Resources were loaded successfully");

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
        HashMap<String, HashMap<String, EvaluationPair>> gt = readEvaluationSet(evalCollection);

        System.out.println("Num of Queries: " + gt.size());

        HashMap<String, String> queryList = getQueryList(gt); // retrieve list of queries

        ArrayList<String> contextWords = new ArrayList<>();
        if (evalCollection.contains("FRUCE")) {
            contextWords.add("problem");
            contextWords.add("issue");
            contextWords.add("report");
            contextWords.add("hotel");
            contextWords.add("complaint");
            contextWords.add("anyone");
            contextWords.add("complain");
        } else if (evalCollection.contains("webAP")) {
            contextWords.add("give");
            contextWords.add("information");
            contextWords.add("discribe");
            contextWords.add("state");
        }

//        ClassPathResource resource = new ClassPathResource("/trainedModels/doc2vec");
//        TokenizerFactory t = new DefaultTokenizerFactory();
//        t.setTokenPreProcessor(new CommonPreprocessor());
//
//        // we load externally originated model
//        ParagraphVectors vectors = WordVectorSerializer.readParagraphVectors(resource.getFile());
//        vectors.setTokenizerFactory(t);
//        vectors.getConfiguration().setIterations(20); // please note, we set iterations to 1 here, just to speedup inference
//        Doc2vecModel d2vModel = new Doc2vecModel("Doc2VecModel", vectors, t, resource, comments);
//        produceResults(d2vModel, queryList, gt, relThreshold); // produce result set of model
//        d2vModel = null;
//
//        BaselineModel baseline = new BaselineModel("Baseline model (Jaccard Similarity)", comments); // Instantiate baseline model
//        produceResults(baseline, queryList, gt, relThreshold); // produce result set of model
        //printResults(baseline.getDescription()); // print results
//        ModelStats stats = new ModelStats(baseline.getDescription());
//        stats.evaluate2(gt, relThreshold, evalFileName);
//        System.out.println(stats);
//        baseline = null;
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

        // Choose wordnet sources to be used
//        ArrayList<String> wordnetResources = new ArrayList<>();
//        wordnetResources.add("synonyms");
//        wordnetResources.add("antonyms");
//        wordnetResources.add("hypernyms");
//        WordnetModel wordnet = new WordnetModel("Wordnet model", dict, wordnetResources, comments);
//        produceResults(wordnet, queryList, gt, relThreshold); // produce result set of model
//        //printResults(baseline.getDescription()); // print results
////        stats = new ModelStats(wordnet.getDescription());
////        stats.evaluate2(gt, relThreshold, evalFileName);
////        System.out.println(stats);
//        wordnet = null;

//        WordnetModel_II wordnet_II = new WordnetModel_II("Wordnet model II", dict, wordnetResources, comments);
//        produceResults(wordnet_II, queryList, gt, relThreshold); // produce result set of model
//        //printResults(baseline.getDescription()); // print results
////        stats = new ModelStats(wordnet.getDescription());
////        stats.evaluate2(gt, relThreshold, evalFileName);
////        System.out.println(stats);
//        wordnet_II = null;
        // Create Word2Vec model
        timer.start();
        File gModel = new File("C:/Users/Sgo/Desktop/Developer/Vector Models/GoogleNews-vectors-negative300.bin.gz");
        Word2Vec vec = WordVectorSerializer.readWord2VecModel(gModel);
        WordMovers wm = WordMovers.Builder().wordVectors(vec).build();
        timer.end();
        long word2vecTime = timer.getTotalTime();
        System.out.println("Time to load word2vec: " + word2vecTime);

        // Retrieve hyperparameters
        ModelHyperparameters bestModel = (ModelHyperparameters) Utils.getSavedObject("AVEPbased_BestModel");
        float word2vec_w = bestModel.getWord2vecWeight();
        float wordNet_w = bestModel.getWordNetWeight();
        // Choose weights to be used in model IV
        HashMap<String, Float> model_weights = new HashMap<>();
        model_weights.put("wordnet", wordNet_w);
        model_weights.put("word2vec", word2vec_w);

//        WordnetWord2vecModel combination = new WordnetWord2vecModel("Word2vec and Wordnet", dict, wordnetResources, wm, vec, model_weights, comments);
//        produceResults(combination, queryList, gt, relThreshold); // produce result set of model
//        //printResults(baseline.getDescription()); // print results
////        stats = new ModelStats(combination.getDescription());
////        stats.evaluate2(gt, relThreshold, evalFileName);
////        System.out.println(stats);
//        combination = null;

//        WordnetWord2vecModel combination_cw = new WordnetWord2vecModel("Word2vec and Wordnet Context Words", dict, wordnetResources, wm, vec, model_weights, comments, contextWords);
//        if (evalCollection.contains("FRUCE")) {
//            produceResults(combination_cw, queryList, gt, relThreshold); // produce result set of model
//        } else if (evalCollection.contains("webAP")) {
//            produceBigResults(combination_cw, queryList, gt, relThreshold); // produce result set of model
//        }

        //printResults(baseline.getDescription()); // print results
//        stats = new ModelStats(combination.getDescription());
//        stats.evaluate2(gt, relThreshold, evalFileName);
//        System.out.println(stats);
//        combination_cw = null;

//        WordnetWord2vecModel_II combination_II = new WordnetWord2vecModel_II("Word2vec and Wordnet II", dict, wordnetResources, wm, vec, comments);
//        produceResults(combination_II, queryList, gt, relThreshold); // produce result set of model
////        printResults(baseline.getDescription()); // print results
////        stats = new ModelStats(combination_II.getDescription());
////        stats.evaluate2(gt, relThreshold, evalFileName);
////        System.out.println(stats);
//        combination_II = null;
//        WordnetWord2vecModel_III combination_III = new WordnetWord2vecModel_III("Word2vec and Wordnet III", dict, wordnetResources, wm, vec, model_weights, comments);
//        if (evalCollection.contains("FRUCE")) {
//            produceResults(combination_III, queryList, gt, relThreshold); // produce result set of model
//        } else if (evalCollection.contains("webAP")) {
//            produceBigResults(combination_III, queryList, gt, relThreshold); // produce result set of model
//        }
//        produceResults(combination_III, queryList, gt, relThreshold); // produce result set of model
//        printResults(baseline.getDescription()); // print results
//        stats = new ModelStats(combination_II.getDescription());
//        stats.evaluate2(gt, relThreshold, evalFileName);
//        System.out.println(stats);
//        combination_III = null;
////the code bellow is to concatenate and save partial results of produceBigResults() method.
//        HashMap<String, ArrayList<Integer>> allQueriesTestSet = new HashMap<>();
//        for (int cnt = 10; cnt <= 80; cnt += 10) {
//            HashMap<String, ArrayList<Integer>> partOfQueriesTestSet = (HashMap<String, ArrayList<Integer>>) Utils.getSavedObject("results_Word2vec and Wordnet Context Words" + "_" + evalFileName + "_" + cnt);
//            allQueriesTestSet.putAll(partOfQueriesTestSet);
//        }
//        Utils.saveObject(allQueriesTestSet, "results_Word2vec and Wordnet Context Words" + "_" + evalFileName);
//        System.out.println(allQueriesTestSet.size());

//        WordnetWord2vecModel_III combination_III_CW = new WordnetWord2vecModel_III("Word2vec and Wordnet III Context Words", dict, wordnetResources, wm, vec, model_weights, comments, contextWords);
//        if (evalCollection.contains("FRUCE")) {
//            produceResults(combination_III_CW, queryList, gt, relThreshold); // produce result set of model
//        } else if (evalCollection.contains("webAP")) {
//            produceBigResults(combination_III_CW, queryList, gt, relThreshold); // produce result set of model
//        }
////        printResults(baseline.getDescription()); // print results
////        stats = new ModelStats(combination_II.getDescription());
////        stats.evaluate2(gt, relThreshold, evalFileName);
////        System.out.println(stats);
//        combination_III_CW = null;
//        dict = null;

//        Word2vecModel word2vec = new Word2vecModel("Word2vec model", wm, vec, comments);
//        produceResults(word2vec, queryList, gt, relThreshold); // produce result set of model
//        //printResults(baseline.getDescription()); // print results
////        stats = new ModelStats(word2vec.getDescription());
////        stats.evaluate2(gt, relThreshold, evalFileName);
////        System.out.println(stats);
//        word2vec = null;
//        Word2vecModel_II word2vec_II = new Word2vecModel_II("Word2vec model II", wm, vec, comments);
//        produceResults(word2vec_II, queryList, gt, relThreshold); // produce result set of model
//        //printResults(baseline.getDescription()); // print results
//        //stats = new ModelStats(word2vec.getDescription());
//        //stats.evaluate2(gt, relThreshold, evalFileName);
//        //System.out.println(stats);
//        word2vec_II = null;

//        Word2vecModel word2vec_CW = new Word2vecModel("Word2vec model Context Words", wm, vec, comments, contextWords);
//        produceResults(word2vec_CW, queryList, gt, relThreshold); // produce result set of model
//        word2vec_CW = null;
//
//        Word2vecModel_III word2vec_III = new Word2vecModel_III("Word2vec model III", wm, vec, comments);
//        produceResults(word2vec_III, queryList, gt, relThreshold); // produce result set of model
//        word2vec_III = null;
//
//        Word2vecModel_III word2vec_III_CW = new Word2vecModel_III("Word2vec model III Context Words", wm, vec, comments, contextWords);
//        produceResults(word2vec_III_CW, queryList, gt, relThreshold); // produce result set of model
//        word2vec_III_CW = null;

        Word2vecModel_IV word2vec_IV_CW = new Word2vecModel_IV("Word2vec model IV Context Words", wm, vec, comments, contextWords);
        produceResults(word2vec_IV_CW, queryList, gt, relThreshold); // produce result set of model
        word2vec_IV_CW = null;
    }

    public static void produceResults(Model model, HashMap<String, String> queryList, HashMap<String, HashMap<String, EvaluationPair>> gt, int relThreshold) throws IOException {
        HashMap<String, ArrayList<Integer>> allQueriesTestSet = new HashMap<>();

        //for each query
        for (String qID : queryList.keySet()) {
            // Get the ground truth for the current query
            HashMap<String, EvaluationPair> evalPairsWithCrntQueryId = gt.get(qID);

            // Set R parameters of evaluation metrics
            int R2 = 2;
            int R = EvalCollectionManipulator.getNumOfRels(evalPairsWithCrntQueryId, relThreshold);
            //System.out.println("Query: " + qID + " Num of rels: " + R + "Total num of pairs: " + evalPairsWithCrntQueryId.size());

            // init test set for the current query
            ArrayList<Integer> testSet = new ArrayList<>();

            //Get the user's question
            String question = queryList.get(qID);

            model.scoreComments(question);
            ArrayList<Comment> rankedComments = model.getTopComments(model.getComments().size());

            // for all retrieved comments
            for (Comment resultCom : rankedComments) {
                // keep truck of comment's true and calculated relevance value
                // if comment is unjudged skip it
                EvaluationPair p = evalPairsWithCrntQueryId.get(resultCom.getId());
                if (p != null) {
                    if (resultCom.getScore() > 0.0001f) {
                        testSet.add(p.getRelevance()); // true binarry relevance
                        //System.out.println(p.getQuery().getId() + " == " + resultCom.getId() + " == " + resultCom.getScore() + " == " + p.getRelevance());
                    } else {
                        testSet.add(0); // true binarry relevance
                    }
                }
            }
            System.out.println(qID);
            allQueriesTestSet.put(qID, testSet);
        }
        Utils.saveObject(allQueriesTestSet, "results_" + model.getDescription() + "_" + evalFileName);
    }

    public static void produceBigResults(Model model, HashMap<String, String> queryList, HashMap<String, HashMap<String, EvaluationPair>> gt, int relThreshold) throws IOException {
        HashMap<String, ArrayList<Integer>> partOfQueriesTestSet;
        int cnt = 0;

        //for (int cntLim = 0; cntLim <= queryList.size(); cntLim += 5) {
        partOfQueriesTestSet = new HashMap<>();
        int downLim = 70;
        int upLim = downLim + 10;
        //for each query
        for (String qID : queryList.keySet()) {
            cnt++;
            if (cnt <= downLim) {
                continue;
            }

            // Get the ground truth for the current query
            HashMap<String, EvaluationPair> evalPairsWithCrntQueryId = gt.get(qID);

            //System.out.println("Query: " + qID + " Num of rels: " + R + "Total num of pairs: " + evalPairsWithCrntQueryId.size());
            // init test set for the current query
            ArrayList<Integer> testSet = new ArrayList<>();

            //Get the user's question
            String question = queryList.get(qID);

            model.scoreComments(question);
            ArrayList<Comment> rankedComments = model.getTopComments(model.getComments().size());

            // for all retrieved comments
            for (Comment resultCom : rankedComments) {
                // keep truck of comment's true and calculated relevance value
                // if comment is unjudged skip it
                EvaluationPair p = evalPairsWithCrntQueryId.get(resultCom.getId());
                if (p != null) {
                    if (resultCom.getScore() > 0.0001f) {
                        testSet.add(p.getRelevance()); // true binarry relevance
                        //System.out.println(p.getQuery().getId() + " == " + resultCom.getId() + " == " + resultCom.getScore() + " == " + p.getRelevance());
                    } else {
                        testSet.add(0); // true binarry relevance
                    }
                }
            }
            System.out.println(qID);
            partOfQueriesTestSet.put(qID, testSet);

            if (cnt == upLim) {
                break;
            }
        }
        Utils.saveObject(partOfQueriesTestSet, "results_" + model.getDescription() + "_" + evalFileName + "_" + cnt);
        partOfQueriesTestSet = null;
//        }
//
//        //the code bellow is to concatenate and save partial results of produceBigResults() method.
//        HashMap<String, ArrayList<Integer>> allQueriesTestSet = new HashMap<>();
//        for (int i = 10; i <= 80; i += 10) {
//
//            try {
//                partOfQueriesTestSet = (HashMap<String, ArrayList<Integer>>) Utils.getSavedObject("results_" + model.getDescription() + "_" + evalFileName + "_" + i);
//                allQueriesTestSet.putAll(partOfQueriesTestSet);
//            } catch (FileNotFoundException ex) {
//                Logger.getLogger(HotelDemoTestSuit2.class.getName()).log(Level.SEVERE, null, ex);
//            } catch (ClassNotFoundException ex) {
//                Logger.getLogger(HotelDemoTestSuit2.class.getName()).log(Level.SEVERE, null, ex);
//            }
//
//        }
//        Utils.saveObject(allQueriesTestSet, "results_" + model.getDescription() + "_" + evalFileName);
//        System.out.println(allQueriesTestSet.size());
    }

    public static HashMap<String, String> getQueryList(HashMap<String, HashMap<String, EvaluationPair>> gt) {
        HashMap<String, String> queryList = new HashMap<>();

        for (String query_id : gt.keySet()) {
            HashMap<String, EvaluationPair> evalPairs = gt.get(query_id);
            queryList.put(query_id, evalPairs.values().iterator().next().getQuery().getText());
        }

        return queryList;
    }

    public static void printResults(String name) throws IOException, IOException, FileNotFoundException, FileNotFoundException, ClassNotFoundException {
        HashMap<String, ArrayList<Integer>> allQueriesTestSet = (HashMap<String, ArrayList<Integer>>) Utils.getSavedObject("results_" + name);
        for (String qID : allQueriesTestSet.keySet()) {
            System.out.println("Query: " + qID + " Test Set: " + allQueriesTestSet.get(qID));
        }
    }
}
