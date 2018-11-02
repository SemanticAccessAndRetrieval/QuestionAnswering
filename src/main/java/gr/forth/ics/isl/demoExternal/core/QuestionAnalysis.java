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
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.trees.TypedDependency;
import edu.stanford.nlp.util.CoreMap;
import static gr.forth.ics.isl.demoExternal.main.ExternalKnowledgeDemoMain.compounds_pipeline;
import static gr.forth.ics.isl.demoExternal.main.ExternalKnowledgeDemoMain.entityMentions_pipeline;
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
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Lefteris Dimitrakis
 */
public class QuestionAnalysis {

    private String question;
    // Store the useful words of the question
    private Set<String> useful_words;
    // Store the text of the Named Entities
    private Set<String> question_entities;
    // Store the concatenation of useful_words to retrieve cand. triples from LODSyndesis
    private String fact = "";

    private String question_type = "";

    public QuestionAnalysis() {
    }

    public String getQuestion() {
        return question;
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

    public void setQuestion(String quest) {
        this.question = quest;
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

        this.question = extractCleanQuestion(question);
        Logger.getLogger(QuestionAnalysis.class.getName()).log(Level.INFO, "=====Clean Question: {0}", this.question);

        // Extract the Named Entities from the question with their type e.g. Location, Person etc.
        HashMap<String, String> word_NamedEntity = extractEntitiesWithType(this.question);
        Logger.getLogger(QuestionAnalysis.class.getName()).log(Level.INFO, "=====Named Entities: {0}", word_NamedEntity);

        // Store only the text of the Named Entities
        question_entities = word_NamedEntity.keySet();
        Logger.getLogger(QuestionAnalysis.class.getName()).log(Level.INFO, "===== Entities: {0}", question_entities);

        useful_words = extractUsefulWords(this.question, this.question_entities);

        Logger.getLogger(QuestionAnalysis.class.getName()).log(Level.INFO, "===== Useful_words: {0}", useful_words);

        question_type = identifyQuestionType(this.question.toLowerCase(), this.useful_words);

        Logger.getLogger(QuestionAnalysis.class.getName()).log(Level.INFO, "===== Question Type: {0}", question_type);

        // For definition question we search for certain tags e.g. comment, description, abstract
        // These tags should be included in the useful_words set
        if (question_type.equals("definition")) {
            useful_words = new HashSet<>(AnswerExtraction.definition_relations);
        }

        // Concat all word in useful_words to construct a fact
        for (String word : useful_words) {
            fact += word + " ";
        }
        fact = fact.trim();
    }

    public String extractCleanQuestion(String question) {
        // Remove any quotes
        question = question.replaceAll("\"", "");
        // Trim the final question text
        question = question.trim();

        return question;
    }

    public HashMap<String, String> extractEntitiesWithType(String question) {
        // Extract the Named Entities from the question with their type e.g. Location, Person etc.
        HashMap<String, String> word_NamedEntity = getEntityMentionsWithNer(question);

        HashMap<String, String> word_compounded = extractCompoundWords(question);

        System.out.println("========!!!!!!!!!======== Initial Entities: " + word_NamedEntity);

        // Replace entity with its compounded form, if it exists
        // e.g. entity = Hunaydi, compounded_entity = Hunyadi family
        HashMap<String, String> compound_word_NamedEntity = new HashMap<>();
        for (String word : word_NamedEntity.keySet()) {
            if (word_compounded.containsKey(word)) {
                compound_word_NamedEntity.put(word_compounded.get(word), word_NamedEntity.get(word));
            } else {
                compound_word_NamedEntity.put(word, word_NamedEntity.get(word));
            }
        }
        System.out.println("========!!!!!!!!!======== Compounded entities: " + compound_word_NamedEntity);

        // Remove the first word of multi-word entities if they start with a stop-word
        HashMap<String, String> clean_word_NamedEntity = new HashMap<>();
        for (String entity : compound_word_NamedEntity.keySet()) {
            String[] entity_words = entity.split(" ");

            if (entity_words.length > 1 && StringUtils.isStopWord(entity_words[0].toLowerCase())) {
                Logger.getLogger(QuestionAnalysis.class.getName()).log(Level.INFO, "===== ENTITY WITH STARTING WORD STOPWORD: {0}", entity);
                String tmp_entity = "";
                for (int i = 1; i < entity_words.length; i++) {
                    tmp_entity += entity_words[i] + " ";
                }
                clean_word_NamedEntity.put(tmp_entity.trim(), compound_word_NamedEntity.get(entity));
            } else {
                clean_word_NamedEntity.put(entity, compound_word_NamedEntity.get(entity));
            }
        }

        System.out.println("========!!!!!!!!!======== Final entities: " + clean_word_NamedEntity);
        return clean_word_NamedEntity;
    }

    public HashSet<String> extractUsefulWords(String question, Set<String> entities) {
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

        HashSet<String> entities_words = new HashSet<>();

        // Find all entity words and store them
        // We split each identified Named entity to catch also multi-word named entities e.g. Golden Pavilion
        for (String word : entities) {
            for (String w : word.split(" ")) {
                entities_words.add(w.replaceAll("[^a-zA-Z ]", "").toLowerCase().trim());
            }
        }

        // Remove all entity words
        final_useful_words.removeAll(entities_words);

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

    public static HashMap<String, String> getEntityMentionsWithNer(String text) {

        //apply
        Annotation document = new Annotation(text);

        entityMentions_pipeline.annotate(document);

        List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);

        HashMap<String, String> entityMention_ner = new HashMap<>();

        //For each sentence
        for (CoreMap sentence : sentences) {
            for (CoreMap entityMention : sentence.get(CoreAnnotations.MentionsAnnotation.class)) {
                entityMention_ner.put(entityMention.toString().trim(), entityMention.get(CoreAnnotations.EntityTypeAnnotation.class).trim());
            }
        }
        return entityMention_ner;
    }

    public static HashMap<String, String> extractCompoundWords(String text) {

        HashMap<String, String> word_pos = getTokensWithPos(text);

        //apply
        Annotation document = new Annotation(text);

        compounds_pipeline.annotate(document);

        List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);

        TreeMap<Integer, String> index_word;
        TreeMap<String, Integer> word_index;
        HashMap<String, TreeSet<Integer>> word_compounds_indices;
        //For each sentence
        for (CoreMap sentence : sentences) {
            SemanticGraph semanticGraph = sentence.get(SemanticGraphCoreAnnotations.BasicDependenciesAnnotation.class);

            index_word = new TreeMap<>();
            word_index = new TreeMap<>();
            // Iterate over all the typed dependencies to extract two Maps
            // 1) <word, index in sentence>, 2) <index in sentence,word>
            for (TypedDependency das : semanticGraph.typedDependencies()) {
                int dep_id = das.dep().index();
                if (dep_id != 0 && !index_word.containsKey(dep_id)) {
                    index_word.put(dep_id, das.dep().word().trim());
                    word_index.put(das.dep().word().trim(), dep_id);
                }

                int gov_id = das.gov().index();
                if (gov_id != 0 && !index_word.containsKey(gov_id)) {
                    index_word.put(gov_id, das.gov().word().trim());
                    word_index.put(das.gov().word().trim(), gov_id);
                }
            }

            word_compounds_indices = new HashMap<>();
            // Iterate over all the typed dependencies to find all compound relations e.g. compound(Hunaydi,family) => Hunaydi family
            // We construct a hashmap with keys all the words which appear as 1st argument in a compound relation
            // and as value a TreeSet of word indices, useful to construct the compounded string
            for (TypedDependency das : semanticGraph.typedDependencies()) {
                if (das.reln().getShortName().equalsIgnoreCase("compound")) {
                    // We extract the text of the 1st and 2nd argument of the compound
                    // as well as their indices in the sentence
                    String dep = das.dep().word().trim();
                    int dep_index = das.dep().index();
                    String gov = das.gov().word().trim();
                    int gov_index = das.gov().index();

                    // If the hashmap already contains this word, we update the corresponding treeset of indices
                    if (word_compounds_indices.containsKey(dep)) {
                        TreeSet<Integer> tmp_ind = word_compounds_indices.get(dep);
                        int start_index = dep_index + 1;
                        int end_index = gov_index;
                        // if the compound words are not consecutive words, we include also the indices of the words between them
                        for (int ind = start_index; ind <= end_index; ind++) {
                            tmp_ind.add(ind);
                        }
                        word_compounds_indices.replace(dep, tmp_ind);
                    } else {
                        TreeSet<Integer> tmp_ind = new TreeSet<>();
                        int start_index = dep_index + 1;
                        int end_index = gov_index;
                        for (int ind = start_index; ind <= end_index; ind++) {
                            tmp_ind.add(ind);
                        }
                        word_compounds_indices.put(dep, tmp_ind);
                    }
                }
            }

            HashMap<String, String> word_compounded = new HashMap<>();
            for (String word : word_compounds_indices.keySet()) {
                int head_word_index = word_index.get(word);
                int last_word_index = word_compounds_indices.get(word).last();
                String tmp_comp = index_word.get(head_word_index) + " ";

                for (int index : word_compounds_indices.get(word)) {
                    if (index == last_word_index) {
                        String tmp_word = index_word.get(index);
                        if (!word_pos.get(tmp_word.toLowerCase()).toLowerCase().startsWith("vb")) {
                            tmp_comp += index_word.get(index) + " ";
                        }
                    } else {
                        tmp_comp += index_word.get(index) + " ";
                    }
                    //System.out.println(index);
                }

                word_compounded.put(word, tmp_comp.trim());
            }

            return word_compounded;
        }
        return null;
    }

    public static HashMap<String, String> getTokensWithPos(String text) {
        Annotation document = new Annotation(text);
        split_pipeline.annotate(document);

        List<CoreLabel> tokens = document.get(CoreAnnotations.TokensAnnotation.class);

        HashMap<String, String> final_tokens = new HashMap<>();

        String tmp_token = "";
        for (CoreLabel tok : tokens) {

            tmp_token = tok.get(CoreAnnotations.TextAnnotation.class).toLowerCase().trim();
            if (!tmp_token.isEmpty()) {
                //Get the POS tag of the token
                String pos = tok.get(CoreAnnotations.PartOfSpeechAnnotation.class);
                final_tokens.put(tmp_token, pos);
            }

        }

        return final_tokens;

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
