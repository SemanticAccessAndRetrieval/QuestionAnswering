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
import edu.stanford.nlp.util.CoreMap;
import gr.forth.ics.isl.demoExternal.LODsyndesis.LODSyndesisChanel;
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
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
    public static StanfordCoreNLP pipeline2;
    public static IDictionary dictionary;
    public static ArrayList<String> wordnetResources = new ArrayList<>();
    public static LODSyndesisChanel chanel;

    public static void main(String[] args) {

        ExternalKnowledgeDemoMain lala = new ExternalKnowledgeDemoMain("WNHOME");

        String query = "What is the population of Kyoto?";

        // Get clean question words with PartOfSpeech tags
        HashMap<String, String> clean_query_with_POS = getCleanTokensWithPos(query);

        // Store the useful words of the question
        Set<String> useful_words = clean_query_with_POS.keySet();

        // Extract the Named Entities from the question with their type e.g. Location, Person etc.
        HashMap<String, String> word_NamedEntity = getTokensWithNer(query);

        // Store only the text of the Named Entities
        Set<String> entities = word_NamedEntity.keySet();

        Logger.getLogger(ExternalKnowledgeDemoMain.class.getName()).log(Level.INFO, "===== Entities: {0}", entities);

        useful_words.removeAll(entities);

        Logger.getLogger(ExternalKnowledgeDemoMain.class.getName()).log(Level.INFO, "===== Useful_words: {0}", useful_words);

        // Hashmap to store entities along with their candidate URIs
        HashMap<String, ArrayList<String>> entity_candidateURIs = new HashMap<>();
        // Hashmap to store each entity and the selected URI (the highest scored)
        HashMap<String, String> entity_URI = new HashMap<>();

        // For each entity retrieve from LODSyndesis the candidate URIs
        for (String entity : entities) {
            entity_candidateURIs.put(entity, chanel.getEntityFromKeyWord(entity));
        }

        Logger.getLogger(ExternalKnowledgeDemoMain.class.getName()).log(Level.INFO, "===== Entity-candidate URIs: {0}", entity_candidateURIs);

        // For each entity find the final matching URI
        for (String entity : entities) {
            entity_URI.put(entity, getMatchingEntityURI(entity, entity_candidateURIs.get(entity)));
        }

        Logger.getLogger(ExternalKnowledgeDemoMain.class.getName()).log(Level.INFO, "===== Entity-matched URI: {0}", entity_URI);

        // Concat all word in useful_words to construct a fact
        String fact = "";
        for (String s : useful_words) {
            fact += s + " ";
        }

        String tmp_cand_facts = "";
        ArrayList<JSONObject> cand_facts = new ArrayList<>();

        // For each entity
        for (String entity : entity_URI.keySet()) {
            // Retrieve all the candidate triples and concatanate the result to construct a string
            for (String str : chanel.checkFactAsJSON(entity_URI.get(entity), fact, 0.5).get(0)) {
                tmp_cand_facts += str + " ";
            }
            //Store all the candidate triples stored as JSONObjects extracted from the text
            cand_facts.addAll(extractJSONObjectsFromString(tmp_cand_facts));
        }

        ArrayList<String> cand_relations = new ArrayList<>();
        String tmp_pred;

        // Extract all the properties from the triples
        for (JSONObject ob : cand_facts) {
            try {
                //Retrieve from JSONObject the predicate of the triple
                tmp_pred = (String) ob.get("predicate");
                // Remove the surrounding symbols < ...URI... >
                cand_relations.add(tmp_pred.substring(1, tmp_pred.length() - 1));
            } catch (JSONException ex) {
                Logger.getLogger(ExternalKnowledgeDemoMain.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        Logger.getLogger(ExternalKnowledgeDemoMain.class.getName()).log(Level.INFO, "===== Cand. Relations: {0}", cand_relations);

        // Retrieve the matching property based on levenshtein distance
        String matched_property = getMatchingProperty(new ArrayList<>(useful_words), cand_relations);

        Logger.getLogger(ExternalKnowledgeDemoMain.class.getName()).log(Level.INFO, "===== Matched Relation: {0}", matched_property);

        // Retrieve the answer based on the matching relation/predicate
        String answer = getAnswer(matched_property, cand_facts);

        Logger.getLogger(ExternalKnowledgeDemoMain.class.getName()).log(Level.INFO, "===== Answer: {0}", answer);
    }

    public static String getAnswer(String matched_relation, ArrayList<JSONObject> cand_triples) {
        ArrayList<JSONObject> matched_triples = new ArrayList<>();

        for (JSONObject triple : cand_triples) {
            try {
                if (((String) triple.get("predicate")).equalsIgnoreCase("<" + matched_relation + ">")) {
                    matched_triples.add(triple);
                }
            } catch (JSONException ex) {
                Logger.getLogger(ExternalKnowledgeDemoMain.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        // TO UPDATE MORE SOPHISTICATED SELECTION OF THE ANSWER
        // CURRENTLY we return the object of the 1st matching triple
        for (JSONObject triple : matched_triples) {
            try {
                return (String) triple.get("object");
            } catch (JSONException ex) {
                Logger.getLogger(ExternalKnowledgeDemoMain.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return "Answer not found!";
    }

    public static String getMatchingProperty(ArrayList<String> useful_words, ArrayList<String> candidate_predicates) {
        HashMap<String, Integer> uri_distance = new HashMap<>();

        int cnt = 0;
        int tmp_distance;
        int min_distance = Integer.MAX_VALUE;
        int min_cnt = 0;

        //SHOULD UPDATE AND TAKE INTO ACCOUNT ALL THE USEFUL WORDS AND THEN CALCULATE A COMBINED SCORE
        String useful_word = useful_words.get(0);

        // I should first clean the URIs from the prefixes
        for (String cand_pred : candidate_predicates) {
            tmp_distance = StringUtils.LevenshteinDistance(useful_word, getSuffixOfURI(cand_pred));
            if (tmp_distance < min_distance) {
                min_distance = tmp_distance;
                min_cnt = cnt;
            }
            uri_distance.put(cand_pred, tmp_distance);
            cnt++;
        }

        return candidate_predicates.get(min_cnt);

    }

    public ExternalKnowledgeDemoMain(String wordnetPath) {
        initialize(wordnetPath);
    }

    // Retrieve the best matching URI for the current entity
    public static String getMatchingEntityURI(String entity, ArrayList<String> candidate_URIs) {
        HashMap<String, Integer> uri_distance = new HashMap<>();

        int cnt = 0;
        int tmp_distance;
        int min_distance = Integer.MAX_VALUE;
        int min_cnt = 0;

        for (String cand_uri : candidate_URIs) {
            tmp_distance = StringUtils.LevenshteinDistance(entity, getSuffixOfURI(cand_uri));
            if (tmp_distance < min_distance) {
                min_distance = tmp_distance;
                min_cnt = cnt;
            }
            uri_distance.put(cand_uri, tmp_distance);
            cnt++;
        }

        return candidate_URIs.get(min_cnt);

    }

    public static String getSuffixOfURI(String uri) {
        String[] tmp = uri.split("\\/|#");
        String suffix = tmp[tmp.length - 1];
        return suffix;
    }

    public static void initialize(String wordnetPath) {

        try {
            Logger.getLogger(ExternalKnowledgeDemoMain.class.getName()).log(Level.INFO, "...Generating stop-words lists...");
            StringUtils.generateStopListsFromExternalSource(filePath_en, filePath_gr);

            //Code to initialize also the Word2Vec model
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
            //wordnetResources.add("antonyms");
            //wordnetResources.add("hypernyms");

            Properties props = new Properties();
            //Properties including lemmatization
            //props.put("annotators", "tokenize, ssplit, pos, lemma");
            //Properties without lemmatization
            props.put("annotators", "tokenize, ssplit, pos");
            props.put("tokenize.language", "en");
            pipeline = new StanfordCoreNLP(props);

            Properties props2 = new Properties();
            props2.put("annotators", "tokenize, ssplit, pos, lemma,  ner");
            props2.put("tokenize.language", "en");
            pipeline2 = new StanfordCoreNLP(props2);

            chanel = new LODSyndesisChanel();

        } catch (MalformedURLException ex) {
            Logger.getLogger(ExternalKnowledgeDemoMain.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ExternalKnowledgeDemoMain.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public JSONObject getAnalyzedQueryAsJson(String query) {
        try {
            JSONObject obj = new JSONObject();

            HashMap<String, String> clean_query = getCleanTokensWithPos(query);

            obj.put("clean_query", clean_query.keySet());

            ArrayList<String> expanded_query = getCleanExpandedQuery(query);

            obj.put("expanded_query", expanded_query);

            HashMap<String, String> word_NamedEntity = getTokensWithNer(query);

            obj.put("recognized_NamedEntities", word_NamedEntity);

            return obj;
        } catch (JSONException ex) {
            Logger.getLogger(ExternalKnowledgeDemoMain.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }

    public ArrayList<String> getCleanExpandedQuery(String query) {
        String crntTermPosTag;

        //Construct query's wordnet representation
        HashMap<String, String> queryMapWithPosTags = getCleanTokensWithPos(query);
        HashSet<String> querySynset = new HashSet<>();

        //querySynset.addAll(queryMapWithPosTags.keySet());
        for (String queryTerm : queryMapWithPosTags.keySet()) {
            try {
                crntTermPosTag = queryMapWithPosTags.get(queryTerm);
                //remove the initial words, and keep only the synonyms?????
                querySynset.addAll(getWordNetResources(crntTermPosTag, dictionary, queryTerm, wordnetResources));
            } catch (IOException ex) {
                Logger.getLogger(ExternalKnowledgeDemoMain.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return new ArrayList<>(querySynset);
    }

    public static HashMap<String, String> getTokensWithNer(String text) {

        //apply
        Annotation document = new Annotation(text);
        pipeline2.annotate(document);

        List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);

        HashMap<String, String> word_ner = new HashMap<>();

        //For each sentence
        for (CoreMap sentence : sentences) {
            //For each word in the sentence
            for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {

                //Get the TEXT of the token
                String word = token.get(CoreAnnotations.TextAnnotation.class).toLowerCase().trim();

                //Get the NER tag of the token
                String ner = token.get(CoreAnnotations.NamedEntityTagAnnotation.class);

                if (!ner.equalsIgnoreCase("o"))
                word_ner.put(word, ner);

            }
        }
        return word_ner;
    }

    public static HashMap<String, String> getCleanLemmatizedTokensWithPos(String text) {
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

    public static HashMap<String, String> getCleanTokensWithPos(String text) {
        Annotation document = new Annotation(text);
        pipeline.annotate(document);

        List<CoreLabel> tokens = document.get(CoreAnnotations.TokensAnnotation.class);

        HashMap<String, String> final_tokens = new HashMap<>();

        String tmp_token = "";
        for (CoreLabel tok : tokens) {

            tmp_token = tok.get(CoreAnnotations.TextAnnotation.class).replaceAll("[^a-zA-Z ]", "").toLowerCase().trim();
            if (!tmp_token.isEmpty() && !StringUtils.isStopWord(tmp_token)) {
                //Get the POS tag of the token
                String pos = tok.get(CoreAnnotations.PartOfSpeechAnnotation.class);
                final_tokens.put(tmp_token, pos);

            }

        }

        return final_tokens;

    }

    public static ArrayList<JSONObject> extractJSONObjectsFromString(String cand_facts) {

        List<String> matches = new ArrayList<String>();

        Pattern ptn = Pattern.compile("\\{.*?\\}");

        Matcher mtch = ptn.matcher(cand_facts);

        while (mtch.find()) {
            matches.add(mtch.group());
        }

        ArrayList<JSONObject> cand_triples = new ArrayList<>();

        for (String object_str : matches) {
            try {
                cand_triples.add(new JSONObject(object_str));
            } catch (JSONException ex) {
                Logger.getLogger(ExternalKnowledgeDemoMain.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return cand_triples;
    }


    public synchronized HashSet<String> getWordNetResources(String pos, IDictionary dict, String token, ArrayList<String> resources) throws IOException {

        //Get the wordnet POS based on coreNLP POS
        POS word_pos = WordNet.getWordNetPos(pos);

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

}
