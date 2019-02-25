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

import edu.mit.jwi.IDictionary;
import edu.mit.jwi.item.POS;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreAnnotations.LemmaAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import static gr.forth.ics.isl.demoExternal.main.ExternalKnowledgeDemoMain.lemma_pipeline;
import static gr.forth.ics.isl.demoExternal.main.ExternalKnowledgeDemoMain.split_pipeline;
import static gr.forth.ics.isl.demoExternal.main.ExternalKnowledgeDemoMain.wordnetResources;
import static gr.forth.ics.isl.demoExternal.main.ExternalKnowledgeDemoMain.wordnet_dict;
import gr.forth.ics.isl.nlp.externalTools.WordNet;
import gr.forth.ics.isl.utilities.StringUtils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Lefteris Dimitrakis
 */
public class QuestionAnalysis {

    private String question;
    // Store the question type (factoid, confirmation, definition)
    private String question_type = "";

    public QuestionAnalysis() {

    }

    public String getQuestion() {
        return question;
    }

    public String getQuestionType() {
        return question_type;
    }

    public void setQuestion(String quest) {
        this.question = quest;
    }

    public void setQuestionType(String q_type) {
        this.question_type = q_type;
    }

    public void analyzeQuestion(String question) {

        this.question = extractCleanQuestion(question);
        Logger.getLogger(QuestionAnalysis.class.getName()).log(Level.INFO, "=====Clean Question: {0}", this.question);

        Set<String> other_words = extractOtherWords(this.question);

        question_type = identifyQuestionType(this.question.toLowerCase(), other_words);

        Logger.getLogger(QuestionAnalysis.class.getName()).log(Level.INFO, "===== Question Type: {0}", question_type);
    }

    public String extractCleanQuestion(String question) {
        // Remove any quotes
        question = question.replaceAll("\"", "");
        // Trim the final question text
        question = question.trim();

        return question;
    }

    public static HashSet<String> extractOtherWords(String question) {
        // Get clean question words with PartOfSpeech tags
        HashMap<String, String> clean_query_with_POS = getCleanTokensWithPos(question);

        HashSet<String> final_useful_words = new HashSet<>(clean_query_with_POS.keySet());

        HashSet<String> stop_words = new HashSet<>();

        // Find all single char words and store them
        for (String word : final_useful_words) {
            if (word.length() == 1) {
                stop_words.add(word);
            }
        }

        // Remove all single char useful words
        final_useful_words.removeAll(stop_words);

        return final_useful_words;
    }

    public String identifyQuestionType(String question, Set<String> usef_words) {

        ArrayList<String> definition_words = new ArrayList<>(Arrays.asList("mean", "meaning", "definition"));
        ArrayList<String> definition_starting_words = new ArrayList<>(Arrays.asList("what is"));

        Set<String> question_words = getCleanTokensWithPos(question).keySet();

        for (String d_s_word : definition_starting_words) {
            if (question.startsWith(d_s_word) && usef_words.isEmpty()) {
                return "definition";
            }
        }

        for (String d_word : definition_words) {
            if (question_words.contains(d_word)) {
                return "definition";
            }
        }

        ArrayList<String> factoid_words = new ArrayList<>(Arrays.asList("when", "who", "where", "what", "which",
                "in which", "to which", "on which", "how many", "how much", "show me", "give me", "show", "how", "whom", "in what",
                "of what", "name a", "for which"));

        for (String f_word : factoid_words) {
            if (question.startsWith(f_word)) {
                return "factoid";
            }
        }

        ArrayList<String> confirmation_words = new ArrayList<>(Arrays.asList("are", "did", "is ", "was", "does", "were", "do "));

        for (String c_word : confirmation_words) {
            if (question.startsWith(c_word)) {
                return "confirmation";
            }
        }

        return "none";
    }

    public static HashMap<String, String> getCleanTokensWithPos(String text) {
        Annotation document = new Annotation(text);
        split_pipeline.annotate(document);

        List<CoreLabel> tokens = document.get(CoreAnnotations.TokensAnnotation.class);

        HashMap<String, String> final_tokens = new HashMap<>();

        String tmp_token = "";
        for (CoreLabel tok : tokens) {

            tmp_token = tok.get(CoreAnnotations.TextAnnotation.class).replaceAll("[^a-zA-Z ]", "").toLowerCase().trim();
            if (!tmp_token.isEmpty() && !StringUtils.isStopWord(tmp_token)) {
                //Get the POS tag of the token
                String pos = tok.get(CoreAnnotations.PartOfSpeechAnnotation.class);
                if (!pos.startsWith("-"))
                final_tokens.put(tmp_token, pos);

            }

        }

        return final_tokens;

    }

