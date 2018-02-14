/*
 *  This code belongs to the Semantic Access and Retrieval (SAR) group of the 
 *  Information Systems Laboratory (ISL) of the 
 *  Institute of Computer Science (ICS) of the  
 *  Foundation for Research and Technology - Hellas (FORTH)
 *  Nobody is allowed to use, copy, distribute, or modify this work.
 *  It is published for reasons of research results reproducibility.
 *  (c) 2017 Semantic Access and Retrieval group, All rights reserved
 */
package gr.forth.ics.isl.main;

import gr.forth.ics.isl.nlp.models.Comment;
import gr.forth.ics.isl.nlp.models.Question;
import gr.forth.ics.isl.sailInfoBase.QAInfoBase;
import gr.forth.ics.isl.sailInfoBase.models.Subject;
import gr.forth.ics.isl.utilities.StringUtils;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import javax.swing.JOptionPane;
import mitos.stemmer.trie.Trie;
import org.apache.lucene.queryparser.classic.ParseException;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.RepositoryException;

/**
 *
 * @author Lefteris Dimitrakis
 */
public class demo_main {

    //Number of top comments to retrieve
    static int topK = 1;

    //The paths for the stopWords Files.
    public static String filePath_en = "src/main/resources/stoplists/stopwordsEn.txt";
    public static String filePath_gr = "src/main/resources/stoplists/stopwordsGr.txt";

    //A hashMap that contains two Trie, one for the English stopList and one for the Greek
    public static HashMap<String, Trie> stopLists = new HashMap<>();

    public static void main(String[] args) throws IOException, ParseException, FileNotFoundException, ClassNotFoundException, RepositoryException, MalformedQueryException, QueryEvaluationException {

        //Create the list of stopWords to use
        StringUtils.generateStopLists(filePath_en, filePath_gr);

        QAInfoBase KB = new QAInfoBase();

        HashSet<Subject> hotels = KB.getAllSubjectsOfType("hippalus", "hippalusID");


        while (true) {
            //Get the user's question
            String question = JOptionPane.showInputDialog("Submit your question", "");

            //Create a Question instance for the submitted question
            Question quest = new Question();
            quest.setQuestion(question);

            // Apply tokenization and lemmatization in the input question
            ArrayList<String> words = new ArrayList<>(Arrays.asList(quest.prepareText(question)));

            System.out.println(words);

            // Remove the stopwords from the question
            words = StringUtils.removeStopWords(words);

            System.out.println(words);

            // Get all the comments
            ArrayList<Comment> comments = getComments(hotels, KB);

            //Calculate score for each comment
            for (Comment com : comments) {
                com.calculateScore();
            }

            System.out.println(comments);

            // Sort comments by date (from the most recent to the least)
            Collections.sort(comments, new Comparator<Comment>() {
                public int compare(Comment c1, Comment c2) {
                    if (c1.getDate() == null || c2.getDate() == null) {
                        return 0;
                    }
                    return -c1.getDate().compareTo(c2.getDate());
                }
            });

            // System out the comments
            System.out.println(comments);

            // Get the best comments based on their score
            ArrayList<Comment> topComments = getTopKComments(comments, topK);

            System.out.println(topComments);
        }

    }

    public static ArrayList<Comment> getComments(HashSet<Subject> allSubjectsOfType, QAInfoBase KB) throws RepositoryException, MalformedQueryException, QueryEvaluationException {
        ArrayList<Comment> comments = new ArrayList<>();

        for (Subject sub : allSubjectsOfType) {
            HashMap<String, HashSet<String>> crntHotelObjectProps = sub.getObjectPropsWithObjectValues();
            for (String prop : crntHotelObjectProps.keySet()) {
                HashSet<String> crntHotelCommentIds = crntHotelObjectProps.get(prop);
                for (String commentId : crntHotelCommentIds) {
                    HashMap<String, HashSet<String>> commentProps = KB.getAllUndeclaredDataTypePropertiesWithValuesOf(commentId);

                    String date = commentProps.get("http://ics.forth.gr/isl/hippalus/#hasDate").iterator().next();
                    String text = commentProps.get("http://ics.forth.gr/isl/hippalus/#hasText").iterator().next();

                    Comment tmpComment = new Comment(prop, commentId, text, date);
                    comments.add(tmpComment);
                }
            }
        }

        return comments;
    }

    public static ArrayList<Comment> getTopKComments(ArrayList<Comment> comments, int topK) {
        ArrayList<Comment> topComments = new ArrayList<>();

        // Sort comments by score (in decreasing order)
        Collections.sort(comments, new Comparator<Comment>() {
            public int compare(Comment c1, Comment c2) {
                return -Double.compare(c1.getScore(), c2.getScore());
            }
        });

        // Get the top Comments
        if (topK <= comments.size()) {
            topComments = new ArrayList<>(comments.subList(0, topK));
        } else {
            topComments = comments;
        }

        return topComments;
    }
}
