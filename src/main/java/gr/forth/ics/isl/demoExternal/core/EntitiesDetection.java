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
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Lefteris Dimitrakis
 */
public class EntitiesDetection {

    public static HashMap<String, String> retrieveMatchingURIs(Set<String> question_entities) {
        // Hashmap to store entities along with their candidate URIs
        HashMap<String, ArrayList<String>> entity_candidateURIs = retrieveCandidateEntityURIs(question_entities);

        // If we don't have a uri for an entity we cannot deliver any answer!
        for (ArrayList<String> cand_URIs : entity_candidateURIs.values()) {
            if (cand_URIs.isEmpty()) {
                return null;
            }
        }

        // Hashmap to store each entity and the selected URI (the highest scored)
        HashMap<String, String> entity_URI = new HashMap<>();

        // For each entity find the final matching URI
        for (String entity : question_entities) {
            entity_URI.put(entity, getTopScoredEntityURI(entity, entity_candidateURIs.get(entity)));
        }

        Logger.getLogger(EntitiesDetection.class.getName()).log(Level.INFO, "===== Entity-matched URI: {0}", entity_URI);

        return entity_URI;
    }

    public static HashMap<String, ArrayList<String>> retrieveCandidateEntityURIs(Set<String> question_entities) {
        // Hashmap to store entities along with their candidate URIs
        HashMap<String, ArrayList<String>> entity_candidateURIs = new HashMap<>();

        // For each entity retrieve from LODSyndesis the candidate URIs
        for (String entity : question_entities) {
            entity_candidateURIs.put(entity, chanel.getEntityFromKeyWord(entity));
        }

        Logger.getLogger(EntitiesDetection.class.getName()).log(Level.INFO, "===== Entity-candidate URIs: {0}", entity_candidateURIs);

        return entity_candidateURIs;
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

    public static String getSuffixOfURI(String uri) {
        String[] tmp = uri.split("\\/|#");
        String suffix = tmp[tmp.length - 1];
        return suffix;
    }

}
