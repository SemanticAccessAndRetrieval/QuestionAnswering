/* 
 *  This code belongs to the Semantic Access and Retrieval (SAR) group of the 
 *  Information Systems Laboratory (ISL) of the 
 *  Institute of Computer Science (ICS) of the  
 *  Foundation for Research and Technology - Hellas (FORTH)
 *  Nobody is allowed to use, copy, distribute, or modify this work.
 *  It is published for reasons of research results reproducibility.
 *  (c) 2017 Semantic Access and Retrieval group, All rights reserved
 */









package gr.forth.ics.isl.utilities;

import edu.stanford.nlp.util.Sets;
import static gr.forth.ics.isl.main.QA_main.stopLists;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.stream.Stream;
import mitos.stemmer.Stemmer;
import mitos.stemmer.trie.Trie;

/**
 * This class contains utility functions for Strings
 */
public class StringUtils {

    //The paths for the stopWords Files.
    public static String filePath_en = "src/main/resources/stoplists/stopwordsEn.txt";
    public static String filePath_gr = "src/main/resources/stoplists/stopwordsGr.txt";

    /**
     * This function is responsible to take as input an ArrayList of words and
     * return a new ArrayList with all the words that are not stopWords
     *
     * @param words. ArrayList with words
     * @return
     */
    public static ArrayList<String> removeStopWords(ArrayList<String> words) {
        ArrayList<String> cleanWords = new ArrayList<>();
        int notExist;
        for (String word : words) {
            notExist = 0;
            for (Map.Entry<String, Trie> entry : stopLists.entrySet()) {
                //If the word isn't a stopWord and it is not the empty String
                if (!entry.getValue().FindSubstring(word, true).equals(word) && !word.trim().isEmpty()) {
                    notExist++;
                }
            }
            if (notExist == stopLists.size()) {
                cleanWords.add(word);
            }
        }
        return cleanWords;
    }

    /**
     * This function is responsible to take as input a HashSet of words and
     * return a new HashSet with all the words that are not stopWords
     *
     * @param words. HashSet with words
     * @return
     */
    public static HashSet<String> removeStopWords(HashSet<String> words) {
        HashSet<String> cleanWords = new HashSet<>();
        int notExist;
        for (String word : words) {
            notExist = 0;
            for (Map.Entry<String, Trie> entry : stopLists.entrySet()) {
                //If the word isn't a stopWord and it is not the empty String
                if (!entry.getValue().FindSubstring(word, true).equals(word) && !word.trim().isEmpty()) {
                    notExist++;
                }
            }
            if (notExist == stopLists.size()) {
                cleanWords.add(word);
            }
        }
        return cleanWords;
    }

