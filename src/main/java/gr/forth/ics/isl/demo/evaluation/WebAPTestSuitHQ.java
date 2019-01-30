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
import gr.forth.ics.isl.demo.models.BaselineModel;
import gr.forth.ics.isl.demo.models.Model;
import gr.forth.ics.isl.demo.models.Word2vecModel;
import gr.forth.ics.isl.demo.models.Word2vecModel_III;
import gr.forth.ics.isl.demo.models.WordnetModel;
import gr.forth.ics.isl.demo.models.WordnetWord2vecModel;
import gr.forth.ics.isl.demo.models.WordnetWord2vecModel_III;
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
import java.util.stream.Stream;
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.word2vec.Word2Vec;

/**
 *
 * @author Sgo
 */
public class WebAPTestSuitHQ {

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
        properties.load(WebAPTestSuitHQ.class.getResourceAsStream("/configuration/userReviewsExperimentsHQConfig.properties"));

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

        // Retrieve hyperparameters
        //ModelHyperparameters bestModel = (ModelHyperparameters) Utils.getSavedObject("AVEPbased_BestModel");
        float word2vec_w = 0.4f;// bestModel.getWord2vecWeight();
        float wordNet_w = 0.6f;//bestModel.getWordNetWeight();
        // Choose weights to be used in combination model
        HashMap<String, Float> model_weights = new HashMap<>();
        model_weights.put("wordnet", wordNet_w);
        model_weights.put("word2vec", word2vec_w);

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

        // Choose wordnet sources to be used
        ArrayList<String> wordnetResources = new ArrayList<>();

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

        ArrayList<Comment> passages = new ArrayList<>();

        passages = getPassagesFromWebAPExternal(); // load passages

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

        // models to be evaluated
        ArrayList<String> models = new ArrayList<String>();
        models.add("BSL");
        models.add("WQE_woAH");
        models.add("WQE_woH");
        models.add("WQE");
        models.add("W2V");
        models.add("W2V_cw");
        models.add("W2V_sqe");
        models.add("W2V_cw_sqe");
        models.add("CMB");
        models.add("CMB_cw");
        models.add("CMB_sqe");
        models.add("CMB_cw_sqe");

        System.out.println("Model Names were loaded successfully");

        Model model = null; // model variable
//        ModelStats modelStats = null; // model statistics variable

