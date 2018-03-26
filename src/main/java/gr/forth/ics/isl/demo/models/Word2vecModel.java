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

import com.crtomirmajer.wmd4j.WordMovers;
import gr.forth.ics.isl.nlp.NlpAnalyzer;
import gr.forth.ics.isl.nlp.models.Comment;
import java.util.ArrayList;
import org.deeplearning4j.models.word2vec.Word2Vec;

/**
 *
 * @author Lefteris Dimitrakis
 */
public class Word2vecModel extends Model {

    private WordMovers wordMovers;
    private final Word2Vec w2_vector;
    private double maxWMD = 0.0;

    public Word2vecModel(String description, WordMovers wm, Word2Vec w2v, ArrayList<Comment> comments) {
        super.setDescription(description);
        super.setComments(comments);
        this.wordMovers = wm;
        this.w2_vector = w2v;
    }

    public WordMovers getWordMovers() {
        return this.wordMovers;
    }

    public Word2Vec getWord2Vec() {
        return this.w2_vector;
    }

    public void setWordMovers(WordMovers wm) {
        this.wordMovers = wm;
    }

    @Override
    public void scoreComments(String query) {
        this.calculateMaxWMD(query);

        for (Comment com : this.getComments()) {
            this.scoreComment(com, query);
        }
    }

    @Override
    public void scoreComment(Comment com, String query) {
        double maxScore = Double.MIN_VALUE;
        double tmpDistance, tmpScore;
        String best_sentence = "";

        for (String sentence : NlpAnalyzer.getSentences(com.getText())) {
            tmpDistance = calculateWordMoversDistance(this.wordMovers, query, sentence, this.w2_vector);

            if (tmpDistance == -1.0f) {
                tmpDistance = this.maxWMD;
            }
            tmpScore = 1.0f - tmpDistance / this.maxWMD;

            if (tmpScore >= maxScore) {
                maxScore = tmpScore;
                best_sentence = sentence;
            }
        }
        com.setBestSentence(best_sentence);
        com.setScore(maxScore);
    }

    private double calculateWordMoversDistance(WordMovers wm, String query, String text, Word2Vec vec) {
        double distance = 0.0;

        ArrayList<String> querySet = NlpAnalyzer.getCleanTokens(query);
        String queryClean = "";
        //Filter query words not contained in word2vec vocabulary
        for (String queryTerm : querySet) {
            if (vec.hasWord(queryTerm)) {
                queryClean += " " + queryTerm;
            }
        }

        ArrayList<String> commentSet = NlpAnalyzer.getCleanTokens(text);
        String commentClean = "";
        //Filter comment words not contained in word2vec vocabulary
        for (String commentTerm : commentSet) {
            if (vec.hasWord(commentTerm)) {
                commentClean += " " + commentTerm;
            }
        }
        try {
            distance = wm.distance(commentClean, queryClean);

        } catch (Exception e) {
            //System.out.println("Comment: " + commentClean);
            //System.out.println("Query: " + queryClean);
            //e.printStackTrace();
            //System.out.println(e.getMessage());
            return -1.0;
        }
        return distance;
    }


    public void calculateMaxWMD(String query) {
        double maxDistance = Double.MIN_VALUE;
        double tmpDistance;

        for (Comment com : this.getComments()) {
            for (String sentence : NlpAnalyzer.getSentences(com.getText())) {
                tmpDistance = calculateWordMoversDistance(this.wordMovers, query, sentence, this.w2_vector);

                if (tmpDistance == -1.0f) {
                    continue;
                }

                if (tmpDistance >= maxDistance) {
                    maxDistance = tmpDistance;
                }
            }
        }
        this.maxWMD = maxDistance;
    }

}
