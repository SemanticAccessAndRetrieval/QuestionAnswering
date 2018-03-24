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
import gr.forth.ics.isl.nlp.NlpAnalyzer;
import gr.forth.ics.isl.nlp.externalTools.WordNet;
import gr.forth.ics.isl.nlp.models.Comment;
import gr.forth.ics.isl.utilities.StringUtils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Lefteris Dimitrakis
 */
public class WordnetModel extends Model {

    private IDictionary dictionary;
    private ArrayList<String> resourcesToRetrieve;

    public WordnetModel(String description, IDictionary dict, ArrayList<String> resources, ArrayList<Comment> comments) {
        super.setDescription(description);
        super.setComments(comments);
        this.dictionary = dict;
        this.resourcesToRetrieve = resources;
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
        score = StringUtils.JaccardSim((String[]) commentSynset.toArray(new String[0]), (String[]) querySynset.toArray(new String[0]));

        return score;
    }

    public HashSet<String> getWordNetResources(String pos, IDictionary dict, String token, ArrayList<String> resources) throws IOException {

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

    // TODO: EXPAND THE CURRENT LIST
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
