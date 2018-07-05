/*
 *  This code belongs to the Semantic Access and Retrieval (SAR) group of the 
 *  Information Systems Laboratory (ISL) of the 
 *  Institute of Computer Science (ICS) of the  
 *  Foundation for Research and Technology - Hellas (FORTH)
 *  Nobody is allowed to use, copy, distribute, or modify this work.
 *  It is published for reasons of research results reproducibility.
 *  (c) 2017 Semantic Access and Retrieval group, All rights reserved
 */
package gr.forth.ics.isl.demoExternal.main;

import edu.mit.jwi.Dictionary;
import edu.mit.jwi.IDictionary;
import edu.mit.jwi.item.POS;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import gr.forth.ics.isl.nlp.externalTools.WordNet;
import gr.forth.ics.isl.utilities.StringUtils;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 *
 * @author Lefteris Dimitrakis
 */
public class ExternalKnowledgeDemoMain {

    //The paths to the stopwords file
    public static String filePath_en = "/stoplists/stopwordsEn.txt";
    public static String filePath_gr = "/stoplists/stopwordsGr.txt";

    //Core Nlp pipeline instance
    public static StanfordCoreNLP pipeline;
    public static IDictionary dictionary;
    public static ArrayList<String> wordnetResources = new ArrayList<>();

    public static void main(String[] args) {

        ExternalKnowledgeDemoMain lala = new ExternalKnowledgeDemoMain("WNHOME");
        System.out.println(lala.getCleanExpandedQuery("population Kyoto"));
    }

    public ExternalKnowledgeDemoMain(String wordnetPath) {
        initialize(wordnetPath);
    }

    public static void initialize(String wordnetPath) {

        try {
            Logger.getLogger(ExternalKnowledgeDemoMain.class.getName()).log(Level.INFO, "...Generating stop-words lists...");
            StringUtils.generateStopListsFromExternalSource(filePath_en, filePath_gr);

            //Logger.getLogger(ExternalKnowledgeDemoMain.class.getName()).log(Level.INFO, "...loading word2vec...");
            //File gModel = new File(word2vecPath + "GoogleNews-vectors-negative300.bin.gz");
            //Word2Vec vec = WordVectorSerializer.readWord2VecModel(gModel);
            //WordMovers wm = WordMovers.Builder().wordVectors(vec).build();

            Logger.getLogger(ExternalKnowledgeDemoMain.class.getName()).log(Level.INFO, "...loading wordnet...");
            String wnhome = System.getenv(wordnetPath);
            String path = wnhome + File.separator + "dict";
            URL url = new URL("file", null, path);
            // construct the dictionary object and open it
            dictionary = new Dictionary(url);
            dictionary.open();

            // Choose wordnet sources to be used
            wordnetResources.add("synonyms");

           // ArrayList<String> wordnetResources = new ArrayList<>();
            //wordnetResources.add("synonyms");
            //wordnetResources.add("antonyms");
            //wordnetResources.add("hypernyms");

            Properties props = new Properties();
            props.put("annotators", "tokenize, ssplit, pos, lemma");
            props.put("tokenize.language", "en");
            pipeline = new StanfordCoreNLP(props);
        } catch (MalformedURLException ex) {
            Logger.getLogger(ExternalKnowledgeDemoMain.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ExternalKnowledgeDemoMain.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public JSONObject getCleanExpandedQuery(String query) {
        String crntTermPosTag;

        //Construct query's wordnet representation
        HashMap<String, String> queryMapWithPosTags = getCleanTokensWithPos(query);
        HashSet<String> querySynset = new HashSet<>();

        //querySynset.addAll(queryMapWithPosTags.keySet());
        for (String queryTerm : queryMapWithPosTags.keySet()) {
            try {
                crntTermPosTag = queryMapWithPosTags.get(queryTerm);
                querySynset.addAll(getWordNetResources(crntTermPosTag, dictionary, queryTerm, wordnetResources));
            } catch (IOException ex) {
                Logger.getLogger(ExternalKnowledgeDemoMain.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return getJASONObject(new ArrayList<>(querySynset));
    }

    private static JSONObject getJASONObject(ArrayList<String> expanded_query) {

        try {
            JSONObject obj = new JSONObject();
            obj.put("expanded_query", expanded_query);

            return obj;
        } catch (JSONException ex) {
            Logger.getLogger(ExternalKnowledgeDemoMain.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }

    public static HashMap<String, String> getCleanTokensWithPos(String text) {
        Annotation document = new Annotation(text);
        pipeline.annotate(document);

        List<CoreLabel> tokens = document.get(CoreAnnotations.TokensAnnotation.class);

        HashMap<String, String> final_tokens = new HashMap<>();

        String tmp_token = "";
        for (CoreLabel tok : tokens) {

            tmp_token = tok.get(CoreAnnotations.LemmaAnnotation.class).replaceAll("[^a-zA-Z ]", "").toLowerCase().trim();
            if (!tmp_token.isEmpty() && !StringUtils.isStopWord(tmp_token)) {
                //Get the POS tag of the token
                String pos = tok.get(CoreAnnotations.PartOfSpeechAnnotation.class);
                final_tokens.put(tmp_token, pos);

            }

        }

        return final_tokens;

    }


    public synchronized HashSet<String> getWordNetResources(String pos, IDictionary dict, String token, ArrayList<String> resources) throws IOException {

        //Get the wordnet POS based on coreNLP POS
        POS word_pos = getWordNetPos(pos);

        if (word_pos == null) {
            return new HashSet<>();
        }

        HashSet<String> synset = new HashSet<>();
        HashSet<String> crntSynset;

        if (resources.contains("synonyms")) {
            crntSynset = WordNet.getSynonyms(dict, token, word_pos);
            if (crntSynset != null) {
                synset.addAll(crntSynset);
            }
        }

        if (resources.contains("antonyms")) {
            crntSynset = WordNet.getAntonyms(dict, token, word_pos);
            if (crntSynset != null) {
                synset.addAll(crntSynset);
            }
        }
        if (resources.contains("hypernyms")) {
            crntSynset = WordNet.getHypernyms(dict, token, word_pos);
            if (crntSynset != null) {
                synset.addAll(crntSynset);
            }
        }
        return synset;
    }

    //Get the wordnet POS based on coreNLP POS
    public POS getWordNetPos(String pos) {
        if (pos.startsWith("J")) {
            return POS.ADJECTIVE;
        } else if (pos.startsWith("R")) {
            return POS.ADVERB;
        } else if (pos.startsWith("N")) {
            return POS.NOUN;
        } else if (pos.startsWith("V")) {
            return POS.VERB;
        }
        return null;
    }

}
