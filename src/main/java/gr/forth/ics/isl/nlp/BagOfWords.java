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
import gr.forth.ics.isl.sailInfoBase.QAInfoBase;
import gr.forth.ics.isl.sailInfoBase.models.Individual;
import gr.forth.ics.isl.sailInfoBase.models.Neighbor;
import gr.forth.ics.isl.utilities.StringUtils;
import gr.forth.ics.isl.utilities.Utils;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;
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
public class BagOfWords {

    //A hashMap that contains two Trie, one for the English stopList and one for the greek
    public static HashMap<String, Trie> stopLists = new HashMap<>();

    /**
     * This method returns the union of two bags of words (ArrayList<String>).
     *
     * @param oldBag
     * @param newBag
     * @return ArrayList<String> concatenated bags
     */
    public static HashSet<String> concatBags(HashSet<String> oldBag, HashSet<String> newBag) {
        oldBag.addAll(newBag);
        return oldBag;
    }

    /**
     * This method splits an input property.in to meaningful tokens. e.g.
     * hasRoom or has_room ==> has room
     *
     * @param property
     * @return ArrayList<String> split Property
     */
    public static HashSet<String> splitProperty(String property) {
        String[] propertySplited = property.split("(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])");
        HashSet<String> propertyAsList = new HashSet<>();
        for (int i = 0; i < propertySplited.length; i++) {
            propertyAsList.add(propertySplited[i].toLowerCase());
        }
        return propertyAsList;
    }

    /**
     * This method is responsible for splitting a paragraph into individual
     * words.
     *
     * @param paragraph
     * @return ArrayList<string> paragraphs terms
     */
    public static HashSet<String> splitParagraph(String paragraph) {

        HashSet<String> paragraphAsBagOfWords = new HashSet<>();
        ArrayList<Word> words = NlpAnalyzer.getUniqWordsWithPosNer(paragraph);

        for (Word word : words) {
            //if (!word.getNer().toLowerCase().equals("o")) {
            //    paragraphAsBagOfWords.add(word.getNer().toLowerCase());
            //}
            paragraphAsBagOfWords.add(word.getText().toLowerCase());
        }

        paragraphAsBagOfWords = StringUtils.removeStopWords(paragraphAsBagOfWords);

        return paragraphAsBagOfWords;
    }

    /**
     * This method is able to create a bag of words, that represents the input
     * individual.
     *
     * @param indi
     * @return ArrayList<String> individual as bag of words
     */
    public static HashSet<String> getIndividualAsBagOfWords(Individual indi) {
        HashSet<String> indiBag = new HashSet<>();
        ArrayList<Neighbor> indiNeighbors = indi.getNeighbors();
        indiBag.add(indi.getName());

        if (indiNeighbors != null) {
            for (Neighbor neighbor : indiNeighbors) {
                //if (neighbor.getPropertyName() != null && neighbor.getUriOrLiteral() != null) {
                HashSet<String> propNameAsBagOfWords = splitParagraph(neighbor.getPropertyName());
                HashSet<String> neighborAsBagOfWords = splitParagraph(neighbor.getUriOrLiteral());
                //System.out.println(propNameAsBagOfWords);
                indiBag = concatBags(indiBag, concatBags(propNameAsBagOfWords, neighborAsBagOfWords));
                //}
            }
        }
        return indiBag;
    }

    /**
     * This method is able to compute the sum of the two input vectors.
     *
     * @param vec1
     * @param vec2
     * @return double[] vector
     */
    public static double[] addVectors(double[] vec1, double[] vec2) {
        double[] vec = new double[vec1.length];
        for (int i = 0; i < vec1.length; i++) {
            vec[i] = vec1[i] + vec2[i];
        }
        return vec;
    }

    /**
     * This method is used to normalize the input vector into the range [0,1].
     * To succeed that, it divides the input vector by its norm.
     *
     * @param vec
     * @param denominator
     * @return double[] normalized vector
     */
    public static double[] normalizeVector(double[] vec, int denominator) {
        double[] normalizedVec = new double[vec.length];
        for (int i = 0; i < vec.length; i++) {
            normalizedVec[i] = vec[i] / denominator;
        }
        return normalizedVec;
    }

