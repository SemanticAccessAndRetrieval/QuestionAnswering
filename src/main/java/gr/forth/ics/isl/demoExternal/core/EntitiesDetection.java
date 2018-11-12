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

import static gr.forth.ics.isl.demoExternal.main.ExternalKnowledgeDemoMain.chanel;
import gr.forth.ics.isl.utilities.StringUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
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

    private HashMap<String, ArrayList<String>> entity_cand_URIs;
    private HashMap<String, String> entity_matchedURI;

    public HashMap<String, ArrayList<String>> getEntitiesWithCandidateURIs() {
        return this.entity_cand_URIs;
    }

    public HashMap<String, String> getEntitiesWithMatchedURIs() {
        return this.entity_matchedURI;
    }

    public void setEntitiesWithCandidateURIs(HashMap<String, ArrayList<String>> entities_URIs) {
        this.entity_cand_URIs = entities_URIs;
    }

    public void setEntitiesWithMatchedURIs(HashMap<String, String> entities_matched_URIs) {
        this.entity_matchedURI = entities_matched_URIs;
    }

    public HashMap<String, String> getMatchingURIs(Set<String> question_entities) {

        // Hashmap to store each entity and the selected URI (the highest scored)
        HashMap<String, String> entity_URI = new HashMap<>();

        // For each entity find the final matching URI
        for (String entity : question_entities) {
            entity_URI.put(entity, getTopScoredEntityURI(entity, entity_cand_URIs.get(entity)));
        }

        //Logger.getLogger(EntitiesDetection.class.getName()).log(Level.INFO, "===== Entity-matched URI: {0}", entity_URI);

        this.entity_matchedURI = entity_URI;

        return entity_URI;
    }

    public void retrieveCandidateEntityURIs(Set<String> question_entities) {
        // Hashmap to store entities along with their candidate URIs
        HashMap<String, ArrayList<String>> entity_candidateURIs = new HashMap<>();

        // For each entity retrieve from LODSyndesis the candidate URIs
        for (String entity : question_entities) {
            entity_candidateURIs.put(entity, chanel.getEntityFromKeyWord(entity));
        }

        //Logger.getLogger(EntitiesDetection.class.getName()).log(Level.INFO, "===== Entity-candidate URIs: {0}", entity_candidateURIs);

        this.entity_cand_URIs = entity_candidateURIs;
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

    public static HashMap<String, String> extractCombinedEntities(String question, HashMap<String, String> corenlp_entity_uri, HashMap<String, String> spotlight_entity_uri) {
        HashMap<String, String> corenlp_entities = replacePartialRecognizedEntities(corenlp_entity_uri, spotlight_entity_uri);
        HashMap<String, String> spotlight_entities = replacePartialRecognizedEntities(spotlight_entity_uri, corenlp_entities);

        HashMap<String, String> combined_entities = new HashMap<>(extractBestMatchingEntitiesURIs(question, corenlp_entities, spotlight_entities));

        HashMap<String, String> final_combined_entities = replaceOverlappingEntities(combined_entities);

        return final_combined_entities;
    }

    public static HashMap<String, String> replaceOverlappingEntities(HashMap<String, String> entities) {
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
