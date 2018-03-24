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

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Lefteris Dimitrakis
 */
public class Comment implements Serializable {

    private String hotel_id;
    private String hotel_name;
    private String id;
    private String text;
    private String best_sentence;
    private Date date;
    private double score;
    //private static SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
    private static SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

    public Comment(String hotel_name, String hotel_id, String id, String text, String date) {
        this.hotel_name = hotel_name;
        this.hotel_id = hotel_id;
        this.id = id;
        this.text = text;
        try {
            this.date = sdf.parse(date);
        } catch (ParseException ex) {
            Logger.getLogger(Comment.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.score = 0;
        this.best_sentence = "";
    }

    public String getHotelName() {
        return this.hotel_name;
    }

    public String getHotelId() {
        return this.hotel_id;
    }

    public String getId() {
        return this.id;
    }

    public String getText() {
        return this.text;
    }

    public String getBestSentence() {
        return this.best_sentence;
    }

    public Date getDate() {
        return this.date;
    }

    public double getScore() {
        return this.score;
    }

    public void setHotelId(String hotel_id) {
        this.hotel_id = hotel_id;
    }

    public void setHotelName(String hotel_name) {
        this.hotel_name = hotel_name;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setBestSentence(String sentence) {
        this.best_sentence = sentence;
    }

    public void setDate(String date) {
        try {
            this.date = sdf.parse(date);
        } catch (ParseException ex) {
            Logger.getLogger(Comment.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void setScore(double score) {
        this.score = score;
    }

    @Override
    public String toString() {
        return "Comment for hotel: " + this.hotel_id + " with id: " + this.id + " says: " + this.text + " posted at: " + this.date + " with score: " + this.score;
    }

}
