/*
 *  This code belongs to the Semantic Access and Retrieval (SAR) group of the
 *  Information Systems Laboratory (ISL) of the
 *  Institute of Computer Science (ICS) of the
 *  Foundation for Research and Technology - Hellas (FORTH)
 *  Nobody is allowed to use, copy, distribute, or modify this work.
 *  It is published for reasons of research results reproducibility.
 *  (c) 2017 Semantic Access and Retrieval group, All rights reserved
 */
package gr.forth.ics.isl.demo.evaluation;

import gr.forth.ics.isl.demo.evaluation.models.EvaluationComment;
import gr.forth.ics.isl.demo.evaluation.models.EvaluationPair;
import gr.forth.ics.isl.demo.evaluation.models.EvaluationQuery;
import static gr.forth.ics.isl.main.demo_main.filePath_en;
import static gr.forth.ics.isl.main.demo_main.filePath_gr;
import static gr.forth.ics.isl.main.demo_main.getCommentsFromBooking;
import gr.forth.ics.isl.nlp.models.Comment;
import gr.forth.ics.isl.sailInfoBase.QAInfoBase;
import gr.forth.ics.isl.sailInfoBase.models.Subject;
import gr.forth.ics.isl.utilities.StringUtils;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;
import javax.swing.JOptionPane;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.RepositoryException;

/**
 *
 * @author Sgo
 */
public class EvalCollectionManipulator {

    public static void main(String[] args) throws RepositoryException, IOException, MalformedQueryException, QueryEvaluationException {

        build("BookingEvalCollection.csv");

    }

    public static void build(String fileName) throws RepositoryException, IOException, MalformedQueryException, QueryEvaluationException {
        //Retrieve the available comments
        QAInfoBase KB = new QAInfoBase();
        HashSet<Subject> commentsAsSubs = KB.getAllSubjectsOfType("hip", "review");
        ArrayList<Comment> comments = getCommentsFromBooking((commentsAsSubs));

        //Create the list of stopWords to use
        StringUtils.generateStopLists(filePath_en, filePath_gr);

        HashMap<String, HashMap<String, EvaluationPair>> evaluationSet = readEvaluationSet(fileName);

        PrintWriter writer = new PrintWriter(new FileOutputStream(
                new File("src/main/resources/evaluation/" + fileName),
                true));

        //int queryCnt = 1;
        int pairCnt = getMaxPairID(evaluationSet) + 1;
        System.out.println(pairCnt);
        boolean onUse = true;

        while (onUse) {
            try {
                //Get the user's question
                String question = JOptionPane.showInputDialog("Submit your question", "");
                String query_id = JOptionPane.showInputDialog("Submit query_id", "");

                HashMap<String, EvaluationPair> pairsWithQueryId = evaluationSet.get(query_id);

                for (Comment com : comments) {

                    if (!pairsWithQueryId.containsKey(com.getId())) {
                        System.out.println(com.getText());
                        Scanner insertRel = new Scanner(System.in);
                        String relevance = insertRel.nextLine();

                        if ("1".equals(relevance)) {
                            writer.println(pairCnt + ";;" + query_id + ";;" + question + ";;" + com.getId() + ";;" + com.getText() + ";;" + com.getDate() + ";;" + "1");
                            pairCnt++;
                        } else if ("0".equals(relevance)) {
                            writer.println(pairCnt + ";;" + query_id + ";;" + question + ";;" + com.getId() + ";;" + com.getText() + ";;" + com.getDate() + ";;" + "0");
                            pairCnt++;
                        } else if ("q".equals(relevance)) {
                            onUse = false;
                            break;
                        } else {
                            System.out.println("Review skiped");
                        }
                    }

                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
                continue;
            }

        }
        writer.close();
    }

    public static int getMaxPairID(HashMap<String, HashMap<String, EvaluationPair>> evaluationSet) {
        int max_id = 0;

        for (String query_id : evaluationSet.keySet()) {

            HashMap<String, EvaluationPair> pairsWithQueryId = evaluationSet.get(query_id);
            for (EvaluationPair pair : pairsWithQueryId.values()) {
                if (max_id <= pair.getId()) {
                    max_id = pair.getId();
                }

            }

        }

        return max_id;
    }

    /**
     * This method is used to create our evaluation structure based on the
     * evaluation collection.
     *
     * @param fileName
     * @return HashMap<String, ArrayList<EvaluationPair>> groundTruth
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static HashMap<String, HashMap<String, EvaluationPair>> readEvaluationSet(String fileName) throws FileNotFoundException, IOException {
        HashMap<String, HashMap<String, EvaluationPair>> groundTruth = new HashMap<>();
        String line;
        BufferedReader br = new BufferedReader(new FileReader("src/main/resources/evaluation/" + fileName));
        while ((line = br.readLine()) != null) {

            // use comma as separator
            String[] tuple = line.split(";;");

            int eval_pair_id = Integer.parseInt(tuple[0].trim());
            //System.out.println(eval_pair_id);
            String query_id = tuple[1];
            String query_text = tuple[2];
            String comment_id = tuple[3];
            String comment_text = tuple[4];
            String comment_date = tuple[5];
            int relevance = Integer.parseInt(tuple[6].trim());

            EvaluationQuery evalQuery = new EvaluationQuery(query_id, query_text);
            EvaluationComment evalComment = new EvaluationComment(comment_id, comment_text, comment_date);
            EvaluationPair evalPair = new EvaluationPair(eval_pair_id, evalQuery, evalComment, relevance);

            HashMap<String, EvaluationPair> pairsWithQueryId = groundTruth.get(query_id);
            if (pairsWithQueryId == null) {
                pairsWithQueryId = new HashMap<>();
                pairsWithQueryId.put(evalPair.getComment().getId(), evalPair);
                groundTruth.put(query_id, pairsWithQueryId);
            } else {
                pairsWithQueryId.put(evalPair.getComment().getId(), evalPair);
                groundTruth.put(query_id, pairsWithQueryId);
            }

        }

        return groundTruth;
    }

    /**
     * This method computes and return the number of relevant comments for a
     * specified query.
     *
     * @param evalPairsWithQueryId
     * @return
     */
    public static int getNumOfRels(HashMap<String, EvaluationPair> evalPairsWithQueryId, int relThreshold) {
        int R = 0;
        for (EvaluationPair ep : evalPairsWithQueryId.values()) {
            if (ep.getRelevance() > relThreshold) {
                R++;
            }
        }
        return R;
    }
}
