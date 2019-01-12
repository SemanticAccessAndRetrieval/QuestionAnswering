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

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.trees.TypedDependency;
import edu.stanford.nlp.util.CoreMap;
import static gr.forth.ics.isl.demoExternal.core.QuestionAnalysis.getTokensWithPos;
import static gr.forth.ics.isl.demoExternal.main.ExternalKnowledgeDemoMain.chanel;
import static gr.forth.ics.isl.demoExternal.main.ExternalKnowledgeDemoMain.compounds_pipeline;
import static gr.forth.ics.isl.demoExternal.main.ExternalKnowledgeDemoMain.entityMentions_pipeline;
import static gr.forth.ics.isl.demoExternal.main.ExternalKnowledgeDemoMain.spotlight;
import gr.forth.ics.isl.nlp.externalTools.models.AnnotationUnit;
import gr.forth.ics.isl.nlp.externalTools.models.ResourceItem;
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
public class EntitiesDetection {

    // Store the text of the NE (recognized by SCNLP)
    private Set<String> corenlp_entities;
    // Store the text and the uri of the NE (recognized by DBPediaSpotlight)
    private HashMap<String, String> spotlight_entities_uris;
    private HashMap<String, ArrayList<String>> corenlp_entities_cand_URIs;
    private HashMap<String, String> corenlp_entities_uris;
    private HashMap<String, String> final_entities_uris;

    private ArrayList<String> dbpedia_blacklist = new ArrayList<>(Arrays.asList("time zone", "city", "area code"));

    public Set<String> getCorenlpEntities() {
        return corenlp_entities;
    }

    public HashMap<String, String> getSpotlightEntitiesUris() {
        return spotlight_entities_uris;
    }

    public HashMap<String, ArrayList<String>> getCorenlpEntitiesWithCandidateURIs() {
        return this.corenlp_entities_cand_URIs;
    }

    public HashMap<String, String> getCorenlpEntitiesWithURIs() {
        return this.corenlp_entities_uris;
    }

    public HashMap<String, String> getFinalEntitiesWithURIs() {
        return this.final_entities_uris;
    }

    public void setCorenlpEntities(Set<String> entities) {
        this.corenlp_entities = entities;
    }

    public void setSpotlightEntitiesUris(HashMap<String, String> entities_uris) {
        this.spotlight_entities_uris = entities_uris;
    }

    public void setCorenlpEntitiesWithCandidateURIs(HashMap<String, ArrayList<String>> entities_URIs) {
        this.corenlp_entities_cand_URIs = entities_URIs;
    }

    public void setCorenlpEntitiesWithURIs(HashMap<String, String> entities_matched_URIs) {
        this.corenlp_entities_uris = entities_matched_URIs;
    }

    public void setFinalEntitiesWithUris(HashMap<String, String> entities_final_URIs) {
        this.final_entities_uris = entities_final_URIs;
    }

    public void identifyNamedEntities(String question, String tool) {

        if (tool.equalsIgnoreCase("scnlp")) {
            // Extract the Named Entities from the question with their type e.g. Location, Person etc.
            HashMap<String, String> word_NamedEntity = extractCorenlpEntitiesWithType(question);
            this.corenlp_entities = word_NamedEntity.keySet();
            Logger.getLogger(EntitiesDetection.class.getName()).log(Level.INFO, "=====CoreNLP Named Entities: {0}", word_NamedEntity);

            this.spotlight_entities_uris = new HashMap<>();

        } else if (tool.equalsIgnoreCase("dbpedia")) {
            // Extract the Named Entities from the question with their corresponding dbpedia uri
            HashMap<String, String> cand_entities_uris = extractEntitiesWithSpotlight(question);
            this.spotlight_entities_uris = cand_entities_uris;
            Logger.getLogger(EntitiesDetection.class.getName()).log(Level.INFO, "=====Spotlight Named Entities: {0}", cand_entities_uris);

            this.corenlp_entities = new HashSet<>();
        } else {
            // Extract the Named Entities from the question with their type e.g. Location, Person etc.
            HashMap<String, String> word_NamedEntity = extractCorenlpEntitiesWithType(question);
            this.corenlp_entities = word_NamedEntity.keySet();
            Logger.getLogger(EntitiesDetection.class.getName()).log(Level.INFO, "=====CoreNLP Named Entities: {0}", word_NamedEntity);

            // Extract the Named Entities from the question with their corresponding dbpedia uri
            HashMap<String, String> cand_entities_uris = extractEntitiesWithSpotlight(question);
            this.spotlight_entities_uris = cand_entities_uris;
            Logger.getLogger(EntitiesDetection.class.getName()).log(Level.INFO, "=====Spotlight Named Entities: {0}", cand_entities_uris);
        }
    }

