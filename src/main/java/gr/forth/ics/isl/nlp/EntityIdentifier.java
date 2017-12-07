/* 
 *  This code belongs to the Semantic Access and Retrieval (SAR) group of the 
 *  Information Systems Laboratory (ISL) of the 
 *  Institute of Computer Science (ICS) of the  
 *  Foundation for Research and Technology - Hellas (FORTH)
 *  Nobody is allowed to use, copy, distribute, or modify this work.
 *  It is published for reasons of research results reproducibility.
 *  (c) 2017 Semantic Access and Retrieval group, All rights reserved
 */









package gr.forth.ics.isl.nlp;

import gr.forth.ics.isl.nlp.models.Word;
import gr.forth.ics.isl.sailInfoBase.models.Individual;
import gr.forth.ics.isl.utilities.StringUtils;
import gr.forth.ics.isl.utilities.Utils;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;

/**
 *
 * @author Sgo
 */
public class EntityIdentifier {

    /**
     * This method is responsible for computing the dot product of the two input
     * arrays.
     *
     * @param vec1
     * @param vec2
     * @return double dotProduct
     */
    public static double dotProduct(double[] vec1, double[] vec2) {
        double dotProduct = 0.0;
        for (int i = 0; i < vec1.length; i++) {
            dotProduct += vec1[i] * vec2[i];
        }
        return dotProduct;
    }

    /**
     * This purpose of this method is to compute the cosine similarity of the
     * two input vectors.
     *
     * @param vec1
     * @param vec2
     * @return double cosineSimilarity
     */
    public static double cosSim(double[] vec1, double[] vec2) {
        double similarity = dotProduct(vec1, vec2) / (vec1.length * vec2.length);
        return similarity;
    }

    public static double jacardSim(HashSet<String> bag1, HashSet<String> bag2) {
        HashSet<String> intersection = new HashSet<>(bag1);
        intersection.retainAll(bag2);

        HashSet<String> union = new HashSet<>(bag1);
        union.addAll(bag2);

        return (double) intersection.size() / union.size();
    }

    /**
     * This is the main method of the project. Here it is were the vector space
     * model and the input KB's triple-store are been initialized. Moreover,
     * here is where the UI of the project lies. It is actually a dialogue-like
     * interface, where the user is able to query the KB via natural language
     * without any need to know the underlying data scheme. Firstly The input
     * query is processed in order to remove stop words and apply POS and NER
     * tagging and be tokenized. Then the vector representation of each token is
     * retrieved from the word2vec vocabulary, in order to compute the mean
     * vector representation of the whole input query. Finally, the cosine
     * similarity of the produced vector and each individual's mean vector
     * representation is been computed, in order to sort the resources by
     * descending order as a final answer.
     *
     * @param args
     * @throws IOException
     * @throws FileNotFoundException
     * @throws ClassNotFoundException
     */
    public static void main(String[] args) throws IOException, FileNotFoundException, ClassNotFoundException {
        //File gModel = new File("C:/Users/Sgo/Desktop/Developer/Vector Models/GoogleNews-vectors-negative300.bin.gz");
        //Word2Vec vec = WordVectorSerializer.readWord2VecModel(gModel);
        ArrayList<Individual> individuals = (ArrayList<Individual>) Utils.getSavedObject("individuals");

        while (true) {
            //Create a scanner to read from keyboard
            Scanner in = new Scanner(System.in);

            System.out.println("Submit your question");
            String question = in.nextLine();

            ArrayList<Word> questionsWords = NlpAnalyzer.getUniqWordsWithPosNer(question);
            HashSet<String> questionsTerms = new HashSet<>();
            for (Word word : questionsWords) {
                //questionsTerms.add(word.getNer());
                if (!StringUtils.isStopWord(word.getText())) {
                    questionsTerms.add(word.getText());
                }
            }

            /*
            // compute the mean vector representation cosine similarity between
            // the query and each individual
            int cnt = 0;
            double[] meanOfCurrentIndi = new double[300];
            for (String term : questionsTerms) {
                cnt += 1;
                if (cnt == 1) {
                    meanOfCurrentIndi = vec.getWordVector(term);
                } else {
                    meanOfCurrentIndi = addVectors(meanOfCurrentIndi, vec.getWordVector(term));
                }
            }
            meanOfCurrentIndi = normalizeVector(meanOfCurrentIndi, meanOfCurrentIndi.length);

            HashMap<String, Double> rankedIndividuals = new HashMap<>();
            for (Individual indi : individuals) {
                rankedIndividuals.put(indi.getURI(), cosSim(meanOfCurrentIndi, indi.getVectorRepresentation()));
            }
             */
            // compute the bag of words representation jacard similarity between
            // the query and each individual
            HashMap<String, Double> rankedIndividuals = new HashMap<>();
            for (Individual ind : individuals) {
                rankedIndividuals.put(ind.getURI(), jacardSim(ind.getbagOfWordsRepresentation(), questionsTerms));
            }
            //System.out.println("Submit the answer's entity type");
            //String question_type = in.nextLine();
            System.out.println(Utils.sortByValue(rankedIndividuals));
            //Get the user's question
            //String question = JOptionPane.showInputDialog("Submit your question","");
            //Get the answer type of the question
            //String question_type = JOptionPane.showInputDialog("Submit the answer's entity type","");
        }
    }
}
