///*
// *  This code belongs to the Semantic Access and Retrieval (SAR) group of the
// *  Information Systems Laboratory (ISL) of the
// *  Institute of Computer Science (ICS) of the
// *  Foundation for Research and Technology - Hellas (FORTH)
// *  Nobody is allowed to use, copy, distribute, or modify this work.
// *  It is published for reasons of research results reproducibility.
// *  (c) 2017 Semantic Access and Retrieval group, All rights reserved
// */
//package gr.forth.ics.isl.demo.evaluation;
//
//import com.crtomirmajer.wmd4j.WordMovers;
//import edu.mit.jwi.Dictionary;
//import edu.mit.jwi.IDictionary;
//import gr.forth.ics.isl.demo.auxiliaryClasses.Timer;
//import gr.forth.ics.isl.demo.evaluation.models.ModelHyperparameters;
//import static gr.forth.ics.isl.main.demo_main.filePath_en;
//import static gr.forth.ics.isl.main.demo_main.filePath_gr;
//import static gr.forth.ics.isl.main.demo_main.getCommentsFromTextOnlyKB;
//import static gr.forth.ics.isl.main.demo_main.getTopKComments;
//import gr.forth.ics.isl.nlp.models.Comment;
//import gr.forth.ics.isl.sailInfoBase.QAInfoBase;
//import gr.forth.ics.isl.sailInfoBase.models.Subject;
//import gr.forth.ics.isl.utilities.StringUtils;
//import gr.forth.ics.isl.utilities.Utils;
//import java.io.File;
//import java.io.IOException;
//import java.io.PrintWriter;
//import java.net.URL;
//import java.util.ArrayList;
//import java.util.HashSet;
//import java.util.Scanner;
//import javax.swing.JOptionPane;
//import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
//import org.deeplearning4j.models.word2vec.Word2Vec;
//import org.openrdf.query.MalformedQueryException;
//import org.openrdf.query.QueryEvaluationException;
//import org.openrdf.repository.RepositoryException;
//
///**
// *
// * @author Sgo
// */
//
//
//public class EvalCollectionBuilder {
//
//    private static int topK = 20;
//
//    public static void main(String[] args) throws RepositoryException, IOException, MalformedQueryException, QueryEvaluationException {
//        Timer timer = new Timer();
//        timer.start();
//
//        QAInfoBase KB = new QAInfoBase();
//        HashSet<Subject> commentsAsSubs = KB.getAllSubjectsOfType("hip", "review");
//        ArrayList<Comment> comments = getCommentsFromTextOnlyKB((commentsAsSubs));
//
//        timer.end();
//        long initTime = timer.getTotalTime();
//
//        //Create the list of stopWords to use
//        StringUtils.generateStopLists(filePath_en, filePath_gr);
//
//        File gModel = new File("C:/Users/Sgo/Desktop/Developer/Vector Models/GoogleNews-vectors-negative300.bin.gz");
//        Word2Vec vec = WordVectorSerializer.readWord2VecModel(gModel);
//        WordMovers wm = WordMovers.Builder().wordVectors(vec).build();
//
//        String wnhome = System.getenv("WNHOME");
//        String path = wnhome + File.separator + "dict";
//        URL url = new URL("file", null, path);
//        // construct the dictionary object and open it
//        IDictionary dict = new Dictionary(url);
//        dict.open();
//
//        System.out.println("External Resources were loaded successfully");
//
//        PrintWriter writer = new PrintWriter("src/main/resources/evaluation/hotelsTestCollectionB.csv", "UTF-8");
//
//        int queryCnt = 1;
//        int pairCnt = 1;
//        boolean onUse = true;
//
//        while (onUse) {
//            try {
//                Scanner in = new Scanner(System.in);
//
//                //Get the user's question
//                String question = JOptionPane.showInputDialog("Submit your question", "");
//
//                //Get best performing model
//                ModelHyperparameters retrievalModel = (ModelHyperparameters) Utils.getSavedObject("AVEPbased_BestModel");
//                //Get the weights for the scoring
//                //System.out.println("Enter word2vec weight: ");
//                //float word2vec_w = in.nextFloat();
//                float word2vec_w = retrievalModel.getWord2vecWeight();
//                System.out.println("word2vec weight: " + word2vec_w);
//                //System.out.println("Enter wordNet weight: ");
//                //float wordNet_w = in.nextFloat();
//                float wordNet_w = retrievalModel.getWordNetWeight();
//                System.out.println("wordNet weight: " + wordNet_w);
//                //Get the threshold for relevancy
//                //System.out.println("Enter threshold: ");
//                //float threshold = in.nextFloat();
//                float threshold = retrievalModel.getThreshold();
//                System.out.println("threshold: " + threshold);
//
//                double max_dist = Double.MIN_VALUE;
//                //Calculate score for each comment
//                //Also calculate max word mover distance
//                for (Comment com : comments) {
//                    com.calculateScores(wm, question, vec, dict, word2vec_w, wordNet_w);
//                    if (com.getWordScore() >= max_dist) {
//                        max_dist = com.getWordScore();
//                    }
//                }
//
//                //Normalize WordMoverDistance, and update comments with the final scores
//                for (Comment com : comments) {
//                    com.calculateWordScore(max_dist);
//                    com.calculateScore(word2vec_w, wordNet_w);
//                }
//
//                // Get the best comments based on their score (currently all of them)
//                ArrayList<Comment> topComments = getTopKComments(comments, topK);
//
//                for (Comment com : topComments) {
//                    System.out.println(com.getText());
//
//                    Scanner insertRel = new Scanner(System.in);
//                    String relevance = insertRel.nextLine();
//
//                    if ("1".equals(relevance)) {
//                        writer.println(pairCnt + ",q" + queryCnt + "," + com.getId() + "," + com.getDate() + "," + "1");
//                        pairCnt++;
//                    } else if ("0".equals(relevance)) {
//                        writer.println(pairCnt + ",q" + queryCnt + "," + com.getId() + "," + com.getDate() + "," + "0");
//                        pairCnt++;
//                    } else if ("q".equals(relevance)) {
//                        onUse = false;
//                        break;
//                    } else {
//                        System.out.println("Review skiped");
//                    }
//                }
//
//                queryCnt++;
//
//            } catch (Exception e) {
//                System.out.println(e.getMessage());
//                continue;
//            }
//
//            System.out.println(initTime);
//            //System.out.println(comments);
//        }
//        writer.close();
//    }
//}
