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
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import gr.forth.ics.isl.demo.models.Model;
import gr.forth.ics.isl.demo.models.WordnetWord2vecModel;
import gr.forth.ics.isl.demo.models.WordnetWord2vecModel_III;
import gr.forth.ics.isl.demoCombined.main.combinedDemoMain;
import gr.forth.ics.isl.main.demo_main;
import gr.forth.ics.isl.nlp.models.Comment;
import gr.forth.ics.isl.sailInfoBase.QAInfoBase;
import gr.forth.ics.isl.utilities.StringUtils;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Properties;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.word2vec.Word2Vec;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.RepositoryException;

/**
 *
 * @author Sgo
 */
public class OnFocusRRR {

    //The paths for the stopWords Files.
    public static String filePath_en = "/stoplists/stopwordsEn.txt";
    public static String filePath_gr = "/stoplists/stopwordsGr.txt";

    public Model combination; // Model instance
    public QAInfoBase KB; // Knowledge base instance
    public static StanfordCoreNLP pipeline; // Stanford coreNLP pipline instance

    /**
     * This method initializes the review retrieval model with respect to the
     * input configuration file. Note that the file should be place in
     * /src/main/resources/configuration/ folder.
     *
     * @param configFilePath: holds the properties of the model to be
     * initialized
     * @throws RepositoryException
     * @throws IOException
     */
    public OnFocusRRR(String configFile) throws RepositoryException, IOException {
        Properties properties = new Properties();

        properties.load(OnFocusRRR.class.getResourceAsStream("/configuration/" + configFile));

        String gModelPath = properties.getProperty("gModelPath");
        String wnhomePath = properties.getProperty("wnhomePath");
        ArrayList<String> contextWords = null;
        if (properties.getProperty("cwList") != null) {
            contextWords = new ArrayList<String>(Arrays.asList(properties.getProperty("cwList").split(",")));
        }
        float word2vec_w = Float.valueOf(properties.getProperty("word2vec_w"));
        float wordNet_w = Float.valueOf(properties.getProperty("wordNet_w"));
        boolean sqe = Boolean.valueOf(properties.getProperty("sqe"));
        ArrayList<String> wordnet_resources = new ArrayList<String>(Arrays.asList(properties.getProperty("wordnet_resources").split(",")));

        initialize(gModelPath, wnhomePath, contextWords, word2vec_w, wordNet_w, sqe, wordnet_resources);
    }

    /**
     * This method initializes the review retrieval model with respect to the
     * input properties Object. Should be used when the model is used
     * programmatically.
     *
     * @param configFilePath: holds the properties of the model to be
     * initialized
     * @throws RepositoryException
     * @throws IOException
     */
    public OnFocusRRR(Properties properties) throws RepositoryException, IOException {

        String gModelPath = properties.getProperty("gModelPath");
        String wnhomePath = properties.getProperty("wnhomePath");
        ArrayList<String> contextWords = null;
        if (properties.getProperty("cwList") != null) {
            contextWords = new ArrayList<String>(Arrays.asList(properties.getProperty("cwList").split(",")));
        }
        float word2vec_w = Float.valueOf(properties.getProperty("word2vec_w"));
        float wordNet_w = Float.valueOf(properties.getProperty("wordNet_w"));
        boolean sqe = Boolean.valueOf(properties.getProperty("sqe"));
        ArrayList<String> wordnet_resources = new ArrayList<String>(Arrays.asList(properties.getProperty("wordnet_resources").split(",")));

        initialize(gModelPath, wnhomePath, contextWords, word2vec_w, wordNet_w, sqe, wordnet_resources);
    }

