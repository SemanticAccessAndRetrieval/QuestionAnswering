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
import gr.forth.ics.isl.demo.evaluation.models.EvaluationPair;
import gr.forth.ics.isl.demo.evaluation.models.EvaluationResult;
import gr.forth.ics.isl.demo.models.Model;
import gr.forth.ics.isl.demo.models.WordnetWord2vecModel;
import gr.forth.ics.isl.demoCombined.main.combinedDemoMain;
import gr.forth.ics.isl.nlp.models.Comment;
import gr.forth.ics.isl.utilities.StringUtils;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.word2vec.Word2Vec;

/**
 *
 * @author Sgo
 */
public class WebAPTrainSuitHQ implements Runnable {

    static String evalCollection = "webAP.csv";
    static String resultFileName = "resultFileName";
    static String resultFilePath = "resultFilePath";
    static String wnhomePath = "wnhomePath";
    static String gModelPath = "gModelPath";

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException, FileNotFoundException, ClassNotFoundException {

        ////////////////////SET PROPERTIES/////////////////////////////////////
        Properties properties = new Properties();
        properties.load(WebAPTrainSuitHQ.class.getResourceAsStream("/configuration/userReviewsExperimentsHQConfig.properties"));

        resultFileName = properties.getProperty(resultFileName);
        resultFilePath = properties.getProperty(resultFilePath);
        wnhomePath = properties.getProperty(wnhomePath);
        gModelPath = properties.getProperty(gModelPath);

        System.out.println(evalCollection);
        System.out.println(resultFileName);
        System.out.println(resultFilePath);
        System.out.println(wnhomePath);
        System.out.println(gModelPath);

        //////////////////////WORD2VEC//////////////////////////////////////////
        File gModel = new File(gModelPath);
        Word2Vec vec = WordVectorSerializer.readWord2VecModel(gModel);
        WordMovers wm = WordMovers.Builder().wordVectors(vec).build();

        System.out.println("Statistical resources were loaded successfully");

        //////////////////////WORDNET///////////////////////////////////////////
        // Create WodNet Dictionary
        String wnhome = System.getenv(wnhomePath);
        String path = wnhome + File.separator + "dict";
        URL url = new URL("file", null, path);
        // construct the dictionary object and open it
        IDictionary dict = new Dictionary(url);
        // open WordNet dictionary
        dict.open();

        System.out.println("Handmade resources were loaded successfully");

        ////////////////////////////////////////////////////////////////////////
        // Create the list of stopWords to use
        StringUtils.generateStopListsFromExternalSource(combinedDemoMain.filePath_en, combinedDemoMain.filePath_gr);

        // configure and initialize SCNLP pipeline
        Properties props = new Properties();
        props.put("annotators", "tokenize, ssplit, pos, lemma");
        props.put("tokenize.language", "en");
        combinedDemoMain.pipeline = new StanfordCoreNLP(props);

        System.out.println("SCNLP pipeline initialized successfully");

        ArrayList<Comment> passages = getPassagesFromWebAPExternal(); // load passages

        System.out.println("Passages were loaded successfully");

        // ground truth relevance between each query-passage pair
        HashMap<String, HashMap<String, EvaluationPair>> gt = EvalCollectionManipulator.readEvaluationSetExternal(evalCollection);

        System.out.println("Num of Queries: " + gt.size());

        HashMap<String, String> queryList = getQueryList(gt); // retrieve list of queries

        System.out.println("Query list was loaded successfully");

        // context words
        ArrayList<String> contextWords = new ArrayList<>();
        contextWords.add("give");
        contextWords.add("information");
        contextWords.add("discribe");
        contextWords.add("state");

        System.out.println("Context words were loaded successfully");

        HashMap<String, Float> model_weights = new HashMap<>();
        ArrayList<HashMap<String, Float>> possible_model_weights = new ArrayList<>();
        model_weights = new HashMap<>();
        model_weights.put("wordnet", 0.0f);
        model_weights.put("word2vec", 1.0f);
        possible_model_weights.add(model_weights);
        model_weights = new HashMap<>();
        model_weights.put("wordnet", 0.1f);
        model_weights.put("word2vec", 0.9f);
        possible_model_weights.add(model_weights);
        model_weights = new HashMap<>();
        model_weights.put("wordnet", 0.2f);
        model_weights.put("word2vec", 0.8f);
        possible_model_weights.add(model_weights);
        model_weights = new HashMap<>();
        model_weights.put("wordnet", 0.3f);
        model_weights.put("word2vec", 0.7f);
        possible_model_weights.add(model_weights);
        model_weights = new HashMap<>();
        model_weights.put("wordnet", 0.4f);
        model_weights.put("word2vec", 0.6f);
        possible_model_weights.add(model_weights);
        model_weights = new HashMap<>();
        model_weights.put("wordnet", 0.5f);
        model_weights.put("word2vec", 0.5f);
        possible_model_weights.add(model_weights);
        model_weights = new HashMap<>();
        model_weights.put("wordnet", 0.6f);
        model_weights.put("word2vec", 0.4f);
        possible_model_weights.add(model_weights);
        model_weights = new HashMap<>();
        model_weights.put("wordnet", 0.7f);
        model_weights.put("word2vec", 0.3f);
        possible_model_weights.add(model_weights);
        model_weights = new HashMap<>();
        model_weights.put("wordnet", 0.8f);
        model_weights.put("word2vec", 0.2f);
        possible_model_weights.add(model_weights);
        model_weights = new HashMap<>();
        model_weights.put("wordnet", 0.9f);
        model_weights.put("word2vec", 0.1f);
        possible_model_weights.add(model_weights);
        model_weights = new HashMap<>();
        model_weights.put("wordnet", 1.0f);
        model_weights.put("word2vec", 0.0f);
        possible_model_weights.add(model_weights);

        for (HashMap<String, Float> mw : possible_model_weights) {
            System.out.println("wordnet weight: " + mw.get("wordnet"));
            System.out.println("word2vec weight: " + mw.get("word2vec"));
            System.out.println("");

            Thread crntThread = new Thread() {
                @Override
                public void run() {

                    ArrayList<String> wordnetResources = new ArrayList<>();
                    wordnetResources.add("synonyms");
                    wordnetResources.add("antonyms");
                    wordnetResources.add("hypernyms");
                    WordnetWord2vecModel model = new WordnetWord2vecModel("CMB", dict, wordnetResources, wm, vec, mw, new ArrayList<>(passages));

                    System.out.println(model.getDescription() + " was initialized successfully");

                    try {
                        produceBigResultsWebAPHQ(model, queryList, gt, mw);
                    } catch (IOException ex) {
                        Logger.getLogger(WebAPTrainSuitHQ.class.getName()).log(Level.SEVERE, null, ex);
                    }

                    System.out.println(model.getDescription() + " was evaluated successfully");

                }

            };
            crntThread.start();

        }

        System.out.println("Evaluation was completed successfully");

    }

