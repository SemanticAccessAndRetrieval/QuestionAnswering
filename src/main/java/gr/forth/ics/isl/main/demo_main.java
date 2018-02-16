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

import com.crtomirmajer.wmd4j.WordMovers;
import edu.mit.jwi.Dictionary;
import edu.mit.jwi.IDictionary;
import gr.forth.ics.isl.nlp.models.Comment;
import gr.forth.ics.isl.sailInfoBase.QAInfoBase;
import gr.forth.ics.isl.sailInfoBase.models.Subject;
import gr.forth.ics.isl.utilities.StringUtils;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;
import javax.swing.JOptionPane;
import mitos.stemmer.trie.Trie;
import org.apache.lucene.queryparser.classic.ParseException;
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.word2vec.Word2Vec;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.RepositoryException;

/**
 *
 * @author Lefteris Dimitrakis
 */
public class demo_main {

    //Number of top comments to retrieve
    static int topK = 10;

    //The paths for the stopWords Files.
    public static String filePath_en = "src/main/resources/stoplists/stopwordsEn.txt";
    public static String filePath_gr = "src/main/resources/stoplists/stopwordsGr.txt";

    //A hashMap that contains two Trie, one for the English stopList and one for the Greek
    public static HashMap<String, Trie> stopLists = new HashMap<>();

    public static void main(String[] args) throws IOException, ParseException, FileNotFoundException, ClassNotFoundException, RepositoryException, MalformedQueryException, QueryEvaluationException {

        //Create the list of stopWords to use
        StringUtils.generateStopLists(filePath_en, filePath_gr);

        File gModel = new File("C:/Users/Sgo/Desktop/Developer/Vector Models/GoogleNews-vectors-negative300.bin.gz");
        Word2Vec vec = WordVectorSerializer.readWord2VecModel(gModel);
        WordMovers wm = WordMovers.Builder().wordVectors(vec).build();

        String wnhome = System.getenv("WNHOME");
        String path = wnhome + File.separator + "dict";
        URL url = new URL("file", null, path);
        // construct the dictionary object and open it
        IDictionary dict = new Dictionary(url);
        dict.open();

        QAInfoBase KB = new QAInfoBase();
        HashSet<Subject> hotels = KB.getAllSubjectsOfType("hippalus", "hippalusID");

        System.out.println("External Resources were loaded successfully");

        while (true) {
            try {
                Scanner in = new Scanner(System.in);

                //Get the user's question
                String question = JOptionPane.showInputDialog("Submit your question", "");

                //Get the weights for the scoring
                System.out.println("Enter word2vec weight: ");
                float word2vec_w = in.nextFloat();
                System.out.println("Enter wordNet weight: ");
                float wordNet_w = in.nextFloat();
                //Get the threshold for relevancy
                System.out.println("Enter threshold: ");
                float threshold = in.nextFloat();

                // Get all the comments
                ArrayList<Comment> comments = getComments(hotels, KB);

                double max_dist = Double.MIN_VALUE;
                //Calculate score for each comment
                //Also calculate max word mover distance
                for (Comment com : comments) {
                    com.calculateScores(wm, question, vec, dict);
                    if (com.getWordScore() >= max_dist) {
                        max_dist = com.getWordScore();
                    }
                }

                //Normalize WordMoverDistance, and update comments with the final scores
                for (Comment com : comments) {
                    com.calculateWordScore(max_dist);
                    com.calculateScore(word2vec_w, wordNet_w);
                }

                // Get the best comments based on their score (currently all of them)
                ArrayList<Comment> topComments = getTopKComments(comments, topK);


                ArrayList<Comment> relevantComments = new ArrayList<>();
                HashMap<String, ArrayList<Comment>> commentsPerHotel = new HashMap<>();
                ArrayList<Comment> tmp;
                int rank = 1;

                for (Comment c : topComments) {
                    System.out.println("\nRank:" + rank + "\nComment:" + c.getText() + "\nScore:" + String.format("%.3f", c.getScore()) + "\nFor Hotel:" + c.getHotelId());
                    rank++;

                    // Filter comments based on the threshold
                    if (c.getScore() >= threshold) {
                        relevantComments.add(c);
                    }
                    // Group comments based on the hotel (commentsPerHotel)
                    if (!commentsPerHotel.containsKey(c.getHotelId())) {
                        tmp = new ArrayList<>();
                        tmp.add(c);
                        commentsPerHotel.put(c.getHotelId(), tmp);
                    } else {
                        tmp = commentsPerHotel.get(c.getHotelId());
                        tmp.add(c);
                        commentsPerHotel.put(c.getHotelId(), tmp);
                    }
                }

                //System.out.println(relevantComments);

                System.out.println("\nI have found " + relevantComments.size() + " comments.(" + String.format("%.1f", ((float) relevantComments.size() / topComments.size()) * 100.0) + "%)");
                System.out.println("The most related comment says: " + relevantComments.get(0).getText());

                // Sort comments by date (from the most recent to the least)
                Collections.sort(relevantComments, new Comparator<Comment>() {
                    public int compare(Comment c1, Comment c2) {
                        if (c1.getDate() == null || c2.getDate() == null) {
                            return 0;
                        }
                        return -c1.getDate().compareTo(c2.getDate());
                    }
                });
                System.out.println("The most recent comment says: " + relevantComments.get(0).getText());

                for (String hotel_id : commentsPerHotel.keySet()) {
                    System.out.println("=================================================");
                    System.out.println("Comments for hotel: " + hotel_id + "\n");
                    for (Comment c : commentsPerHotel.get(hotel_id)) {
                        System.out.println("TEXT: " + c.getText());
                        System.out.println("SCORE: " + String.format("%.3f", c.getScore()) + "\n");
                    }
                }


            } catch (Exception e) {
                System.out.println(e.getMessage());
                continue;
            }
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

                    Comment tmpComment = new Comment(sub.getUri(), commentId, text, date);
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
