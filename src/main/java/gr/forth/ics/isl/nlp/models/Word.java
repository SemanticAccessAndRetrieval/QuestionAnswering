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

/**
 *
 * @author Lefteris Dimitrakis
 */
public class Word {
    private String text;
    private String pos;
    private String ner;
    private int sentence_id;
    
    public Word(String text, String pos, String ner, int sentence_id){
        this.text = text;
        this.pos = pos;
        this.ner = ner;
        this.sentence_id = sentence_id;
    }
    
    public void setText(String text){
        this.text = text;
    }
    
    public void setPos(String pos){
        this.pos = pos;
    }
    
    public void setNer(String ner){
        this.ner = ner;
    }
    
    public void setSentenceId(int sentence_id){
        this.sentence_id = sentence_id;
    }
    
    public String getText(){
        return this.text;
    }
    
    public String getPos(){
        return this.pos;
    }
    
    public String getNer(){
        return this.ner;
    }
    
    public int getSentenceId(){
        return this.sentence_id;
    }
    
    /*
    @Override
    public String toString(){
        return "Word: " + this.text + " Pos: " + this.pos + " Ner: " + this.ner + " SentenceId: " + this.sentence_id;
    }
    */
    
    @Override
    public String toString(){
        return this.text + " " + this.ner + " " + this.pos + " " + this.sentence_id;
    }
}