    public static void produceResultsWebAPHQ(Model model, HashMap<String, String> queryList, HashMap<String, HashMap<String, EvaluationPair>> gt) throws IOException {

        int pairID = 1;

        //for each query
        for (String qID : queryList.keySet()) {

            // Get the ground truth for the current query
            HashMap<String, EvaluationPair> evalPairsWithCrntQueryId = gt.get(qID);

            // init test set for the current query
            ArrayList<EvaluationResult> resultSet = new ArrayList<>();

            //Get the user's question
            String question = queryList.get(qID);

            model.scoreComments(question);
            ArrayList<Comment> rankedPassages = model.getTopComments(model.getComments().size());

            // for all retrieved comments
            for (Comment resultCom : rankedPassages) {
                // keep truck of comment's true and calculated relevance value
                // if comment is unjudged skip it
                EvaluationPair p = evalPairsWithCrntQueryId.get(resultCom.getId());
                if (p != null) {
                    String bestSentence = "none";
                    if (!resultCom.getBestSentence().equals("")) {
                        bestSentence = resultCom.getBestSentence();
                    }
                    EvaluationResult result = new EvaluationResult(pairID, Integer.valueOf(p.getQuery().getId()), p.getQuery().getText(), Integer.valueOf(p.getComment().getId()), p.getComment().getText(), resultCom.getScore(), bestSentence);
                    pairID++;

                    result.setPairRelevance(Integer.valueOf(p.getRelevance()));
                    resultSet.add(result); // true binarry relevance

                }
            }

            writeResultsToFile(resultSet, model.getDescription());
            System.out.println(qID);
        }
    }

