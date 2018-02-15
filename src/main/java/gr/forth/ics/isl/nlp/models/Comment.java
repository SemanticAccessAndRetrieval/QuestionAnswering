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
    private String id;
    private String text;
    private Date date;
    private double score;
    private static SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");

    public Comment(String hotel_id, String id, String text, String date) {
        this.hotel_id = hotel_id;
        this.id = id;
        this.text = text;
        try {
            this.date = sdf.parse(date);
        } catch (ParseException ex) {
            Logger.getLogger(Comment.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.score = 0;
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

    public Date getDate() {
        return this.date;
    }

    public double getScore() {
        return this.score;
    }

    public void setHotelId(String hotel_id) {
        this.hotel_id = hotel_id;
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

    public double calculateWordSimilarity(WordMovers wm, String query, Word2Vec vec) {
        double score = 0;

        ArrayList<String> querySet = NlpAnalyzer.getCleanTokens(query);
        String queryClean = "";
        for (String queryTerm : querySet) {
            if (vec.hasWord(queryTerm)) {
                queryClean += " " + queryTerm;
            }
        }

        ArrayList<String> commentSet = NlpAnalyzer.getCleanTokens(this.text);
        String commentClean = "";
        for (String commentTerm : commentSet) {
            if (vec.hasWord(commentTerm)) {
                commentClean += " " + commentTerm;
            }
        }

        score = 1 - wm.distance(commentClean, queryClean);

        return score;
    }

    public double calculateSynsetSimilarity(String query, IDictionary dict) throws IOException {

        //dict.open();

        double score = 0.0;

        HashSet<String> crntSynset = new HashSet<>();
        HashMap<String, String> queryMapWithPosTags = NlpAnalyzer.getCleanTokensWithPos(query);
        HashSet<String> querySynset = new HashSet<>();

        for (String queryTerm : queryMapWithPosTags.keySet()) {

            String crntTermPosTag = queryMapWithPosTags.get(queryTerm);
            HashSet<String> synset = new HashSet<>();

            if (crntTermPosTag.startsWith("J")) {
                crntSynset = WordNet.getSynonyms(dict, queryTerm, POS.ADJECTIVE);
                if (crntSynset != null) {
                    synset.addAll(crntSynset);
                }
                crntSynset = WordNet.getAntonyms(dict, queryTerm, POS.ADJECTIVE);
                if (crntSynset != null) {
                    synset.addAll(crntSynset);
                }
                crntSynset = WordNet.getHypernyms(dict, queryTerm, POS.ADJECTIVE);
                if (crntSynset != null) {
                    synset.addAll(crntSynset);
                }

            } else if (crntTermPosTag.startsWith("R")) {
                crntSynset = WordNet.getSynonyms(dict, queryTerm, POS.ADVERB);
                if (crntSynset != null) {
                    synset.addAll(crntSynset);
                }
                crntSynset = WordNet.getAntonyms(dict, queryTerm, POS.ADVERB);
                if (crntSynset != null) {
                    synset.addAll(crntSynset);
                }
                crntSynset = WordNet.getHypernyms(dict, queryTerm, POS.ADVERB);
                if (crntSynset != null) {
                    synset.addAll(crntSynset);
                }

            } else if (crntTermPosTag.startsWith("N")) {
                crntSynset = WordNet.getSynonyms(dict, queryTerm, POS.NOUN);
                if (crntSynset != null) {
                    synset.addAll(crntSynset);
                }
                crntSynset = WordNet.getAntonyms(dict, queryTerm, POS.NOUN);
                if (crntSynset != null) {
                    synset.addAll(crntSynset);
                }
                crntSynset = WordNet.getHypernyms(dict, queryTerm, POS.NOUN);
                if (crntSynset != null) {
                    synset.addAll(crntSynset);
                }

            } else if (crntTermPosTag.startsWith("V")) {
                crntSynset = WordNet.getSynonyms(dict, queryTerm, POS.VERB);
                if (crntSynset != null) {
                    synset.addAll(crntSynset);
                }
                crntSynset = WordNet.getAntonyms(dict, queryTerm, POS.VERB);
                if (crntSynset != null) {
                    synset.addAll(crntSynset);
                }
                crntSynset = WordNet.getHypernyms(dict, queryTerm, POS.VERB);
                if (crntSynset != null) {
                    synset.addAll(crntSynset);
                }

            }

            querySynset.addAll(synset);
        }

        HashMap<String, String> commentMapWithPosTags = NlpAnalyzer.getCleanTokensWithPos(this.text);
        HashSet<String> commentSynset = new HashSet<>();

        for (String commentTerm : commentMapWithPosTags.keySet()) {

            String crntTermPosTag = commentMapWithPosTags.get(commentTerm);
            HashSet<String> synset = new HashSet<>();

            if (crntTermPosTag.startsWith("J")) {
                crntSynset = WordNet.getSynonyms(dict, commentTerm, POS.ADJECTIVE);
                if (crntSynset != null) {
                    synset.addAll(crntSynset);
                }
                crntSynset = WordNet.getAntonyms(dict, commentTerm, POS.ADJECTIVE);
                if (crntSynset != null) {
                    synset.addAll(crntSynset);
                }
                crntSynset = WordNet.getHypernyms(dict, commentTerm, POS.ADJECTIVE);
                if (crntSynset != null) {
                    synset.addAll(crntSynset);
                }

            } else if (crntTermPosTag.startsWith("R")) {
                crntSynset = WordNet.getSynonyms(dict, commentTerm, POS.ADVERB);
                if (crntSynset != null) {
                    synset.addAll(crntSynset);
                }
                crntSynset = WordNet.getAntonyms(dict, commentTerm, POS.ADVERB);
                if (crntSynset != null) {
                    synset.addAll(crntSynset);
                }
                crntSynset = WordNet.getHypernyms(dict, commentTerm, POS.ADVERB);
                if (crntSynset != null) {
                    synset.addAll(crntSynset);
                }

            } else if (crntTermPosTag.startsWith("N")) {
                crntSynset = WordNet.getSynonyms(dict, commentTerm, POS.NOUN);
                if (crntSynset != null) {
                    synset.addAll(crntSynset);
                }
                crntSynset = WordNet.getAntonyms(dict, commentTerm, POS.NOUN);
                if (crntSynset != null) {
                    synset.addAll(crntSynset);
                }
                crntSynset = WordNet.getHypernyms(dict, commentTerm, POS.NOUN);
                if (crntSynset != null) {
                    synset.addAll(crntSynset);
                }

            } else if (crntTermPosTag.startsWith("V")) {
                crntSynset = WordNet.getSynonyms(dict, commentTerm, POS.VERB);
                if (crntSynset != null) {
                    synset.addAll(crntSynset);
                }
                crntSynset = WordNet.getAntonyms(dict, commentTerm, POS.VERB);
                if (crntSynset != null) {
                    synset.addAll(crntSynset);
                }
                crntSynset = WordNet.getHypernyms(dict, commentTerm, POS.VERB);
                if (crntSynset != null) {
                    synset.addAll(crntSynset);
                }

            }

            commentSynset.addAll(synset);
        }

        score = StringUtils.JaccardSim((String[]) commentSynset.toArray(new String[0]), (String[]) querySynset.toArray(new String[0]));

        return score;
    }

    public void calculateScore(WordMovers wm, String query, Word2Vec vec, IDictionary dict) throws IOException {
        this.setScore((0.5 * calculateWordSimilarity(wm, query, vec)) + (0.5 * calculateSynsetSimilarity(query, dict)));
    }

    @Override
    public String toString() {
        return "Comment for hotel: " + this.hotel_id + " with id: " + this.id + " says: " + this.text + " posted at: " + this.date + " with score: " + this.score;
    }

}
