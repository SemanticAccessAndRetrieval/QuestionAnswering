/*
 *  This code belongs to the Semantic Access and Retrieval (SAR) group of the 
 *  Information Systems Laboratory (ISL) of the 
 *  Institute of Computer Science (ICS) of the  
 *  Foundation for Research and Technology - Hellas (FORTH)
 *  Nobody is allowed to use, copy, distribute, or modify this work.
 *  It is published for reasons of research results reproducibility.
 *  (c) 2017 Semantic Access and Retrieval group, All rights reserved
 */
package gr.forth.ics.isl.demo.models;

import gr.forth.ics.isl.nlp.models.Comment;
import java.util.ArrayList;
import java.util.Collections;

/**
 *
 * @author Lefteris Dimitrakis
 */
public abstract class Model {

    private String description;
    private ArrayList<Comment> comments;

    public void setComments(ArrayList<Comment> comms) {
        this.comments = comms;
    }

    public ArrayList<Comment> getComments() {
        return this.comments;
    }

    public void setDescription(String desc) {
        this.description = desc;
    }

    public String getDescription() {
        return this.description;
    }

    public ArrayList<Comment> getTopComments(int topK) {
        ArrayList<Comment> topComments;

        // Sort comments by score (in decreasing order)
        Collections.sort(this.comments, (Comment c1, Comment c2) -> -Double.compare(c1.getScore(), c2.getScore()));

        // Get the top Comments
        if (topK <= this.comments.size()) {
            topComments = new ArrayList<>(this.comments.subList(0, topK));
        } else {
            topComments = this.comments;
        }

        return topComments;
    }

    public abstract void scoreComments(String query);

    public abstract void scoreComment(Comment com, String query);
}