    /**
     * Main method of data preprocessing.
     *
     * Approach 1: Here is where the computation of the     * mean vector representation of the individuals happens. After this
     * computation, the produced data is stored into the disk and it is
     * retrievable by other applications.
     *
     * Approach 2: Here we compute the bag of words representation and store it
     * to each individual's instance. We can then use this set for keyword
     * search over the individuals (e.g. jacard similarity).
     *
     * TO DO: 1) Split names of URIs properly (e.g. Yiannis+Tzitzikas => Yiannis
     * Tzitzikas). 2) move stop-lists in main scope and change it from final 3)
     * Improve stop-words list, by adding words the are frequently occur in the
     * knowledge base. This, will probably improve the approximation of the
     * individual's semantic (vector) representation. 4) Somehow reduce the
     * noise, in each individual's bag of words representation.
     *
     * @param args
     */
    public static void main(String[] args) {

        try {
            File gModel = new File("C:/Users/Sgo/Desktop/Developer/Vector Models/GoogleNews-vectors-negative300.bin.gz");
            Word2Vec vec = WordVectorSerializer.readWord2VecModel(gModel);
            QAInfoBase KB = new QAInfoBase();

            StringUtils.generateStopLists("src/main/resources/stoplists/stopwordsEn.txt", "src/main/resources/stoplists/stopwordsGr.txt");

            ArrayList<Individual> individuals = KB.getAllIndividuals();
            //KB.printAnswer(individuals);

            HashMap<String, ArrayList<Neighbor>> individualsWithNeighbors = new HashMap<>();
            HashMap<String, HashSet<String>> individualsWithNeighborsAsBagOfWords = new HashMap<>();
            for (Individual ind : individuals) {
                ArrayList<Neighbor> neighborsOfCurntInd = KB.getNeighborsOf(ind.getURI().toString());
                ind.setNeighbors(neighborsOfCurntInd);
                individualsWithNeighbors.put(ind.getURI(), ind.getNeighbors());
                HashSet<String> indAsBagOfWords = getIndividualAsBagOfWords(ind);
                HashSet<String> indAsBagOfIndexedWords = new HashSet<>();
                for (String term : indAsBagOfWords) {
                    if (vec.hasWord(term)) {
                        indAsBagOfIndexedWords.add(term);
                    }
                }
                individualsWithNeighborsAsBagOfWords.put(ind.getURI(), indAsBagOfIndexedWords);
                // We use this bag of words representation for key words matching.
                // Thus, we do not care whether the words are indexed in the word2vec
                // model or not. That is why we add indAsBagOfWords rather
                ind.setBagOfWordsRepresentation(indAsBagOfWords);
            }

            // At this point we have a HashMap with each individual's URI as key and
            // an ArrayList of it's Neighbors as value. Where each neighbor is constructed
            // by the current individual's neighboring node (URI or Literal) and the
            // property (URI) that connects them. We now can split all neighbors at unique
            // terms and get their vector representation. Then we can compute the mean vector
            // and keep it as the vector representation of the individual itself.
            System.out.println(individualsWithNeighborsAsBagOfWords);
            //System.out.println(individuals.get(individuals.size() - 100).getURI());
            //System.out.println(getIndividualAsBagOfWords(individuals.get(individuals.size() - 100)));

            // set the mean vec representation for each individual
            HashMap<String, double[]> indisMeanVecRepresentation = new HashMap<>();
            for (String ind : individualsWithNeighborsAsBagOfWords.keySet()) {
                HashSet<String> terms = individualsWithNeighborsAsBagOfWords.get(ind);
                int cnt = 0;
                double[] meanOfCurrentIndi = new double[300];
                for (String term : terms) {
                    cnt += 1;
                    if (cnt == 1) {
                        meanOfCurrentIndi = vec.getWordVector(term);
                    } else {
                        meanOfCurrentIndi = addVectors(meanOfCurrentIndi, vec.getWordVector(term));
                    }
                }
                meanOfCurrentIndi = normalizeVector(meanOfCurrentIndi, meanOfCurrentIndi.length);

                indisMeanVecRepresentation.put(ind, meanOfCurrentIndi);
            }

            System.out.println(indisMeanVecRepresentation);

            // set the bagOfWords representation for each individual
            for (Individual indi : individuals) {
                indi.setVectorRepresentation(indisMeanVecRepresentation.get(indi.getURI()));
            }

            Utils.saveObject(individuals, "individuals");
            try {
                individuals = (ArrayList<Individual>) Utils.getSavedObject("individuals");
                System.out.println(individuals);
                for (Individual ind : individuals) {
                    System.out.println(ind.getbagOfWordsRepresentation());
                }

            } catch (FileNotFoundException | ClassNotFoundException ex) {
                Logger.getLogger(BagOfWords.class.getName()).log(Level.SEVERE, null, ex);
            }

        } catch (RepositoryException | IOException | MalformedQueryException | QueryEvaluationException ex) {
            Logger.getLogger(BagOfWords.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