    /**
     * This function takes as input a string and return as value true if the
     * string is stopword, false otherwise
     *
     * @param word
     * @return
     */
    public static boolean isStopWord(String word) {

        if (word.trim().isEmpty()) {
            return true;
        }

        for (Map.Entry<String, Trie> entry : stopLists.entrySet()) {
            if (entry.getValue().FindSubstring(word, true).equals(word)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Split input string in individual words. Also it transform them to
     * lowerCase
     *
     * @param string
     * @return
     */
    public static ArrayList<String> splitString(String string) {
        String delimiter = "\t\n\r\f ";
        StringTokenizer tokenizer = new StringTokenizer(string, delimiter);

        ArrayList<String> words = new ArrayList<>();

        while (tokenizer.hasMoreTokens()) {
            String currentToken = tokenizer.nextToken();
            if (!currentToken.trim().isEmpty()) {
                words.add(currentToken.toLowerCase().trim());
            }
        }

        return words;
    }

    /**
     * Remove all punctuation from each string in the input ArrayList.
     *
     * @param words
     * @return
     */
    public static ArrayList<String> removePanctuantions(ArrayList<String> words) {
        ArrayList<String> cleanWords = new ArrayList<>();
        String tmpWord;

        for (String word : words) {
            //Replace all panctuations with the blank String
            tmpWord = word.replaceAll("\\p{P}", "");
            //Add the cleaned string in the ArrayList
            cleanWords.add(tmpWord);
        }
        return cleanWords;
    }

    /**
     * Apply stemming in each word in the input ArrayList.
     *
     * @param words
     * @return
     */
    public static ArrayList<String> applyStemming(ArrayList<String> words) {

        //Initialize Mitos Stemmer
        Stemmer.Initialize();

        ArrayList<String> stemmedWords = new ArrayList<>();

        for (String word : words) {
            //add the stemmed version of the word
            stemmedWords.add(Stemmer.Stem(word));
        }

        return stemmedWords;
    }

    /**
     * This function is responsible to clean the ArrayList of strings, it
     * removes panctuations as well as it applies stemming
     *
     * @param words
     * @return
     */
    public static ArrayList<String> cleanString(ArrayList<String> words) {
        words = removePanctuantions(words);
        words = applyStemming(words);

        return words;
    }

    /**
     * Generates a HashMap with languages as keys and stopLists as values.
     *
     * @param filePath_en
     * @param filePath_gr
     */
    public static void generateStopLists(String filePath_en, String filePath_gr) {
        Trie stopWordsEn = new Trie();
        Trie stopWordsGr = new Trie();
        try (Stream<String> stream = Files.lines(Paths.get(filePath_en))) {
            stream.forEach(word -> stopWordsEn.Insert(word, false));
        } catch (IOException ex) {
            //Logger.getLogger(main.class.getName()).log(Level.SEVERE, null, ex);
        }
        stopLists.put("en", stopWordsEn);
        try (Stream<String> stream = Files.lines(Paths.get(filePath_gr))) {
            stream.forEach(word -> stopWordsGr.Insert(word, false));
        } catch (IOException ex) {
            //Logger.getLogger(main.class.getName()).log(Level.SEVERE, null, ex);
        }
        stopLists.put("gr", stopWordsGr);
    }

    /**
     * Generates a HashMap with languages as keys and stopLists as values.
     *
     * @param filePath_en
     * @param filePath_gr
     */
    public static void generateStopListsFromExternalSource(String filePath_en, String filePath_gr) {
        Trie stopWordsEn = new Trie();
        Trie stopWordsGr = new Trie();

        ClassLoader classLoader = StringUtils.class.getClassLoader();
        InputStream is = classLoader.getResourceAsStream(filePath_en);

        try (Stream<String> stream = new BufferedReader(new InputStreamReader(is, "UTF-8")).lines()) {
            stream.forEach(word -> stopWordsEn.Insert(word, false));
        } catch (IOException ex) {
            //Logger.getLogger(main.class.getName()).log(Level.SEVERE, null, ex);
        }
        stopLists.put("en", stopWordsEn);

        is = classLoader.getResourceAsStream(filePath_gr);

        try (Stream<String> stream = new BufferedReader(new InputStreamReader(is, "UTF-8")).lines()) {
            stream.forEach(word -> stopWordsGr.Insert(word, false));
        } catch (IOException ex) {
            //Logger.getLogger(main.class.getName()).log(Level.SEVERE, null, ex);
        }
        stopLists.put("gr", stopWordsGr);
    }

    //Function which calculates the Jaccard Similarity between to Arrays of Strings
    public static double JaccardSim(String[] sentenceTerms, String[] quesTerms) {
        Set<String> sentenceTermsAsSet = new HashSet<String>(Arrays.asList(sentenceTerms));
        Set<String> quesTermsAsSet = new HashSet<String>(Arrays.asList(quesTerms));
        Set<String> union = Sets.union(sentenceTermsAsSet, quesTermsAsSet);
        Set<String> intersection = Sets.intersection(sentenceTermsAsSet, quesTermsAsSet);
        double jaccardSim = ((double) intersection.size()) / union.size();
        return jaccardSim;
    }

    //Function which calculates the LevenshteinDistance between two strings 
    public static int LevenshteinDistance(String a, String b) {
        a = a.toLowerCase();
        b = b.toLowerCase();
        // i == 0
        int[] costs = new int[b.length() + 1];
        for (int j = 0; j < costs.length; j++) {
            costs[j] = j;
        }
        for (int i = 1; i <= a.length(); i++) {
            // j == 0; nw = lev(i - 1, j)
            costs[0] = i;
            int nw = i - 1;
            for (int j = 1; j <= b.length(); j++) {
                int cj = Math.min(1 + Math.min(costs[j], costs[j - 1]), a.charAt(i - 1) == b.charAt(j - 1) ? nw : nw + 1);
                nw = costs[j];
                costs[j] = cj;
            }
        }
        return costs[b.length()];
    }

    public static String getBestParagraph(String document_text, String question, String similarity_measure) {

        if (similarity_measure.equalsIgnoreCase("jaccard")) {
            double bestJaccard = -1.0;
            double tmpJaccard;
            String bestParagraph = "";

            String[] paragraphs = document_text.split("\n\n");
            System.out.println(paragraphs.length);

            //Split String and then remove stopWords
            String[] questionTerms = (String[]) StringUtils.removeStopWords(StringUtils.splitString(question.trim())).toArray(new String[0]);
            
            String[] paragraphTerms;

            for (String paragraph : paragraphs) {
                paragraphTerms = (String[]) StringUtils.removeStopWords(StringUtils.splitString(paragraph.trim())).toArray(new String[0]);
                tmpJaccard = StringUtils.JaccardSim(paragraphTerms, questionTerms);
                
                if (tmpJaccard >= bestJaccard) {
                    bestJaccard = tmpJaccard;
                    bestParagraph = paragraph;
                }
            }

            return bestParagraph;

        }
        else if (similarity_measure.equalsIgnoreCase("levenshtein")) {
            int minLeven = Integer.MAX_VALUE;
            int tmpLeven;
            String bestParagraph = "";

            String[] paragraphs = document_text.split("\n\n");
            System.out.println(paragraphs.length);
            
            for (String paragraph : paragraphs) {
               
                tmpLeven = StringUtils.LevenshteinDistance(paragraph, question);

                if (tmpLeven <= minLeven) {
                    minLeven = tmpLeven;
                    bestParagraph = paragraph;
                }
            }
            return bestParagraph;
        }
        return "";
    }

}
