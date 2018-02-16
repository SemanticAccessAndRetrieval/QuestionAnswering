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

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import gr.forth.ics.isl.utilities.StringUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class Question {

    private String question; //The text of the question
    private String type_of_question; //--Probably unused--
    private String entity; //The entity type of the answer e.g. LOCATION
    private String answer; //The ground truth answer
    private String system_answer; //The answer of our system

    //An ArrayList that contains all the related sentences to the question
    //private HashMap<String, ArrayList<Word>> related_sentences;
    private RelatedSentences related_sentences;
    
    /**
     * @return the question
     */
    public String getQuestion() {
        return question;
    }

    /**
     * @param question the question to set
     */
    public void setQuestion(String question) {
        this.question = question;
    }

    /**
     * @return the type_of_question
     */
    public String getType_of_question() {
        return type_of_question;
    }

    /**
     * @param type_of_question the type_of_question to set
     */
    public void setType_of_question(String type_of_question) {
        this.type_of_question = type_of_question;
    }

    /**
     * @return the entity
     */
    public String getEntity() {
        return entity;
    }

    /**
     * @param expected_entity the entity to set
     */
    public void setEntity(String expected_entity) {
        this.entity = expected_entity;
    }

    /**
     * @return the system_answer
     */
    public String getSystem_answer() {
        calculateAnswer();
        return system_answer;
    }

    /**
     * @param system_answer the system_answer to set
     */
    public void setSystem_answer(String system_answer) {
        this.system_answer = system_answer;
    }

    /**
     * @return the answer
     */
    public String getAnswer() {
        return answer;
    }

    /**
     * @param answer the answer to set
     */
    public void setAnswer(String answer) {
        this.answer = answer;
    }

    /*
    public void setRelatedSenteces(HashMap<String, ArrayList<Word>> sentences) {
        this.related_sentences = sentences;
    }

    public HashMap<String, ArrayList<Word>> getRelatedSentences() {
        return this.related_sentences;
    }
    
    public void calculateAnswer() {
        if (this.related_sentences != null) {

            if (this.related_sentences.values().size() == 1) {
                
               for(ArrayList<Word> word: this.related_sentences.values()){
                   this.setSystem_answer(word.get(0).getText());
                   return;
               }     
                //return this.related_sentences.get(0).get(0).getText();
            }
        }
       // return "";

    }
    
    */
    
    public void setRelatedSenteces(RelatedSentences sentences) {
        this.related_sentences = sentences;
    }

    public RelatedSentences getRelatedSentences() {
        return this.related_sentences;
    }
    
    public void calculateAnswer() {
        this.setSystem_answer(this.related_sentences.calculateAnswer(question));
    }

    //Function to check if the given answer
    //and the system-answer matches
    public boolean isSystemAnswerCorrect() {
        if (this.answer.equals(this.system_answer)) {
            return true;
        }
        return false;
    }

    

    //This function is used to prepare a given text before applying Jaccard Similarity
    //The preparation contains: tokenization, lemmatization, remove stopwords etc
    public String[] prepareText(String text) {

        Properties props = new Properties();

        props.put("annotators", "tokenize, ssplit, pos, lemma, ner");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

        //apply
        Annotation document = new Annotation(text);
        pipeline.annotate(document);

        List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);

        ArrayList<String> result = new ArrayList<>();

        for (CoreMap sentence : sentences) {

            for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                // this is the text of the token
                String word = token.get(CoreAnnotations.LemmaAnnotation.class);
                // this is the POS tag of the token
                String pos = token.get(PartOfSpeechAnnotation.class);
                // this is the NER label of the token
                String ne = token.get(CoreAnnotations.NamedEntityTagAnnotation.class);

                if (!StringUtils.isStopWord(word)) {
                result.add(word);
                }

            }

        }
        String[] result_ar = new String[result.size()];

        return result.toArray(result_ar);
    }

}
