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

import static gr.forth.ics.isl.nlp.EntityIdentifier.jacardSim;
import gr.forth.ics.isl.nlp.NlpAnalyzer;
import gr.forth.ics.isl.nlp.models.Question;
import gr.forth.ics.isl.nlp.models.Word;
import gr.forth.ics.isl.sailInfoBase.models.Individual;
import gr.forth.ics.isl.utilities.StringUtils;
import gr.forth.ics.isl.utilities.Utils;
import gr.forth.ics.isl.utilities.index.SearchFiles;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import javax.swing.JOptionPane;
import mitos.stemmer.trie.Trie;
import org.apache.lucene.queryparser.classic.ParseException;

/**
 *
 * @author Lefteris Dimitrakis
 */
public class QA_main {

//The paths for the stopWords Files.
    public static String filePath_en = "src/main/resources/stoplists/stopwordsEn.txt";
    public static String filePath_gr = "src/main/resources/stoplists/stopwordsGr.txt";

    //A hashMap that contains two Trie, one for the English stopList and one for the Greek
    public static HashMap<String, Trie> stopLists = new HashMap<>();

    public static void main(String[] args) throws IOException, ParseException, FileNotFoundException, ClassNotFoundException {

        //Create the list of stopWords to use
        StringUtils.generateStopLists(filePath_en, filePath_gr);

        // Initiliaze Word2Vec google pre-trained model
        //File gModel = new File("C:/Users/Sgo/Desktop/Developer/Vector Models/GoogleNews-vectors-negative300.bin.gz");
        //Word2Vec vec = WordVectorSerializer.readWord2VecModel(gModel);
        // Retrieve all instances of the KB
        ArrayList<Individual> individuals = (ArrayList<Individual>) Utils.getSavedObject("individuals");

        while (true) {
            /*Get the user's question*/
            //Get the user's question
            String question = JOptionPane.showInputDialog("Submit your question", "");
            //Get the answer type of the question
            String question_type = JOptionPane.showInputDialog("Submit the answer's entity type", "");

            //Get in which mode we want to apply the analysis
            //I.e. Using information from Documents or from a KB
            String mode = JOptionPane.showInputDialog("Select Analysis Mode (KB or Documents)", "Documents");

            //Create a Question instance for the submitted question
            Question quest = new Question();
            quest.setQuestion(question);
            quest.setEntity(question_type);
            quest.setAnswer(" ");

            // If we want to analyze the documents
            if (mode.equalsIgnoreCase("documents")) {

                // Get the most relevant document using lucene
                String document_text = SearchFiles.getTopKhits(question, 1);
                if (document_text == null) {
                    System.out.println("No matching documents!");
                    return;
                }

                // Get the best paragraph by applying JaccardSimilarity with the question
                String paragraph = StringUtils.getBestParagraph(document_text, question, "jaccard");
                //String paragraph = StringUtils.getBestParagraph(document_text, question,"levenshtein");

                //Get the unique sentences of the paragraph
                //Essentially split the provided paragraph into sentences
                ArrayList<String> sentences = NlpAnalyzer.getSentences(paragraph);

                //Get the Ner and Pos, for each Entity in the paragraph
                ArrayList<Word> entities = NlpAnalyzer.getWordsWithPosNer(paragraph);

                System.out.println("-----PARAGRAPH-----");
                System.out.println(paragraph);
                System.out.println();

                quest.setRelatedSenteces(NlpAnalyzer.getRelatedSentencesAndWords(question_type, entities, sentences));

                System.out.println("-----QUESTION-----");
                System.out.println("Text: " + quest.getQuestion());
                System.out.println("AnswerType: " + question_type);
                System.out.println("Answer: " + quest.getAnswer());
                System.out.println("SystemAnswer: " + quest.getSystem_answer());

                //System.out.println();
                //System.out.println("Related Sentences: " + NlpAnalyzer.getRelatedSentencesAndWords(question_type, entities, sentences));
                //System.out.println();
            } // If we want to analyze the Knowledge Base
            else if (mode.equalsIgnoreCase("kb")) {
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

                System.out.println(Utils.sortByValue(rankedIndividuals));
            }
        }
    }
}