    public HashMap<String, String> extractCorenlpEntitiesWithType(String question) {
        // Extract the Named Entities from the question with their type e.g. Location, Person etc.
        HashMap<String, String> word_NamedEntity = getCorenlpEntityMentionsWithNer(question);

        HashMap<String, String> word_compounded = extractCorenlpCompoundWords(question);

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

        // Remove the first word of multi-word entities if they start with a stop-word
        HashMap<String, String> clean_word_NamedEntity = new HashMap<>();
        for (String entity : compound_word_NamedEntity.keySet()) {
            String[] entity_words = entity.split(" ");

            if (entity_words.length > 1 && StringUtils.isStopWord(entity_words[0].toLowerCase())) {
                String tmp_entity = "";
                for (int i = 1; i < entity_words.length; i++) {
                    tmp_entity += entity_words[i] + " ";
                }
                clean_word_NamedEntity.put(tmp_entity.trim(), compound_word_NamedEntity.get(entity));
            } else {
                clean_word_NamedEntity.put(entity, compound_word_NamedEntity.get(entity));
            }
        }

        return clean_word_NamedEntity;
    }

    public static HashMap<String, String> getCorenlpEntityMentionsWithNer(String text) {

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

    public static HashMap<String, String> extractCorenlpCompoundWords(String text) {

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

    public HashMap<String, String> extractEntitiesWithSpotlight(String question) {
        try {
            AnnotationUnit annotationUnit; // keeps the returned annotations

            annotationUnit = spotlight.get(question); // annotate
            HashMap<String, String> entity_uri = new HashMap<>();
            if (annotationUnit.getResources() != null && !annotationUnit.getResources().isEmpty()) {
                for (ResourceItem tmp_resource : annotationUnit.getResources()) {
                    if (!isInDBpediaBlackList(tmp_resource)) {
                        entity_uri.put(tmp_resource.getSurfaceForm().toLowerCase(), tmp_resource.getUri());
                    }
                }
            }
            return entity_uri;
        } catch (IOException ex) {
            Logger.getLogger(QuestionAnalysis.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new HashMap<>();
    }

    public boolean isInDBpediaBlackList(ResourceItem resource) {
        String tmp_label = resource.getSurfaceForm().toLowerCase();
        if (dbpedia_blacklist.contains(tmp_label)) {
            return true;
        }
        return false;
    }

    public HashMap<String, String> extractEntitiesWithUris(String question, String tool) {
        // if both sets are empty, then return as final an empty set
        if (this.corenlp_entities.isEmpty() && this.spotlight_entities_uris.isEmpty()) {
            this.final_entities_uris = new HashMap<>();
            return this.final_entities_uris;
        }

        if ((tool.equalsIgnoreCase("scnlp") && this.corenlp_entities.isEmpty()) || (tool.equalsIgnoreCase("dbpedia") && this.spotlight_entities_uris.isEmpty())) {
            this.final_entities_uris = new HashMap<>();
            return this.final_entities_uris;
        }

        if (tool.equalsIgnoreCase("scnlp") && !this.corenlp_entities.isEmpty()) {
            // Retrieve for each entity its candidate URIs from LODSyndesis
            retrieveCorenlpEntitiesCandidateURIs(this.corenlp_entities);

            ArrayList<String> cand_URIs;
            boolean error = false;
            // Check if there is a problem
            for (String entity : this.corenlp_entities_cand_URIs.keySet()) {
                cand_URIs = this.corenlp_entities_cand_URIs.get(entity);
                if (cand_URIs == null || cand_URIs.isEmpty()) {
                    error = true;
                    break;
                }
            }
            if (error) {
                this.final_entities_uris = new HashMap<>();
                return this.final_entities_uris;
            } else {
                this.corenlp_entities_uris = this.getMatchingCorenlpURIs(corenlp_entities);
                this.final_entities_uris = replaceOverlappingEntities(this.corenlp_entities_uris);
                return this.final_entities_uris;
            }
        } else if (tool.equalsIgnoreCase("dbpedia") && !this.spotlight_entities_uris.isEmpty()) {
            this.final_entities_uris = replaceOverlappingEntities(this.spotlight_entities_uris);
            return this.final_entities_uris;
        } else if (tool.equalsIgnoreCase("both")) {

            if (!this.corenlp_entities.isEmpty()) {
                // Retrieve for each entity its candidate URIs from LODSyndesis
                retrieveCorenlpEntitiesCandidateURIs(this.corenlp_entities);

                ArrayList<String> cand_URIs;
                boolean error = false;
                // Check if there is a problem
                for (String entity : this.corenlp_entities_cand_URIs.keySet()) {
                    cand_URIs = this.corenlp_entities_cand_URIs.get(entity);
                    if (cand_URIs == null || cand_URIs.isEmpty()) {
                        error = true;
                        break;
                    }
                }
                if (error) {
                    if (this.spotlight_entities_uris.isEmpty()) {
                        this.final_entities_uris = new HashMap<>();
                        return this.final_entities_uris;
                    } else {
                        this.final_entities_uris = replaceOverlappingEntities(this.spotlight_entities_uris);
                        return this.final_entities_uris;
                    }
                } else {
                    this.corenlp_entities_uris = this.getMatchingCorenlpURIs(corenlp_entities);

                    if (this.spotlight_entities_uris.isEmpty()) {
                        this.final_entities_uris = replaceOverlappingEntities(this.corenlp_entities_uris);
                        return this.final_entities_uris;
                    } else {
                        this.final_entities_uris = extractCombinedEntities(question, this.corenlp_entities_uris, this.spotlight_entities_uris);
                    }
                }
            } else {
                this.final_entities_uris = replaceOverlappingEntities(this.spotlight_entities_uris);
                return this.final_entities_uris;
            }
        }
        return this.final_entities_uris;
    }

    public HashMap<String, String> getMatchingCorenlpURIs(Set<String> question_entities) {

        // Hashmap to store each entity and the selected URI (the highest scored)
        HashMap<String, String> entity_URI = new HashMap<>();

        // For each entity find the final matching URI
        for (String entity : question_entities) {
            entity_URI.put(entity, getTopScoredEntityURI(entity, corenlp_entities_cand_URIs.get(entity)));
        }

        //Logger.getLogger(EntitiesDetection.class.getName()).log(Level.INFO, "===== Entity-matched URI: {0}", entity_URI);
        this.corenlp_entities_uris = entity_URI;

        return entity_URI;
    }

    public void retrieveCorenlpEntitiesCandidateURIs(Set<String> question_entities) {
        // Hashmap to store entities along with their candidate URIs
        HashMap<String, ArrayList<String>> entity_candidateURIs = new HashMap<>();

        // For each entity retrieve from LODSyndesis the candidate URIs
        for (String entity : question_entities) {
            entity_candidateURIs.put(entity, chanel.getEntityFromKeyWord(entity));
        }

        //Logger.getLogger(EntitiesDetection.class.getName()).log(Level.INFO, "===== Entity-candidate URIs: {0}", entity_candidateURIs);
        this.corenlp_entities_cand_URIs = entity_candidateURIs;
    }

    public static HashMap<String, ArrayList<String>> retrieveEquivalentEntityURIs(HashMap<String, String> entity_URI) {
        // Hashmap to store entities along with their equivalent URIs
        HashMap<String, ArrayList<String>> entity_equivalentURIs = new HashMap<>();

        // For each entity retrieve from LODSyndesis the candidate URIs
        for (String entity : entity_URI.keySet()) {
            entity_equivalentURIs.put(entity, chanel.getEquivalentEntity(entity_URI.get(entity)));
        }

        Logger.getLogger(EntitiesDetection.class.getName()).log(Level.INFO, "===== Entity-equivalent URIs: {0}", entity_equivalentURIs);

        return entity_equivalentURIs;
    }

    // Retrieve the best matching URI for the current entity
    public static String getTopScoredEntityURI(String entity, ArrayList<String> candidate_URIs) {
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

    public HashMap<String, String> extractCombinedEntities(String question, HashMap<String, String> corenlp_entity_uri, HashMap<String, String> spotlight_entity_uri) {
        HashMap<String, String> corenlp_entities = replacePartialRecognizedEntities(corenlp_entity_uri, spotlight_entity_uri);
        HashMap<String, String> spotlight_entities = replacePartialRecognizedEntities(spotlight_entity_uri, corenlp_entities);

        HashMap<String, String> combined_entities = new HashMap<>(extractBestMatchingEntitiesURIs(question, corenlp_entities, spotlight_entities));

        HashMap<String, String> final_combined_entities = replaceOverlappingEntities(combined_entities);

        return final_combined_entities;
    }

    public HashMap<String, String> replaceOverlappingEntities(HashMap<String, String> entities) {
        HashSet<String> entities_to_remove = new HashSet<>();

        for (String tmp_entity : entities.keySet()) {
            String tmp_entity_name = tmp_entity.toLowerCase();

            for (String entity : entities.keySet()) {
                String entity_name = entity.toLowerCase();
                String entity_uri = entities.get(entity).toLowerCase();

                if (!tmp_entity_name.equals(entity_name) && (entity_name.contains(tmp_entity_name) || entity_uri.contains(tmp_entity_name))) {
                    entities_to_remove.add(tmp_entity);
                }
            }
        }

        for (String entity : entities_to_remove) {
            entities.remove(entity);
        }

        return entities;
    }

    public static HashMap<String, String> replacePartialRecognizedEntities(HashMap<String, String> entity_uri, HashMap<String, String> entity_uri2) {

        HashMap<String, String> final_entities = new HashMap<>(entity_uri);

        for (String entity_name : entity_uri.keySet()) {
            String lower_entity_name = entity_name.toLowerCase();

            for (String entity_name2 : entity_uri2.keySet()) {
                String lower_entity_name2 = entity_name2.toLowerCase();

                if ((lower_entity_name2.startsWith(lower_entity_name) || lower_entity_name2.endsWith(lower_entity_name)) && !entity_name2.equalsIgnoreCase(entity_name)) {
                    final_entities.remove(entity_name);
                    final_entities.put(entity_name2, entity_uri2.get(entity_name2));
                    break;
                }

            }

        }
        return final_entities;
    }

    public static TreeMap<String, String> extractBestMatchingEntitiesURIs(String question, HashMap<String, String> corenlp_entity_uri, HashMap<String, String> spotlight_entity_uri) {

        TreeMap<String, String> insensitive_corenlp_entity_uri = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        insensitive_corenlp_entity_uri.putAll(corenlp_entity_uri);

        TreeMap<String, String> insensitive_spotlight_entity_uri = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        insensitive_spotlight_entity_uri.putAll(spotlight_entity_uri);

        TreeSet<String> common_entities = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        common_entities.addAll(insensitive_corenlp_entity_uri.keySet());
        common_entities.retainAll(insensitive_spotlight_entity_uri.keySet());

        TreeSet<String> rest_entities = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        rest_entities.addAll(insensitive_corenlp_entity_uri.keySet());
        rest_entities.addAll(insensitive_spotlight_entity_uri.keySet());
        rest_entities.removeAll(common_entities);

        TreeMap<String, String> final_entities_uri = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

        for (String common_entity : common_entities) {
            String corenlp_uri = insensitive_corenlp_entity_uri.get(common_entity);
            String spotlight_uri = insensitive_spotlight_entity_uri.get(common_entity);
            String best_uri = extractBestUriJaccard(question, corenlp_uri, spotlight_uri);
            final_entities_uri.put(common_entity, best_uri);
        }

        for (String rest_entity : rest_entities) {
            if (insensitive_corenlp_entity_uri.containsKey(rest_entity)) {
                final_entities_uri.put(rest_entity, insensitive_corenlp_entity_uri.get(rest_entity));
            } else {
                final_entities_uri.put(rest_entity, insensitive_spotlight_entity_uri.get(rest_entity));
            }
        }

        return final_entities_uri;
    }

    public static String extractBestUriJaccard(String question, String uri1, String uri2) {
        String uri1_suffix = EntitiesDetection.getSuffixOfURI(uri1.toLowerCase());
        uri1_suffix = uri1_suffix.replaceAll("[^\\dA-Za-z ]", " ");
        String uri2_suffix = EntitiesDetection.getSuffixOfURI(uri2.toLowerCase());
        uri2_suffix = uri2_suffix.replaceAll("[^\\dA-Za-z ]", " ");

        question = question.toLowerCase();
        question = question.replaceAll("[^\\dA-Za-z ]", " ");

        double max_score = Double.MIN_VALUE;
        String best_uri = "";

        double score1 = StringUtils.JaccardSim(uri1_suffix.split(" "), question.split(" "));
        double score2 = StringUtils.JaccardSim(uri2_suffix.split(" "), question.split(" "));

        if (score1 >= score2) {
            return uri1;
        } else {
            return uri2;
        }
    }

    public static String getSuffixOfURI(String uri) {
        String[] tmp = uri.split("\\/|#");
        String suffix = tmp[tmp.length - 1];
        return suffix;
    }

}
