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

import com.crtomirmajer.wmd4j.WordMovers;
import edu.mit.jwi.Dictionary;
import edu.mit.jwi.IDictionary;
import gr.forth.ics.isl.demo.evaluation.models.ModelHyperparameters;
import gr.forth.ics.isl.demo.models.WordnetWord2vecModel;
import static gr.forth.ics.isl.main.demo_main.filePath_en;
import static gr.forth.ics.isl.main.demo_main.filePath_gr;
import static gr.forth.ics.isl.main.demo_main.getCommentsFromTextOnlyKB;
import gr.forth.ics.isl.nlp.models.Comment;
import gr.forth.ics.isl.sailInfoBase.QAInfoBase;
import gr.forth.ics.isl.sailInfoBase.models.Subject;
import gr.forth.ics.isl.utilities.StringUtils;
import gr.forth.ics.isl.utilities.Utils;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;
import javax.swing.JOptionPane;
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.word2vec.Word2Vec;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.RepositoryException;

/**
 *
 * @author Sgo
 */
public class EvalCollectionBuilder {
    
    private static int topK = 20;
    
    public static void main(String[] args) throws RepositoryException, IOException, MalformedQueryException, QueryEvaluationException {

        //Retrieve the available comments
        QAInfoBase KB = new QAInfoBase();
        HashSet<Subject> commentsAsSubs = KB.getAllSubjectsOfType("hip", "review");
        ArrayList<Comment> comments = getCommentsFromTextOnlyKB((commentsAsSubs));

        //Create the list of stopWords to use
        StringUtils.generateStopLists(filePath_en, filePath_gr);

        //Load the word2vec model and the WMD
        File gModel = new File("C:/Users/Sgo/Desktop/Developer/Vector Models/GoogleNews-vectors-negative300.bin.gz");
        Word2Vec vec = WordVectorSerializer.readWord2VecModel(gModel);
        WordMovers wm = WordMovers.Builder().wordVectors(vec).build();

        //Load and open Wordnet dictionary
        String wnhome = System.getenv("WNHOME");
        String path = wnhome + File.separator + "dict";
        URL url = new URL("file", null, path);
        // construct the dictionary object and open it
        IDictionary dict = new Dictionary(url);
        dict.open();
        
        PrintWriter writer = new PrintWriter("src/main/resources/evaluation/hotelsTestCollectionB.csv", "UTF-8");
        
        int queryCnt = 1;
        int pairCnt = 1;
        boolean onUse = true;

        //Define wordnet resources to retrieve for the model
        ArrayList<String> wordnetResources = new ArrayList<>();
        wordnetResources.add("synonyms");
        wordnetResources.add("antonyms");
        wordnetResources.add("hypernyms");
        
        while (onUse) {
            try {
                Scanner in = new Scanner(System.in);
                HashMap<String, Float> model_weights = new HashMap<>();

                //Get the user's question
                String question = JOptionPane.showInputDialog("Submit your question", "");

                //Get best performing model
                ModelHyperparameters retrievalModel = (ModelHyperparameters) Utils.getSavedObject("AVEPbased_BestModel");

                //Get the weights for the scoring
                float word2vec_w = retrievalModel.getWord2vecWeight();
                model_weights.put("word2vec", word2vec_w);
                System.out.println("word2vec weight: " + word2vec_w);

                float wordNet_w = retrievalModel.getWordNetWeight();
                model_weights.put("wordnet", wordNet_w);
                System.out.println("wordNet weight: " + wordNet_w);

                //Create an instance of the combined model (Wordnet-Word2vec)
                WordnetWord2vecModel combination = new WordnetWord2vecModel("Word2vec and Wordnet", dict, wordnetResources, wm, vec, model_weights, comments);

                //Perform the scoring of the comments
                combination.scoreComments(question);

                // Get the best comments based on their score (currently all of them)
                ArrayList<Comment> topComments = combination.getTopComments(topK);
                
                for (Comment com : topComments) {
                    System.out.println(com.getText());
                    
                    Scanner insertRel = new Scanner(System.in);
                    String relevance = insertRel.nextLine();
                    
                    if ("1".equals(relevance)) {
                        writer.println(pairCnt + ",q" + queryCnt + "," + com.getId() + "," + com.getDate() + "," + "1");
                        //writer.println(pairCnt + ",q" + queryCnt + "," + com.getId() + "," + com.getDate() + "," + "1" + "," + com.getText());
                        pairCnt++;
                    } else if ("0".equals(relevance)) {
                        writer.println(pairCnt + ",q" + queryCnt + "," + com.getId() + "," + com.getDate() + "," + "0");
                        //writer.println(pairCnt + ",q" + queryCnt + "," + com.getId() + "," + com.getDate() + "," + "0" + "," + com.getText());
                        pairCnt++;
                    } else if ("q".equals(relevance)) {
                        onUse = false;
                        break;
                    } else {
                        System.out.println("Review skiped");
                    }
                }
                
                queryCnt++;
                
            } catch (Exception e) {
                System.out.println(e.getMessage());
                continue;
            }
            
        }
        writer.close();
    }
}
