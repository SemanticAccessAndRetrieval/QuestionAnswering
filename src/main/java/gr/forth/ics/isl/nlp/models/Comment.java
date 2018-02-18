/*
 *  This code belongs to the Semantic Access and Retrieval (SAR) group of the 
 *  Information Systems Laboratory (ISL) of the 
 *  Institute of Computer Science (ICS) of the  
 *  Foundation for Research and Technology - Hellas (FORTH)
 *  Nobody is allowed to use, copy, distribute, or modify this work.
 *  It is published for reasons of research results reproducibility.
 *  (c) 2017 Semantic Access and Retrieval group, All rights reserved
 */
package gr.forth.ics.isl.nlp.models;

import com.crtomirmajer.wmd4j.WordMovers;
import edu.mit.jwi.IDictionary;
import edu.mit.jwi.item.POS;
import gr.forth.ics.isl.nlp.NlpAnalyzer;
import gr.forth.ics.isl.nlp.externalTools.WordNet;
import gr.forth.ics.isl.utilities.StringUtils;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.deeplearning4j.models.word2vec.Word2Vec;

/**
 *
 * @author Lefteris Dimitrakis
 */
public class Comment {

    private String hotel_id;
    private String hotel_name;
    private String id;
    private String text;
    private String best_sentence;
    private Date date;
    private double score;
    private double word_score;
    private double synset_score;
    private static SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");

