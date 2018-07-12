/*
 *  This code belongs to the Semantic Access and Retrieval (SAR) group of the
 *  Information Systems Laboratory (ISL) of the
 *  Institute of Computer Science (ICS) of the
 *  Foundation for Research and Technology - Hellas (FORTH)
 *  Nobody is allowed to use, copy, distribute, or modify this work.
 *  It is published for reasons of research results reproducibility.
 *  (c) 2017 Semantic Access and Retrieval group, All rights reserved
 */
package gr.forth.ics.isl.demo.models;

import edu.mit.jwi.IDictionary;
import edu.mit.jwi.item.POS;
import edu.stanford.nlp.util.Sets;
import gr.forth.ics.isl.nlp.NlpAnalyzer;
import gr.forth.ics.isl.nlp.externalTools.WordNet;
import gr.forth.ics.isl.nlp.models.Comment;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Sgo
 */


public class WordnetModel_II extends Model {

    public static HashMap<String, Double> word_score = new HashMap<>();
    public static HashMap<String, Integer> word_index = new HashMap<>();
    private IDictionary dictionary;
    private ArrayList<String> resourcesToRetrieve;

    public WordnetModel_II(String description, IDictionary dict, ArrayList<String> resources, ArrayList<Comment> comments) throws IOException {
        super.setDescription(description);
        super.setComments(comments);
        this.dictionary = dict;
        this.resourcesToRetrieve = resources;
        constructVocabIndex();
    }

    public IDictionary getDictionary() {
        return this.dictionary;
    }

    public void setDictionary(IDictionary dict) {
        this.dictionary = dict;
    }

    public ArrayList<String> getResourcesToRetrieve() {
        return this.resourcesToRetrieve;
    }

    public void setResourcesToRetrieve(ArrayList<String> resources) {
        this.resourcesToRetrieve = resources;
    }

    @Override
    public void scoreComments(String query) {
        for (Comment com : this.getComments()) {
            this.scoreComment(com, query);
        }
    }

    @Override
    public void scoreComment(Comment com, String query) {
        double maxScore = Double.MIN_VALUE;
        double tmpScore;
        String best_sentence = "";

        for (String sentence : NlpAnalyzer.getSentences(com.getText())) {
            try {
                tmpScore = calculateSynsetSimilarity(query, sentence, this.dictionary);

                if (tmpScore >= maxScore) {
                    maxScore = tmpScore;
                    best_sentence = sentence;
                }
            } catch (IOException ex) {
                Logger.getLogger(WordnetModel.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        com.setScore(maxScore);
        com.setBestSentence(best_sentence);
    }

    private double calculateSynsetSimilarity(String query, String text, IDictionary dict) throws IOException {

        double score;
        String crntTermPosTag;

        //Construct query's wordnet representation
        HashMap<String, String> queryMapWithPosTags = NlpAnalyzer.getCleanTokensWithPos(query);
        HashSet<String> querySynset = new HashSet<>();

        for (String queryTerm : queryMapWithPosTags.keySet()) {
            crntTermPosTag = queryMapWithPosTags.get(queryTerm);
            querySynset.addAll(getWordNetResources(crntTermPosTag, dict, queryTerm, this.resourcesToRetrieve));
        }

        //Construct comment's wordnet representation
        HashMap<String, String> commentMapWithPosTags = NlpAnalyzer.getCleanTokensWithPos(text);
        HashSet<String> commentSynset = new HashSet<>();

        for (String commentTerm : commentMapWithPosTags.keySet()) {
            crntTermPosTag = commentMapWithPosTags.get(commentTerm);
            commentSynset.addAll(getWordNetResources(crntTermPosTag, dict, commentTerm, this.resourcesToRetrieve));
        }

        //Calculate Jaccard Similarity
        score = calculateWeightedJaccard(querySynset, commentSynset);

        return score;
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

    public static void constructVocabIndex() throws FileNotFoundException, IOException {
        BufferedReader br = new BufferedReader(new FileReader("vocab.txt"));
        String line = "";

        while ((line = br.readLine()) != null) {

            // use comma as separator
            String[] tuple = line.split(";;");
            word_score.put(tuple[1], Double.parseDouble(tuple[2]));
            word_index.put(tuple[1], Integer.parseInt(tuple[0]));

        }
    }

    public static double calculateWeightedJaccard(HashSet<String> query, HashSet<String> comment) {

        // Define the vectors as arrays
        double[] query_vector = constructVectorRepresentation(query, word_score.keySet().size());
        double[] comment_vector = constructVectorRepresentation(comment, word_score.keySet().size());

        // Calculate the union of the two sets
        Set<String> union = Sets.union(query, comment);
        // Calculate the intersection of the two sets
        Set<String> intersection = Sets.intersection(query, comment);

        double numerator = 0.0d;
        double denomenator = 0.0d;

        for (String word : intersection) {
            if (word_score.containsKey(word)) {
                numerator += Math.min(query_vector[word_index.get(word)], comment_vector[word_index.get(word)]);
            } else {
                //System.out.println("Word: " + word + " missing from vocabulary!");
            }
        }

        for (String word : union) {
            if (word_score.containsKey(word)) {
                denomenator += Math.max(query_vector[word_index.get(word)], comment_vector[word_index.get(word)]);
            } else {
                //System.out.println("Word: " + word + " missing from vocabulary!");
            }
        }

        if (denomenator != 0.0d) {
            return numerator / denomenator;
        }

        return 0.0d;
    }

    public static double[] constructVectorRepresentation(HashSet<String> words, int N) {
        // Define the vectors as arrays
        double[] vector = new double[N];

        for (String word : words) {
            if (word_score.containsKey(word)) {
                vector[word_index.get(word)] = word_score.get(word);
            } else {
                //System.out.println("Word: " + word + " missing from vocabulary!");
            }
        }
        return vector;
    }
}
