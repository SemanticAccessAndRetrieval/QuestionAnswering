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
import gr.forth.ics.isl.demo.models.WordnetWord2vecModel;
import gr.forth.ics.isl.main.demo_main;
import gr.forth.ics.isl.nlp.models.Comment;
import gr.forth.ics.isl.sailInfoBase.QAInfoBase;
import gr.forth.ics.isl.utilities.StringUtils;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
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

    //Model instance
    public static WordnetWord2vecModel combination;
    public static QAInfoBase KB;
    public static StanfordCoreNLP pipeline;

    //A hashMap that contains two Trie, one for the English stopList and one for the Greek
    //public static HashMap<String, Trie> stopLists = new HashMap<>();

    public OnFocusRRR(String word2vecPath, String wordnetPath) throws RepositoryException, IOException {
        initialize(word2vecPath, wordnetPath);
    }
    
    public static void initialize(String word2vecPath, String wordnetPath) throws RepositoryException, IOException {
        Logger.getLogger(OnFocusRRR.class.getName()).log(Level.INFO, "...loading KB...");
        KB = new QAInfoBase();


        StringUtils.generateStopListsFromExternalSource(filePath_en, filePath_gr);
        //System.out.println(stopLists);

        Logger.getLogger(OnFocusRRR.class.getName()).log(Level.INFO, "...loading word2vec...");
        File gModel = new File(word2vecPath + "GoogleNews-vectors-negative300.bin.gz");
        Word2Vec vec = WordVectorSerializer.readWord2VecModel(gModel);
        WordMovers wm = WordMovers.Builder().wordVectors(vec).build();

        Logger.getLogger(OnFocusRRR.class.getName()).log(Level.INFO, "...loading wordnet...");
        String wnhome = System.getenv(wordnetPath);
        String path = wnhome + File.separator + "dict";
        URL url = new URL("file", null, path);
        // construct the dictionary object and open it
        IDictionary dict = new Dictionary(url);
        dict.open();

        // Retrieve hyperparameters
        //ModelHyperparameters bestModel = (ModelHyperparameters) Utils.getSavedObject("AVEPbased_BestModel");
        //float word2vec_w = bestModel.getWord2vecWeight();
        //float wordNet_w = bestModel.getWordNetWeight();
        float word2vec_w = 0.4f;
        float wordNet_w = 0.6f;
        // Choose weights to be used in model IV
        HashMap<String, Float> model_weights = new HashMap<>();
        model_weights.put("wordnet", wordNet_w);
        model_weights.put("word2vec", word2vec_w);

        // Choose wordnet sources to be used
        ArrayList<String> wordnetResources = new ArrayList<>();
        wordnetResources.add("synonyms");
        wordnetResources.add("antonyms");
        wordnetResources.add("hypernyms");

        Logger.getLogger(OnFocusRRR.class.getName()).log(Level.INFO, "...initializing model...");
        // Instantiate model
        combination = new WordnetWord2vecModel("Word2vec and Wordnet", dict, wordnetResources, wm, vec, model_weights);

        Properties props = new Properties();
        props.put("annotators", "tokenize, ssplit, pos, lemma");
        props.put("tokenize.language", "en");
        pipeline = new StanfordCoreNLP(props);
    }
    /**
     *
     * @param args (args[0]:word2vec path, args[1]:wordnet home path)
     * @throws RepositoryException
     * @throws IOException
     * @throws MalformedQueryException
     * @throws QueryEvaluationException
     * @throws FileNotFoundException
     * @throws ClassNotFoundException
     */
    public static void main(String[] args) throws RepositoryException, IOException, MalformedQueryException, QueryEvaluationException, FileNotFoundException, ClassNotFoundException {

        Logger.getLogger(OnFocusRRR.class.getName()).log(Level.INFO, "...loading KB...");
        KB = new QAInfoBase();

        StringUtils.generateStopListsFromExternalSource(filePath_en, filePath_gr);

        Logger.getLogger(OnFocusRRR.class.getName()).log(Level.INFO, "...loading word2vec...");
        File gModel = new File(args[0] + "GoogleNews-vectors-negative300.bin.gz");
        Word2Vec vec = WordVectorSerializer.readWord2VecModel(gModel);
        WordMovers wm = WordMovers.Builder().wordVectors(vec).build();

        Logger.getLogger(OnFocusRRR.class.getName()).log(Level.INFO, "...loading wordnet...");
        String wnhome = System.getenv(args[1]);
        String path = wnhome + File.separator + "dict";
        URL url = new URL("file", null, path);
        // construct the dictionary object and open it
        IDictionary dict = new Dictionary(url);
        dict.open();

        // Retrieve hyperparameters
        //ModelHyperparameters bestModel = (ModelHyperparameters) Utils.getSavedObject("AVEPbased_BestModel");
        //float word2vec_w = bestModel.getWord2vecWeight();
        //float wordNet_w = bestModel.getWordNetWeight();
        float word2vec_w = 0.4f;
        float wordNet_w = 0.6f;
        // Choose weights to be used in model IV
        HashMap<String, Float> model_weights = new HashMap<>();
        model_weights.put("wordnet", wordNet_w);
        model_weights.put("word2vec", word2vec_w);

        // Choose wordnet sources to be used
        ArrayList<String> wordnetResources = new ArrayList<>();
        wordnetResources.add("synonyms");
        wordnetResources.add("antonyms");
        wordnetResources.add("hypernyms");

        Logger.getLogger(OnFocusRRR.class.getName()).log(Level.INFO, "...initializing model...");
        // Instantiate model
        combination = new WordnetWord2vecModel("Word2vec and Wordnet", dict, wordnetResources, wm, vec, model_weights);

    }

    public JSONObject getTop2Comments(ArrayList<String> uris, String question) throws RepositoryException, MalformedQueryException, QueryEvaluationException, JSONException {
        //System.out.println(KB.getAllSubjectsOfTypeWithURIs("owl", "NamedIndividual", uris));
        // Get hotels on focus
        //HashSet<Subject> hotels = KB.getAllSubjectsOfTypeWithURIs("owl", "NamedIndividual", uris);
        // Get their comments
        ArrayList<Comment> comments = demo_main.getCommentsOnFocus(KB, uris);
        // Score them
        ArrayList<Comment> scoredComments = combination.scoreComments(question, comments);
        // Get top 2 comments and create JASON object
        JSONObject resultListAsJASON = getJASONObject(combination.getTopComments(2, scoredComments));

        return resultListAsJASON;
        //return null;
    }

    private static JSONObject getJASONObject(ArrayList<Comment> topComments) {

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
