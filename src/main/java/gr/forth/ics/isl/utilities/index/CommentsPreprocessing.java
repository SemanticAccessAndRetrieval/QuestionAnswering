/*
 *  This code belongs to the Semantic Access and Retrieval (SAR) group of the
 *  Information Systems Laboratory (ISL) of the
 *  Institute of Computer Science (ICS) of the
 *  Foundation for Research and Technology - Hellas (FORTH)
 *  Nobody is allowed to use, copy, distribute, or modify this work.
 *  It is published for reasons of research results reproducibility.
 *  (c) 2017 Semantic Access and Retrieval group, All rights reserved
 */
package gr.forth.ics.isl.utilities.index;

import edu.mit.jwi.Dictionary;
import edu.mit.jwi.IDictionary;
import gr.forth.ics.isl.demo.models.WordnetModel;
import static gr.forth.ics.isl.main.demo_main.getComments;
import gr.forth.ics.isl.nlp.NlpAnalyzer;
import gr.forth.ics.isl.nlp.models.Comment;
import gr.forth.ics.isl.sailInfoBase.QAInfoBase;
import gr.forth.ics.isl.sailInfoBase.models.Subject;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.RepositoryException;

/**
 *
 * @author Sgo
 */
public class CommentsPreprocessing {

    private static String commentsDirPath = "src/main/resources/expandedCommentCorpus/";
    private static String KBprefix = "http://ics.forth.gr/isl/hippalus/#";

    public static void main(String[] args) throws MalformedURLException, IOException, RepositoryException, MalformedQueryException, QueryEvaluationException {
        String wnhome = System.getenv("WNHOME");
        String path = wnhome + File.separator + "dict";
        URL url = new URL("file", null, path);
        // construct the dictionary object and open it
        IDictionary dict = new Dictionary(url);
        dict.open();

        QAInfoBase KB = new QAInfoBase();
        HashSet<Subject> hotels = KB.getAllSubjectsOfType("hippalus", "hippalusID");

        System.out.println("External Resources were loaded successfully");

        // Get all the comments
        ArrayList<Comment> comments = getComments(hotels, KB);

        ArrayList<String> wordnetResources = new ArrayList<>();
        wordnetResources.add("synonyms");
        wordnetResources.add("antonyms");
        wordnetResources.add("hypernyms");

        WordnetModel wordnet = new WordnetModel("Wordnet model", dict, wordnetResources, comments);
        String crntTermPosTag;
        HashSet<String> commentSynset;
        HashMap<String, String> commentMapWithPosTags;
        String commentSynsetToString;

        for (Comment com : comments) {
            //Construct comment's wordnet representation
            commentMapWithPosTags = NlpAnalyzer.getCleanTokensWithPos(com.getText());
            commentSynset = new HashSet<>();
            commentSynsetToString = "";

            // expand comment
            for (String commentTerm : commentMapWithPosTags.keySet()) {
                crntTermPosTag = commentMapWithPosTags.get(commentTerm);
                commentSynset.addAll(wordnet.getWordNetResources(crntTermPosTag, dict, commentTerm, wordnetResources));
            }

            for (String term : commentSynset) {
                commentSynsetToString += term + " ";
            }
            // remove prefix from comment id
            String comID = com.getId().replace(KBprefix, "");
            //System.out.println(comID);
            //System.out.println(com.getId().replace(KBprefix, ""));
            //write resulted comment
            PrintWriter writer = new PrintWriter(commentsDirPath + comID + ".txt", "UTF-8");
            writer.println(commentSynsetToString);
            writer.close();
        }

    }
}