    public static void produceBigResultsWebAPHQ(Model model, HashMap<String, String> queryList, HashMap<String, HashMap<String, EvaluationPair>> gt, HashMap<String, Float> modelWeights) throws IOException {

        //int pairID = 8203;
        int pairID = 1;

        //for each query
        for (String qID : queryList.keySet()) {

//            if (qID.equals("709") || qID.equals("711") || qID.equals("712") || qID.equals("713") || qID.equals("714")
//                    || qID.equals("716") || qID.equals("722") || qID.equals("723") || qID.equals("724") || qID.equals("725")
//                    || qID.equals("727") || qID.equals("729") || qID.equals("730") || qID.equals("733") || qID.equals("735")
//                    || qID.equals("737") || qID.equals("738") || qID.equals("739") || qID.equals("740") || qID.equals("741")
//                    || qID.equals("750") || qID.equals("751") || qID.equals("753") || qID.equals("755") || qID.equals("757")
//                    || qID.equals("760") || qID.equals("761") || qID.equals("763") || qID.equals("770") || qID.equals("771")
//                    || qID.equals("772") || qID.equals("776") || qID.equals("782") || qID.equals("792") || qID.equals("794")
//                    || qID.equals("795") || qID.equals("798") || qID.equals("801") || qID.equals("802") || qID.equals("804")
//                    || qID.equals("806") || qID.equals("808") || qID.equals("810") || qID.equals("811") || qID.equals("812")
//                    || qID.equals("816") || qID.equals("817") || qID.equals("818") || qID.equals("830") || qID.equals("831")
//                    || qID.equals("834") || qID.equals("835") || qID.equals("836") || qID.equals("837") || qID.equals("838")
//                    || qID.equals("841") || qID.equals("843") || qID.equals("846") || qID.equals("847") || qID.equals("849")
//                    || qID.equals("850")) {
//
//                continue;
//            }

            // Get the ground truth for the current query
            HashMap<String, EvaluationPair> evalPairsWithCrntQueryId = gt.get(qID);

            // Get the user's question
            String question = queryList.get(qID);

            // Rank the passages according to the input question
            model.scoreComments(question);
            ArrayList<Comment> rankedPassages = model.getTopComments(model.getComments().size());

            // for all retrieved passages
            for (Comment resultCom : rankedPassages) {
                // keep truck of comment's true and calculated relevance value
                // if comment is unjudged skip it
                EvaluationPair p = evalPairsWithCrntQueryId.get(resultCom.getId());
                if (p != null) {
                    String bestSentence = "none";
                    if (!resultCom.getBestSentence().equals("")) {
                        bestSentence = resultCom.getBestSentence();
                    }
                    EvaluationResult result = new EvaluationResult(pairID, Integer.valueOf(p.getQuery().getId()), p.getQuery().getText(), Integer.valueOf(p.getComment().getId()), p.getComment().getText(), resultCom.getScore(), bestSentence);
                    pairID++;

                    result.setPairRelevance(Integer.valueOf(p.getRelevance()));
                    writeResultToFile(result, modelWeights); // fake false positive

                }
            }

            System.out.println(qID);
        }
    }

