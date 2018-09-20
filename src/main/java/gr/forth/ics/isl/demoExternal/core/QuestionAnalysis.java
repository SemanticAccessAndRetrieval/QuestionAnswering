/*
 *  This code belongs to the Semantic Access and Retrieval (SAR) group of the 
 *  Information Systems Laboratory (ISL) of the 
 *  Institute of Computer Science (ICS) of the  
 *  Foundation for Research and Technology - Hellas (FORTH)
 *  Nobody is allowed to use, copy, distribute, or modify this work.
 *  It is published for reasons of research results reproducibility.
 *  (c) 2017 Semantic Access and Retrieval group, All rights reserved
 */
package gr.forth.ics.isl.demoExternal.core;

import edu.mit.jwi.Dictionary;
import edu.mit.jwi.IDictionary;
import edu.mit.jwi.item.POS;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import gr.forth.ics.isl.demoExternal.LODsyndesis.LODSyndesisChanel;
import static gr.forth.ics.isl.demoExternal.main.ExternalKnowledgeDemoMain.chanel;
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

/**
 *
 * @author Lefteris Dimitrakis
 */
public class QuestionAnalysis {

    //The paths to the stopwords file
    public static String filePath_en = "/stoplists/stopwordsEn.txt";
    public static String filePath_gr = "/stoplists/stopwordsGr.txt";

    //Core Nlp pipeline instance
    public static StanfordCoreNLP pipeline;
    public static StanfordCoreNLP pipeline2;
    public static IDictionary dictionary;
    public static ArrayList<String> wordnetResources = new ArrayList<>();

    // Store the useful words of the question
    private Set<String> useful_words;
    // Store the text of the Named Entities
    private Set<String> question_entities;
    // Store the concatenation of useful_words to retrieve cand. triples from LODSyndesis
    private String fact = "";

    public QuestionAnalysis(String wordnetPath) {
        initialize(wordnetPath);
    }

    public static void initialize(String wordnetPath) {

        try {
            Logger.getLogger(QuestionAnalysis.class.getName()).log(Level.INFO, "...Generating stop-words lists...");
            StringUtils.generateStopListsFromExternalSource(filePath_en, filePath_gr);

            //Code to initialize also the Word2Vec model
            //Logger.getLogger(QuestionAnalysis.class.getName()).log(Level.INFO, "...loading word2vec...");
            //File gModel = new File(word2vecPath + "GoogleNews-vectors-negative300.bin.gz");
            //Word2Vec vec = WordVectorSerializer.readWord2VecModel(gModel);
            //WordMovers wm = WordMovers.Builder().wordVectors(vec).build();
            Logger.getLogger(QuestionAnalysis.class.getName()).log(Level.INFO, "...loading wordnet...");
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
            Logger.getLogger(QuestionAnalysis.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(QuestionAnalysis.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public Set<String> getUsefulWords() {
        return useful_words;
    }

    public Set<String> getQuestionEntities() {
        return question_entities;
    }

    public String getFact() {
        return fact;
    }

    public void setUsefulWords(Set<String> words) {
        this.useful_words = words;
    }

    public void setQuestionEntities(Set<String> entities) {
        this.question_entities = entities;
    }

    public void setFact(String fact) {
        this.fact = fact;
    }

    public void analyzeQuestion(String question) {

        // Get clean question words with PartOfSpeech tags
        HashMap<String, String> clean_query_with_POS = getCleanTokensWithPos(question);

        useful_words = clean_query_with_POS.keySet();

        // Extract the Named Entities from the question with their type e.g. Location, Person etc.
        HashMap<String, String> word_NamedEntity = getTokensWithNer(question);

        // Store only the text of the Named Entities
        question_entities = word_NamedEntity.keySet();

        Logger.getLogger(QuestionAnalysis.class.getName()).log(Level.INFO, "===== Entities: {0}", question_entities);

        useful_words.removeAll(question_entities);

        Logger.getLogger(QuestionAnalysis.class.getName()).log(Level.INFO, "===== Useful_words: {0}", useful_words);

        // Concat all word in useful_words to construct a fact
        for (String word : useful_words) {
            fact += word + " ";
        }
        fact.trim();
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

                if (!ner.equalsIgnoreCase("o")) {
                    word_ner.put(word, ner);
                }

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
                Logger.getLogger(QuestionAnalysis.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return new ArrayList<>(querySynset);
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
