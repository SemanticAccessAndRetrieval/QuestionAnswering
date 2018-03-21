/*
 *  This code belongs to the Semantic Access and Retrieval (SAR) group of the
 *  Information Systems Laboratory (ISL) of the
 *  Institute of Computer Science (ICS) of the
 *  Foundation for Research and Technology - Hellas (FORTH)
 *  Nobody is allowed to use, copy, distribute, or modify this work.
 *  It is published for reasons of research results reproducibility.
 *  (c) 2017 Semantic Access and Retrieval group, All rights reserved
 */
package gr.forth.ics.isl.evaluation;

import com.crtomirmajer.wmd4j.WordMovers;
import edu.mit.jwi.Dictionary;
import edu.mit.jwi.IDictionary;
import gr.forth.ics.isl.auxiliaryClasses.Timer;
import static gr.forth.ics.isl.main.demo_main.getCommentsFromTextOnlyKB;
import static gr.forth.ics.isl.main.demo_main.getTopKComments;
import gr.forth.ics.isl.nlp.models.Comment;
import gr.forth.ics.isl.sailInfoBase.QAInfoBase;
import gr.forth.ics.isl.sailInfoBase.models.Subject;
import gr.forth.ics.isl.utilities.StringUtils;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import mitos.stemmer.trie.Trie;
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.word2vec.Word2Vec;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.RepositoryException;

/**
 *
 * @author Sgo
 */


public class HotelDemoEfficiencyTestSuit {

    //Number of top comments to retrieve
    static int topK = 10;

    //The paths for the stopWords Files.
    public static String filePath_en = "src/main/resources/stoplists/stopwordsEn.txt";
    public static String filePath_gr = "src/main/resources/stoplists/stopwordsGr.txt";

    //A hashMap that contains two Trie, one for the English stopList and one for the Greek
    public static HashMap<String, Trie> stopLists = new HashMap<>();

    /*public static ArrayList<Comment> getCommentsFromTextOnlyKB(HashSet<Subject> reviews) {
        ArrayList<Comment> comments = new ArrayList<>();

        for (Subject sub : reviews) {
            HashMap<String, HashSet<String>> crntCommentsProps = sub.getUndeclaredDataTypePropsWithValues();

            String date = crntCommentsProps.get("http://ics.forth.gr/isl/hippalus/#hasDate").iterator().next();
            String text = crntCommentsProps.get("http://ics.forth.gr/isl/hippalus/#hasText").iterator().next();

            Comment tmpComment = new Comment("", "", sub.getUri(), text, date);
            comments.add(tmpComment);
        }

        return comments;
    }*/

    public static void main(String[] args) throws RepositoryException, MalformedQueryException, QueryEvaluationException, IOException {

        //Intance of class timer, for time measurements
        Timer timer = new Timer();

        // Create the list of stopWords to use
        StringUtils.generateStopLists(filePath_en, filePath_gr);

        // Create Word2Vec model
        File gModel = new File("C:/Users/Sgo/Desktop/Developer/Vector Models/GoogleNews-vectors-negative300.bin.gz");

        timer.start();
        Word2Vec vec = WordVectorSerializer.readWord2VecModel(gModel);
        WordMovers wm = WordMovers.Builder().wordVectors(vec).build();
        timer.end();
        long word2vecTime = timer.getTotalTime();
        System.out.println("Time to load word2vec: " + word2vecTime);

        timer.start();
        // Create WodNet Dictionary
        String wnhome = System.getenv("WNHOME");
        String path = wnhome + File.separator + "dict";
        URL url = new URL("file", null, path);
        // construct the dictionary object and open it
        IDictionary dict = new Dictionary(url);
        // open WordNet dictionary
        dict.open();
        timer.end();
        long wordNetTime = timer.getTotalTime();
        System.out.println("Time to load WordNet: " + wordNetTime);

        timer.start();
        QAInfoBase KB = new QAInfoBase();
        // Retrieve hotels
        HashSet<Subject> reviews = KB.getAllSubjectsOfType("hip", "review");

        ArrayList<Comment> comments = getCommentsFromTextOnlyKB(reviews);
        System.out.println(comments.size());
        timer.end();
        long resourcesTime = timer.getTotalTime();
        System.out.println("Time to load resources: " + resourcesTime);

        System.out.println("External Resources were loaded successfully");

        //Get the user's question
        String question = "Has anyone reported a problem about noise?";

        float word2vec_w = 0.4f;
        float wordNet_w = 0.6f;
        float threshold = 0.03f;

        double max_dist = Double.MIN_VALUE;

        Timer min_max = new Timer();
        timer.start();
        //Calculate score for each comment
        //Also calculate max word mover distance
        for (Comment com : comments) {
            min_max.start();
            com.calculateScores(wm, question, vec, dict, word2vec_w, wordNet_w);
            if (com.getWordScore() >= max_dist) {
                max_dist = com.getWordScore();
            }
            min_max.end();
            com.time += min_max.getTotalTime();
        }

        //Normalize WordMoverDistance, and update comments with the final scores
        for (Comment com : comments) {
            min_max.start();
            com.calculateWordScore(max_dist);
            com.calculateScore(word2vec_w, wordNet_w);
            min_max.end();
            com.time += min_max.getTotalTime();
        }

        timer.end();
        long calculateAllScores = timer.getTotalTime();
        System.out.println("Average Time to calculate score (for all reviews): " + calculateAllScores);
        System.out.println("Average Time to calculate score (for one review): " + calculateAllScores / comments.size());

        // Get the best comments based on their score (currently all of them)
        ArrayList<Comment> topComments = getTopKComments(comments, topK);
        ArrayList<Long> minMaxTimes = getMaxMinTime(comments);
        System.out.println(topComments);
        System.out.println(minMaxTimes);
    }

    public static ArrayList<Long> getMaxMinTime(ArrayList<Comment> comments) {
        ArrayList<Comment> topComments = new ArrayList<>();

        // Sort comments by score (in decreasing order)
        Collections.sort(comments, new Comparator<Comment>() {
            public int compare(Comment c1, Comment c2) {
                return -Long.compare(c1.time, c2.time);
            }
        });

        ArrayList<Long> time = new ArrayList<>();
        time.add(comments.get(0).time);
        time.add(comments.get(comments.size() - 1).time);

        return time;
    }
}