    public static HashSet<String> getNouns(String text) {
        Annotation document = new Annotation(text);
        split_pipeline.annotate(document);

        List<CoreLabel> tokens = document.get(CoreAnnotations.TokensAnnotation.class);

        HashSet<String> noun_tokens = new HashSet<>();

        String tmp_token = "";
        for (CoreLabel tok : tokens) {

            tmp_token = tok.get(CoreAnnotations.TextAnnotation.class).replaceAll("[^a-zA-Z ]", "").toLowerCase().trim();
            if (!tmp_token.isEmpty() && !StringUtils.isStopWord(tmp_token)) {
                //Get the POS tag of the token
                String pos = tok.get(CoreAnnotations.PartOfSpeechAnnotation.class);
                System.out.println(pos);
                if (!pos.startsWith("-") && pos.toLowerCase().startsWith("n")) {
                    noun_tokens.add(tmp_token);
                }

            }

        }

        return noun_tokens;

    }

    public static HashMap<String, String> getLemmatizedTokens(String text) {
        Annotation document = new Annotation(text);
        lemma_pipeline.annotate(document);

        HashMap<String, String> lemmas_pos = new HashMap<>();

        List<CoreLabel> tokens = document.get(CoreAnnotations.TokensAnnotation.class);

        String tmp_lemma = "";
        String tmp_pos = "";
        for (CoreLabel tok : tokens) {

            tmp_lemma = tok.get(LemmaAnnotation.class).toLowerCase().trim();
            tmp_pos = tok.get(CoreAnnotations.PartOfSpeechAnnotation.class);
            if (!tmp_lemma.isEmpty()) {
                lemmas_pos.put(tmp_lemma, tmp_pos);
            }

        }

        return lemmas_pos;

    }

    public static HashMap<String, String> getTokensWithPos(String text) {
        Annotation document = new Annotation(text);
        split_pipeline.annotate(document);

        List<CoreLabel> tokens = document.get(CoreAnnotations.TokensAnnotation.class);

        HashMap<String, String> final_tokens = new HashMap<>();

        String tmp_token = "";
        for (CoreLabel tok : tokens) {

            tmp_token = tok.get(CoreAnnotations.TextAnnotation.class).replaceAll("[^a-zA-Z ]", "").toLowerCase().trim();
            if (!tmp_token.isEmpty()) {
                //Get the POS tag of the token
                String pos = tok.get(CoreAnnotations.PartOfSpeechAnnotation.class);
                if (!pos.startsWith("-"))
                final_tokens.put(tmp_token, pos);
            }

        }

        return final_tokens;

    }

    public static HashMap<String, String> getCleanLemmatizedTokensWithPos(String text) {
        Annotation document = new Annotation(text);
        lemma_pipeline.annotate(document);

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
                querySynset.addAll(getWordNetResources(crntTermPosTag, wordnet_dict, queryTerm, wordnetResources));
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

    public synchronized HashMap<String, ArrayList<String>> getWordNetSynonyms(HashMap<String, String> word_pos) throws IOException {

        HashMap<String, ArrayList<String>> word_synset = new HashMap<>();

        for (String token : word_pos.keySet()) {
            String tmp_pos = word_pos.get(token);
            //Get the wordnet POS based on coreNLP POS
            POS tmp_word_pos = WordNet.getWordNetPos(tmp_pos);

            if (tmp_word_pos == null) {
                return new HashMap<>();
            }

            HashSet<String> crntSynset = new HashSet<>();

            crntSynset = WordNet.getSynonyms(wordnet_dict, token, tmp_word_pos);
            if (crntSynset != null) {
                ArrayList<String> tmp_synonyms = new ArrayList<>();
                tmp_synonyms.addAll(crntSynset);
                word_synset.put(token, tmp_synonyms);
            } else {
                word_synset.put(token, new ArrayList<String>());

            }

        }
        return word_synset;
    }
}
