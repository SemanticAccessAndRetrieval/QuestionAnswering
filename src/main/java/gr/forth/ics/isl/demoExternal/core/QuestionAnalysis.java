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
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.util.CoreMap;
import static gr.forth.ics.isl.demoExternal.main.ExternalKnowledgeDemoMain.ner_pipeline;
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

    // Store the useful words of the question
    private Set<String> useful_words;
    // Store the text of the Named Entities
    private Set<String> question_entities;
    // Store the concatenation of useful_words to retrieve cand. triples from LODSyndesis
    private String fact = "";

    private String question_type = "";

    public QuestionAnalysis() {
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

    public String getQuestionType() {
        return question_type;
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

    public void setQuestionType(String q_type) {
        this.question_type = q_type;
    }

    public void analyzeQuestion(String question) {

        question_type = identifyQuestionType(question.toLowerCase());

        // Get clean question words with PartOfSpeech tags
        HashMap<String, String> clean_query_with_POS = getCleanTokensWithPos(question);

        useful_words = clean_query_with_POS.keySet();

        // for definition question we can search for certain tags e.g. comment, label etc.
        // These tags should be included in the useful_words set
        if (question_type.equals("definition")) {
            Set<String> tmp_words = new HashSet<>(useful_words);
            tmp_words.add("comment");
            useful_words = tmp_words;
        }

        // Extract the Named Entities from the question with their type e.g. Location, Person etc.
        HashMap<String, String> word_NamedEntity = getTokensWithMultiNer(question);

        // Store only the text of the Named Entities
        question_entities = word_NamedEntity.keySet();

        Logger.getLogger(QuestionAnalysis.class.getName()).log(Level.INFO, "===== Entities: {0}", question_entities);

        // We split each identified Named entity to catch also multi-word named entities e.g. Golden Pavilion
        for (String word : question_entities) {
            for (String w : word.split(" ")) {
                useful_words.remove(w.trim());
            }

        }

        Logger.getLogger(QuestionAnalysis.class.getName()).log(Level.INFO, "===== Useful_words: {0}", useful_words);

        // Concat all word in useful_words to construct a fact
        for (String word : useful_words) {
            fact += word + " ";
        }
        fact.trim();
    }

    public String identifyQuestionType(String question) {

        ArrayList<String> definition_words = new ArrayList<>(Arrays.asList("mean", "meaning", "definition"));

        Set<String> question_words = getCleanTokensWithPos(question).keySet();

        for (String d_word : definition_words) {
            if (question_words.contains(d_word)) {
                return "definition";
            }
        }

        ArrayList<String> factoid_words = new ArrayList<>(Arrays.asList("when", "who", "where", "what", "which"));

        for (String f_word : factoid_words) {
            if (question.startsWith(f_word)) {
                return "factoid";
            }
        }

        ArrayList<String> confirmation_words = new ArrayList<>(Arrays.asList("are", "did", "is", "was", "does", "were", "do"));

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
                final_tokens.put(tmp_token, pos);

            }

        }

        return final_tokens;

    }


    // Version of Named Entity recognition able to detect multi-word entities e.g. Mount Everest
    public static HashMap<String, String> getTokensWithMultiNer(String text) {

        //apply
        Annotation document = new Annotation(text);
        ner_pipeline.annotate(document);

        List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);

        HashMap<String, String> word_ner = new HashMap<>();

        String previous_token = "";
        String previous_ner = "o";

        //For each sentence
        for (CoreMap sentence : sentences) {
            //For each word in the sentence
            for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {

                //Get the TEXT of the token
                String word = token.get(CoreAnnotations.TextAnnotation.class).toLowerCase().trim();

                //Get the NER tag of the token
                String ner = token.get(CoreAnnotations.NamedEntityTagAnnotation.class);

                if (!ner.equalsIgnoreCase(previous_ner) && !ner.equalsIgnoreCase("o")) {
                    if (!previous_ner.equalsIgnoreCase("o")) {
                        word_ner.put(previous_token.trim(), previous_ner);
                    }
                    previous_ner = ner;
                    previous_token = word;
                } else if (!ner.equalsIgnoreCase(previous_ner) && ner.equalsIgnoreCase("o")) {
                    word_ner.put(previous_token.trim(), previous_ner);
                    previous_ner = ner;
                    previous_token = word;
                } else if (ner.equalsIgnoreCase(previous_ner) && !ner.equalsIgnoreCase("o")) {
                    previous_token += " " + word;
                }

            }
        }
        return word_ner;
    }
    public static HashMap<String, String> getCleanLemmatizedTokensWithPos(String text) {
        Annotation document = new Annotation(text);
        split_pipeline.annotate(document);

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

}
