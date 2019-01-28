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

import gr.forth.ics.isl.nlp.NlpAnalyzer;
import gr.forth.ics.isl.nlp.models.Comment;
import gr.forth.ics.isl.utilities.StringUtils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Lefteris Dimitrakis
 */
public class BaselineModel extends Model {

    public BaselineModel(String description, ArrayList<Comment> comments) {
        super.setDescription(description);
        super.setComments(comments);
    }

    public BaselineModel(String description) {
        super.setDescription(description);
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
                tmpScore = JaccardSimilarity(query, sentence);

                if (tmpScore >= maxScore) {
                    maxScore = tmpScore;
                    best_sentence = sentence;
                }
            } catch (IOException ex) {
                Logger.getLogger(BaselineModel.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        com.setScore(maxScore);
        com.setBestSentence(best_sentence);
    }

    private double JaccardSimilarity(String query, String text) throws IOException {

        double similarity;

        ArrayList<String> queryWords = NlpAnalyzer.getCleanTokens(query);
        ArrayList<String> textWords = NlpAnalyzer.getCleanTokens(text);

        similarity = StringUtils.JaccardSim((String[]) textWords.toArray(new String[0]), (String[]) queryWords.toArray(new String[0]));

        return similarity;
    }

}