        for (String modelName : models) {

            // initialize curent model
            if (modelName.equals("BSL")) {

                model = new BaselineModel("BSL", passages); // Instantiate baseline model

                System.out.println(model.getDescription() + " was initialized successfully");

            } else if (modelName.equals("WQE_woAH")) {

                wordnetResources = new ArrayList<>();
                wordnetResources.add("synonyms");
                model = new WordnetModel("WQE_woAH", dict, wordnetResources, passages);

                System.out.println(model.getDescription() + " was initialized successfully");

            } else if (modelName.equals("WQE_woH")) {

                wordnetResources = new ArrayList<>();
                wordnetResources.add("synonyms");
                wordnetResources.add("antonyms");
                model = new WordnetModel("WQE_woH", dict, wordnetResources, passages);

                System.out.println(model.getDescription() + " was initialized successfully");

            } else if (modelName.equals("WQE")) {

                wordnetResources = new ArrayList<>();
                wordnetResources.add("synonyms");
                wordnetResources.add("antonyms");
                wordnetResources.add("hypernyms");
                model = new WordnetModel("WQE", dict, wordnetResources, passages);

                System.out.println(model.getDescription() + " was initialized successfully");

            } else if (modelName.equals("W2V")) {

                model = new Word2vecModel("W2V", wm, vec, passages);

                System.out.println(model.getDescription() + " was initialized successfully");

            } else if (modelName.equals("W2V_cw")) {

                model = new Word2vecModel("W2V", wm, vec, passages);
                ((Word2vecModel) model).setContextWords(contextWords);

                System.out.println(model.getDescription() + " was initialized successfully");

            } else if (modelName.equals("W2V_sqe")) {

                model = new Word2vecModel_III("W2V", wm, vec, passages);

                System.out.println(model.getDescription() + " was initialized successfully");

            } else if (modelName.equals("W2V_cw_sqe")) {

                model = new Word2vecModel_III("W2V", wm, vec, passages);
                ((Word2vecModel_III) model).setContextWords(contextWords);

                System.out.println(model.getDescription() + " was initialized successfully");

            } else if (modelName.equals("CMB")) {

                wordnetResources = new ArrayList<>();
                wordnetResources.add("synonyms");
                wordnetResources.add("antonyms");
                wordnetResources.add("hypernyms");
                model = new WordnetWord2vecModel("CMB", dict, wordnetResources, wm, vec, model_weights, passages);

                System.out.println(model.getDescription() + " was initialized successfully");

            } else if (modelName.equals("CMB_cw")) {

                wordnetResources = new ArrayList<>();
                wordnetResources.add("synonyms");
                wordnetResources.add("antonyms");
                wordnetResources.add("hypernyms");
                model = new WordnetWord2vecModel("CMB_cw", dict, wordnetResources, wm, vec, model_weights, passages);
                ((WordnetWord2vecModel) model).setContextWords(contextWords);

                System.out.println(model.getDescription() + " was initialized successfully");
            } else if (modelName.equals("CMB_sqe")) {

                wordnetResources = new ArrayList<>();
                wordnetResources.add("synonyms");
                wordnetResources.add("antonyms");
                wordnetResources.add("hypernyms");
                model = new WordnetWord2vecModel_III("CMB_sqe", dict, wordnetResources, wm, vec, model_weights, passages);

                System.out.println(model.getDescription() + " was initialized successfully");

            } else if (modelName.equals("CMB_cw_sqe")) {

                wordnetResources = new ArrayList<>();
                wordnetResources.add("synonyms");
                wordnetResources.add("antonyms");
                wordnetResources.add("hypernyms");
                model = new WordnetWord2vecModel_III("CMB_cw", dict, wordnetResources, wm, vec, model_weights, passages);
                ((WordnetWord2vecModel_III) model).setContextWords(contextWords);

                System.out.println(model.getDescription() + " was initialized successfully");
            } else {
                System.out.println("There is no such model: " + modelName);
            }

            // produce result file of curent model
            produceBigResultsWebAPHQ(model, queryList, gt);
            System.out.println(model.getDescription() + " was evaluated successfully");
            // evaluate model
//            modelStats = new ModelStats(model.getDescription());
//            modelStats.evaluateWebAP(gt, threshold, resultFilePath + resultFileName + "_" + model.getDescription() + ".tsv");
//
//            System.out.println(model.getDescription() + " RESULTS:");
//            System.out.println(modelStats);

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
                    EvaluationResult result = new EvaluationResult(pairID, Integer.valueOf(p.getQuery().getId()), p.getQuery().getText(), Integer.valueOf(p.getComment().getId()), p.getComment().getText(), resultCom.getScore(), resultCom.getBestSentence());
                    pairID++;
                    if (resultCom.getScore() > 0.0001f) {
                        result.setPairRelevance(Integer.valueOf(p.getRelevance()));
                        resultSet.add(result); // true binarry relevance
                    } else {
                        result.setPairRelevance(Integer.valueOf(0));
                        resultSet.add(result); // fake false positive
                    }
                }
            }

            writeResultsToFile(resultSet, model.getDescription());
            System.out.println(qID);
        }
    }

    public static void produceBigResultsWebAPHQ(Model model, HashMap<String, String> queryList, HashMap<String, HashMap<String, EvaluationPair>> gt) throws IOException {

        int pairID = 1;

        //for each query
        for (String qID : queryList.keySet()) {

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
                    EvaluationResult result = new EvaluationResult(pairID, Integer.valueOf(p.getQuery().getId()), p.getQuery().getText(), Integer.valueOf(p.getComment().getId()), p.getComment().getText(), resultCom.getScore(), resultCom.getBestSentence());
                    pairID++;
                    if (resultCom.getScore() > 0.0001f) {
                        result.setPairRelevance(Integer.valueOf(p.getRelevance()));
                        writeResultToFile(result, model.getDescription()); // add relevance
                    } else {
                        result.setPairRelevance(Integer.valueOf(0));
                        writeResultToFile(result, model.getDescription()); // fake false positive
                    }
                }
            }

            System.out.println(qID);
        }
    }

    public static void writeResultToFile(EvaluationResult result, String model) throws IOException {

        File file = new File(resultFilePath + resultFileName + "_" + model + ".tsv");
        BufferedWriter writer;

        if (file.exists()) {
            writer = new BufferedWriter(new FileWriter(resultFilePath + resultFileName + "_" + model + ".tsv", true));
        } else {
            writer = new BufferedWriter(new FileWriter(resultFilePath + resultFileName + "_" + model + ".tsv"));
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

        InputStream is = WebAPTestSuitHQ.class.getResourceAsStream(csvFile);
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

            System.out.println(comments);
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

}