    /**
     * This method automatically called from the constructor and instantiates
     * the review retrieval model with respect to the input properties i.e. its
     * parameters.
     *
     * @param word2vecPath: path of word2vec model + model.
     * @param wordnetPath: path of Wordnet home. (should be enviromental
     * variable)
     * @param cw_list: list of context words
     * @param word2vec_w: weight of word2vec retrieval component
     * @param wordNet_w: weight of wordnet retrieval component
     * @param sqe: true to use SQE, false not to use SQE
     * @param wordnetResources: wordnet resources to be used for the retrieval.
     * e.g. antonyms,hypernyms
     * @throws RepositoryException
     * @throws IOException
     */
    public void initialize(String word2vecPath, String wordnetPath, ArrayList<String> cw_list, float word2vec_w, float wordNet_w, boolean sqe, ArrayList<String> wordnetResources) throws RepositoryException, IOException {
        Logger.getLogger(OnFocusRRR.class.getName()).log(Level.INFO, "...loading KB...");
        KB = new QAInfoBase(); // initialize KB based on src/main/resources/warehouse files


        StringUtils.generateStopListsFromExternalSource(filePath_en, filePath_gr); // generate stop word list
        //System.out.println(stopLists);

        Logger.getLogger(OnFocusRRR.class.getName()).log(Level.INFO, "...loading word2vec...");
        File gModel = new File(word2vecPath);
        Word2Vec vec = WordVectorSerializer.readWord2VecModel(gModel); // initialize word2vec model
        WordMovers wm = WordMovers.Builder().wordVectors(vec).build(); // initialize WMD model

        Logger.getLogger(OnFocusRRR.class.getName()).log(Level.INFO, "...loading wordnet...");
        String wnhome = System.getenv(wordnetPath);
        String path = wnhome + File.separator + "dict";
        URL url = new URL("file", null, path);
        // construct the dictionary object and open it
        IDictionary dict = new Dictionary(url); // initialize wordnet dictionary
        dict.open();

        // Retrieve hyperparameters
        //ModelHyperparameters bestModel = (ModelHyperparameters) Utils.getSavedObject("AVEPbased_BestModel");
        //float word2vec_w = bestModel.getWord2vecWeight();
        //float wordNet_w = bestModel.getWordNetWeight();
//        float word2vec_w = 0.4f;
//        float wordNet_w = 0.6f;
        // Choose weights to be used in model IV
        HashMap<String, Float> model_weights = new HashMap<>();
        model_weights.put("wordnet", wordNet_w);
        model_weights.put("word2vec", word2vec_w);

        // Choose wordnet sources to be used
//        ArrayList<String> wordnetResources = new ArrayList<>();
//        wordnetResources.add("synonyms");
//        wordnetResources.add("antonyms");
//        wordnetResources.add("hypernyms");

//        ArrayList<String> cw_list = new ArrayList<>();
//        cw_list.add("problem");
//        cw_list.add("issue");
//        cw_list.add("report");
//        cw_list.add("hotel");
//        cw_list.add("complaint");
//        cw_list.add("anyone");
//        cw_list.add("complain");

        Logger.getLogger(OnFocusRRR.class.getName()).log(Level.INFO, "...initializing model...");
        // Instantiate model
        if (sqe) {
            combination = new WordnetWord2vecModel_III("Word2vec and Wordnet with SQE", dict, wordnetResources, wm, vec, model_weights);
            ((WordnetWord2vecModel_III) combination).setContextWords(cw_list);
        } else {
            combination = new WordnetWord2vecModel("Word2vec and Wordnet", dict, wordnetResources, wm, vec, model_weights);
            ((WordnetWord2vecModel) combination).setContextWords(cw_list);
        }

        Logger.getLogger(OnFocusRRR.class.getName()).log(Level.INFO, combination.getDescription() + " initialized");

        // initialize Stanford coreNLP pipeline
        Properties props = new Properties();
        props.put("annotators", "tokenize, ssplit, pos, lemma");
        props.put("tokenize.language", "en");
        combinedDemoMain.pipeline = new StanfordCoreNLP(props);
    }
    /**
     *
     * @param args args[0]: configuration file path + configuration file
     * @throws RepositoryException
     * @throws IOException
     * @throws MalformedQueryException
     * @throws QueryEvaluationException
     * @throws FileNotFoundException
     * @throws ClassNotFoundException
     */
    public static void main(String[] args) throws RepositoryException, IOException, MalformedQueryException, QueryEvaluationException, FileNotFoundException, ClassNotFoundException, JSONException {

        Properties properties = new Properties();
//        properties.setProperty("gModelPath", "C:/Users/Sgo/Desktop/Developer/Vector Models/GoogleNews-vectors-negative300.bin.gz");
//        properties.setProperty("wnhomePath", "WNHOME");
//        properties.setProperty("cwList", "problem,issue,report,hotel,complaint,anyone,complain");
//        properties.setProperty("word2vec_w", "0.4");
//        properties.setProperty("wordNet_w", "0.6");
//        properties.setProperty("sqe", "true");
//        properties.setProperty("wordnet_resources", "synonyms,antonyms,hypernyms");
//
//        OnFocusRRR RRRmodel = new OnFocusRRR(properties);

        OnFocusRRR RRRmodel = new OnFocusRRR("onFocusRRRConfig.properties");

        while (true) {
            try {
                Scanner scanner = new Scanner(System.in);
                System.out.println("Provide hotel ids");
                String urisAsString = scanner.nextLine();
                ArrayList<String> uris = new ArrayList<>(Arrays.asList(urisAsString.split(",")));

                System.out.println("Ask question");
                String question = scanner.nextLine();

                JSONObject resultListAsJASON = RRRmodel.getTopKComments(uris, question, 10);
                System.out.println();
                JSONArray maxSentences = resultListAsJASON.getJSONArray("maxSentences");
                for (int maxSentId = 0; maxSentId < maxSentences.length(); maxSentId++) {
                    System.out.println(maxSentences.get(maxSentId) + "\n");
                }

            } catch (JSONException ex) {
                Logger.getLogger(OnFocusRRR.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * Get top @k scored comments for input question @question, for the subset
     * of user reviews that are associated with uris @uris, where @k is defined
     * as input of the method (e.g. 2 for getting the two most similar reviews)
     *
     * @param uris
     * @param question
     * @param k
     * @return JSONObject
     * @throws RepositoryException
     * @throws MalformedQueryException
     * @throws QueryEvaluationException
     * @throws JSONException
     */
    public JSONObject getTopKComments(ArrayList<String> uris, String question, int k) throws RepositoryException, MalformedQueryException, QueryEvaluationException, JSONException {

        // Get comments for objects on focus
        ArrayList<Comment> comments = demo_main.getCommentsOnFocus(KB, uris);
        System.out.println(comments);
        // Score them
        combination.setComments(comments);
        combination.scoreComments(question);
        // Get top 2 comments and create JASON object
        JSONObject resultListAsJASON = getJASONObject(combination.getTopComments(k));

        return resultListAsJASON;
    }

    /**
     * Get top @2 scored comments for input question @question, for the subset
     * of user reviews that are associated with uris @uris.
     *
     * @param uris
     * @param question
     * @return JSONObject
     * @throws RepositoryException
     * @throws MalformedQueryException
     * @throws QueryEvaluationException
     * @throws JSONException
     */
    public JSONObject getTop2Comments(ArrayList<String> uris, String question) throws RepositoryException, MalformedQueryException, QueryEvaluationException, JSONException {

        // Get comments for objects on focus
        ArrayList<Comment> comments = demo_main.getCommentsOnFocus(KB, uris);
        System.out.println(comments);
        // Score them
        combination.setComments(comments);
        combination.scoreComments(question);
        // Get top 2 comments and create JASON object
        JSONObject resultListAsJASON = getJASONObject(combination.getTopComments(2));

        return resultListAsJASON;
        //return null;
    }

    /**
     * Transform and array list of reviews in a JSON Object. Object consists of
     * nine array lists. Each one represents a field of a review answer object,
     * i.e. id, max scored sentence, date, full text, positive sentiment part,
     * negative sentiment part, relevance score, hotel id, hotel name. All the
     * array lists are ordered with respect to the score field of the review
     * object in a way that the first index, i.e. [0], of each array list holds
     * the respective value of the most relevant review, the second index, i.e.
     * [1], holds the respective value of the second most relevant review, and
     * so on.
     *
     *
     * @param topComments
     * @return
     */
    private JSONObject getJASONObject(ArrayList<Comment> topComments) {

        ArrayList<String> commentIds = new ArrayList<>();
        ArrayList<String> maxSentences = new ArrayList<>();
        ArrayList<Date> dates = new ArrayList<>();
        ArrayList<String> fullReview = new ArrayList<>();
        ArrayList<String> posParts = new ArrayList<>();
        ArrayList<String> negParts = new ArrayList<>();
        ArrayList<Double> scores = new ArrayList<>();
        ArrayList<String> hotelIds = new ArrayList<>();
        ArrayList<String> hotelNames = new ArrayList<>();

        for (Comment com : topComments) {
            commentIds.add(com.getId()); // get comment id

            dates.add(com.getDate()); // get comment date

            maxSentences.add(com.getBestSentence()); // get best sentence

            fullReview.add(com.getText());

            String[] comParts = com.getText().split("-"); //split to pos and neg part
            posParts.add(comParts[1]); // get pos part
            negParts.add(comParts[0]); // get neg part

            scores.add(com.getScore()); // get score

            hotelIds.add(com.getHotelId()); // get hotel id

            hotelNames.add(com.getHotelName()); // get hotel name
        }

        try {
            JSONObject obj = new JSONObject();
            obj.put("commentIds", commentIds);
            obj.put("dates", dates);
            obj.put("maxSentences", maxSentences);
            obj.put("fullReview", fullReview);
            obj.put("posParts", posParts);
            obj.put("negParts", negParts);
            obj.put("scores", scores);
            obj.put("hotelIds", hotelIds);
            obj.put("hotelNames", hotelNames);

            return obj;
        } catch (JSONException ex) {
            Logger.getLogger(OnFocusRRR.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }
}