    public Comment(String hotel_name, String hotel_id, String id, String text, String date) {
        this.hotel_name = hotel_name;
        this.hotel_id = hotel_id;
        this.id = id;
        this.text = text;
        try {
            this.date = sdf.parse(date);
        } catch (ParseException ex) {
            Logger.getLogger(Comment.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.score = 0;
        this.word_score = 0;
        this.synset_score = 0;
        this.best_sentence = "";
    }

    public String getHotelName() {
        return this.hotel_name;
    }

    public String getHotelId() {
        return this.hotel_id;
    }

    public String getId() {
        return this.id;
    }

    public String getText() {
        return this.text;
    }

    public String getBestSentence() {
        return this.best_sentence;
    }

    public Date getDate() {
        return this.date;
    }

    public double getScore() {
        return this.score;
    }

    public void setHotelId(String hotel_id) {
        this.hotel_id = hotel_id;
    }

    public void setHotelName(String hotel_name) {
        this.hotel_name = hotel_name;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setDate(String date) {
        try {
            this.date = sdf.parse(date);
        } catch (ParseException ex) {
            Logger.getLogger(Comment.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void setScore(double score) {
        this.score = score;
    }

    public void setSynsetScore(double score) {
        this.synset_score = score;
    }

    public void setWordScore(double score) {
        this.word_score = score;
    }

    public double getWordScore() {
        return this.word_score;
    }

    //private void calculateWordDistance(WordMovers wm, String query, Word2Vec vec) {
    private double calculateWordDistance(WordMovers wm, String query, String text, Word2Vec vec) {
        double score = 0;

        ArrayList<String> querySet = NlpAnalyzer.getCleanTokens(query);
        String queryClean = "";
        for (String queryTerm : querySet) {
            if (vec.hasWord(queryTerm)) {
                queryClean += " " + queryTerm;
            }
        }

        ArrayList<String> commentSet = NlpAnalyzer.getCleanTokens(text);
        String commentClean = "";
        for (String commentTerm : commentSet) {
            if (vec.hasWord(commentTerm)) {
                commentClean += " " + commentTerm;
            }
        }

        score = wm.distance(commentClean, queryClean);

        //this.word_score = score;
        return score;
    }

    public void calculateWordScore(double max_dist) {
        double score = this.word_score;
        this.word_score = 1 - score / max_dist;
    }

    //private void calculateSynsetSimilarity(String query, IDictionary dict) throws IOException {
    private double calculateSynsetSimilarity(String query, String text, IDictionary dict) throws IOException {

        double score = 0.0;
        String crntTermPosTag;

        HashMap<String, String> queryMapWithPosTags = NlpAnalyzer.getCleanTokensWithPos(query);
        HashSet<String> querySynset = new HashSet<>();

        for (String queryTerm : queryMapWithPosTags.keySet()) {

            crntTermPosTag = queryMapWithPosTags.get(queryTerm);

            querySynset.addAll(getWordNetResources(crntTermPosTag, dict, queryTerm));
        }

        HashMap<String, String> commentMapWithPosTags = NlpAnalyzer.getCleanTokensWithPos(text);
        HashSet<String> commentSynset = new HashSet<>();

        for (String commentTerm : commentMapWithPosTags.keySet()) {
            crntTermPosTag = commentMapWithPosTags.get(commentTerm);
            commentSynset.addAll(getWordNetResources(crntTermPosTag, dict, commentTerm));
        }

        score = StringUtils.JaccardSim((String[]) commentSynset.toArray(new String[0]), (String[]) querySynset.toArray(new String[0]));

        //this.synset_score = score;
        return score;
    }

    public void calculateScores(WordMovers wm, String query, Word2Vec vec, IDictionary dict, float word2vec_w, float wordNet_w) throws IOException {
        double maxWordScore = Double.MIN_VALUE;
        double maxSynsetScore = Double.MIN_VALUE;
        double maxScore = Double.MIN_VALUE;
        String best_sentence = "";
        double tmpWordScore, tmpSynsetScore, tmpScore;
        for (String sentence : NlpAnalyzer.getSentences(this.text)) {
            tmpWordScore = calculateWordDistance(wm, query, sentence, vec);
            tmpSynsetScore = calculateSynsetSimilarity(query, sentence, dict);

            tmpScore = word2vec_w * tmpWordScore + wordNet_w * tmpSynsetScore;
            if (tmpScore >= maxScore) {
                maxWordScore = tmpWordScore;
                maxSynsetScore = tmpSynsetScore;
                maxScore = tmpScore;
                best_sentence = sentence;
            }
        }
        this.best_sentence = best_sentence;
        this.word_score = maxWordScore;
        this.synset_score = maxSynsetScore;
        //this.setScore((0.5 * calculateWordSimilarity(wm, query, vec)) + (0.5 * calculateSynsetSimilarity(query, dict)));
    }

    public void calculateScore(float word2vec_w, float wordNet_w) {
        this.score = word2vec_w * this.word_score + wordNet_w * this.synset_score;
        //this.setScore((0.5 * calculateWordSimilarity(wm, query, vec)) + (0.5 * calculateSynsetSimilarity(query, dict)));
    }

    @Override
    public String toString() {
        return "Comment for hotel: " + this.hotel_id + " with id: " + this.id + " says: " + this.text + " posted at: " + this.date + " with score: " + this.score;
    }

    public HashSet<String> getWordNetResources(String pos, IDictionary dict, String token) throws IOException {
        HashSet<String> synset = new HashSet<>();
        HashSet<String> crntSynset = new HashSet<>();
        if (pos.startsWith("J")) {
            crntSynset = WordNet.getSynonyms(dict, token, POS.ADJECTIVE);
            if (crntSynset != null) {
                synset.addAll(crntSynset);
            }
            crntSynset = WordNet.getAntonyms(dict, token, POS.ADJECTIVE);
            if (crntSynset != null) {
                synset.addAll(crntSynset);
            }
            crntSynset = WordNet.getHypernyms(dict, token, POS.ADJECTIVE);
            if (crntSynset != null) {
                synset.addAll(crntSynset);
            }

        } else if (pos.startsWith("R")) {
            crntSynset = WordNet.getSynonyms(dict, token, POS.ADVERB);
            if (crntSynset != null) {
                synset.addAll(crntSynset);
            }
            crntSynset = WordNet.getAntonyms(dict, token, POS.ADVERB);
            if (crntSynset != null) {
                synset.addAll(crntSynset);
            }
            crntSynset = WordNet.getHypernyms(dict, token, POS.ADVERB);
            if (crntSynset != null) {
                synset.addAll(crntSynset);
            }

        } else if (pos.startsWith("N")) {
            crntSynset = WordNet.getSynonyms(dict, token, POS.NOUN);
            if (crntSynset != null) {
                synset.addAll(crntSynset);
            }
            crntSynset = WordNet.getAntonyms(dict, token, POS.NOUN);
            if (crntSynset != null) {
                synset.addAll(crntSynset);
            }
            crntSynset = WordNet.getHypernyms(dict, token, POS.NOUN);
            if (crntSynset != null) {
                synset.addAll(crntSynset);
            }

        } else if (pos.startsWith("V")) {
            crntSynset = WordNet.getSynonyms(dict, token, POS.VERB);
            if (crntSynset != null) {
                synset.addAll(crntSynset);
            }
            crntSynset = WordNet.getAntonyms(dict, token, POS.VERB);
            if (crntSynset != null) {
                synset.addAll(crntSynset);
            }
            crntSynset = WordNet.getHypernyms(dict, token, POS.VERB);
            if (crntSynset != null) {
                synset.addAll(crntSynset);
            }

        }
        return synset;
    }
}
