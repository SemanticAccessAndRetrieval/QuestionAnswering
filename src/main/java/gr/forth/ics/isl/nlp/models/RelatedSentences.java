/* 
 *  This code belongs to the Semantic Access and Retrieval (SAR) group of the 
 *  Information Systems Laboratory (ISL) of the 
 *  Institute of Computer Science (ICS) of the  
 *  Foundation for Research and Technology - Hellas (FORTH)
 *  Nobody is allowed to use, copy, distribute, or modify this work.
 *  It is published for reasons of research results reproducibility.
 *  (c) 2017 Semantic Access and Retrieval group, All rights reserved
 */









package gr.forth.ics.isl.nlp.models;


import gr.forth.ics.isl.utilities.StringUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

/**
 *
 * @author Lefteris Dimitrakis
 */
public class RelatedSentences {

    private HashMap<String, ArrayList<Word>> related_sentences;

    public RelatedSentences(HashMap<String, ArrayList<Word>> related_sentences) {
        this.related_sentences = related_sentences;
    }

    public HashMap<String, ArrayList<Word>> getRelatedSentences() {
        return this.related_sentences;
    }

    public void setRelatedSentences(HashMap<String, ArrayList<Word>> related_sentences) {
        this.related_sentences = related_sentences;
    }

    //ELEGXOUS GIA NULL NA PROS8ESW
    public ArrayList<Word> getRelatedEntities() {
        ArrayList<Word> related_entities = new ArrayList<>();
        
        this.related_sentences.values().forEach((words) -> {
            words.forEach((word) -> {
                related_entities.add(word);
            });
        });
        
        /*
        for(ArrayList<Word> words: this.related_sentences.values()){
            for(Word word : words){
                related_entities.add(word);
            }
        }
         */
        return related_entities;
    }

    public String getBestSentence(String question) {
        double bestJaccard = -1.0;
        double tmpJaccard;
        String bestSentence = "";

        String[] questionTerms = (String[]) StringUtils.splitString(question).toArray(new String[0]);
        String[] sentenceTerms;

        for (String sentence : related_sentences.keySet()) {
            sentenceTerms = (String[]) StringUtils.splitString(sentence).toArray(new String[0]);
            tmpJaccard = StringUtils.JaccardSim(sentenceTerms, questionTerms);

            if (tmpJaccard > bestJaccard) {
                bestJaccard = tmpJaccard;
                bestSentence = sentence;
            }
        }

        return bestSentence;

    }

    public String getFinalAnswer(String question) {
        return this.calculateAnswer(question);
    }

    public String calculateAnswer(String question) {
        if (this.related_sentences == null) {
            return "";
        }

        // IF there is only one related sentence, then return as answer the first related Word not contained in the question
        if (this.related_sentences.values().size() == 1) {
            for (ArrayList<Word> words : this.related_sentences.values()) {
                for (Word word : words) {
                    if (!question.contains(word.getText())) {
                        return word.getText();
                    }
                }
            }
        //IF there are more than one related sentences, calculate the best answer and then return as answer the first related word not contained in the question
        } else {
            String best_sentence = getBestSentence(question);
            System.out.println("Best Sentence: " + best_sentence);
            ArrayList<Word> words = this.related_sentences.get(best_sentence);
            if (words != null) {
                for (Word word : words) {
                    if (!question.contains(word.getText())) {
                        return word.getText();
                    }
                }

            } else {
                return "";
            }
        }

        return "";
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();

        String sentence;

        ArrayList<Word> words;

        int id = 1;
        for (Entry<String, ArrayList<Word>> entry : this.related_sentences.entrySet()) {
            sentence = entry.getKey();
            words = entry.getValue();

            str.append("\n").append("Sentence").append(id).append(": ").append(sentence).append("\n");
            str.append(words).append("\n");

            id++;
        }

        return str.toString();
    }

}