    public static void writeResultToFile(EvaluationResult result, HashMap<String, Float> modelWeights) throws IOException {

        String crntWeightsFolder = "wn" + modelWeights.get("wordnet") + "w2v" + modelWeights.get("word2vec") + "/";
        String folderPath = resultFilePath + crntWeightsFolder;
        File folder = new File(folderPath);

        if (!folder.exists()) {
            folder.mkdirs();
        }

        File file = new File(folder.getPath() + "/" + result.getQueryId() + ".tsv");
        BufferedWriter writer;

        if (file.exists()) {
            writer = new BufferedWriter(new FileWriter(file, true));
        } else {
            writer = new BufferedWriter(new FileWriter(file));
        }

        writer.write(result.toString());

        writer.close();
    }

    public static void writeResultsToFile(ArrayList<EvaluationResult> results, String model) throws IOException {

        File file = new File(resultFilePath + resultFileName + "_" + model + ".tsv");
        BufferedWriter writer;

        if (file.exists()) {
            writer = new BufferedWriter(new FileWriter(resultFilePath + resultFileName + "_" + model + ".tsv", true));
        } else {
            writer = new BufferedWriter(new FileWriter(resultFilePath + resultFileName + "_" + model + ".tsv"));
        }

        for (EvaluationResult er : results) {
            writer.write(er.toString());
        }

        writer.close();
    }

    public static void printEvaluationCollection(HashMap<String, HashMap<String, EvaluationPair>> evalCollection) {
        for (String queryId : evalCollection.keySet()) {
            for (String pairId : evalCollection.get(queryId).keySet()) {
                EvaluationPair pair = evalCollection.get(queryId).get(pairId);
                System.out.println("qID: " + queryId + " pairID: " + pairId + " pairRel: " + pair.getRelevance());
                System.out.println(pair.getQuery().getText());
                System.out.println(pair.getComment().getText());
                System.out.println("");
            }
        }
    }

    public static ArrayList<Comment> getPassagesFromWebAP() {
        ArrayList<Comment> comments = new ArrayList<>();

        String corpusPath = "src/main/resources/corpus/";
        String csvFile = corpusPath + "webAP.txt";

        BufferedReader br = null;
        String line = "";
        String cvsSplitBy = ";;";

        try {

            br = new BufferedReader(new FileReader(csvFile));
            while ((line = br.readLine()) != null) {

                // use comma as separator
                String[] comment = line.split(cvsSplitBy);

                String commentId = comment[0];
                String commentText = comment[1];

                Comment tmpComment = new Comment("", "", commentId, commentText);
                comments.add(tmpComment);

            }

            return comments;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static ArrayList<Comment> getPassagesFromWebAPExternal() {
        ArrayList<Comment> comments = new ArrayList<>();

        String corpusPath = "/corpus/";
        String csvFile = corpusPath + "webAP.txt";

        String cvsSplitBy = ";;";

        InputStream is = WebAPTrainSuitHQ.class.getResourceAsStream(csvFile);
        ArrayList<String> lines = new ArrayList<>();

        try (Stream<String> stream = new BufferedReader(new InputStreamReader(is, "UTF-8")).lines()) {
            stream.forEach(line -> lines.add(line));
            for (String line : lines) {
                // use comma as separator
                String[] comment = line.split(cvsSplitBy);

                String commentId = comment[0];
                String commentText = comment[1];

                Comment tmpComment = new Comment("", "", commentId, commentText);
                comments.add(tmpComment);
            }

            //System.out.println(comments);
            return comments;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static HashMap<String, String> getQueryList(HashMap<String, HashMap<String, EvaluationPair>> gt) {
        HashMap<String, String> queryList = new HashMap<>();

        for (String query_id : gt.keySet()) {
            HashMap<String, EvaluationPair> evalPairs = gt.get(query_id);
            queryList.put(query_id, evalPairs.values().iterator().next().getQuery().getText());
        }

        return queryList;
    }

    @Override
    public void run() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
