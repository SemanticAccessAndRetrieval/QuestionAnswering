/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.forth.ics.isl.nlp.externalTools.models;

import static gr.forth.ics.isl.nlp.externalTools.models.Constants.COMMA;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 *
 * @author Sgo
 */
@Getter
@Setter
@NoArgsConstructor
public class ResourceCandidate {
    private String uri;
    private String label;
    private String contextualScore;
    private String percentageOfSecondRank;
    private String support;
    private String priorScore;
    private String finalScore;
    private String types;

    public ResourceCandidate(String uri, String label, String contextualScore, String percentageOfSecondRank,
            String support, String priorScore, String finalScore, String types) {
        this.uri = uri.replace("\"", "");
        this.label = label.replace("\"", "");
        this.contextualScore = contextualScore.replace("\"", "");
        this.percentageOfSecondRank = percentageOfSecondRank.replace("\"", "");
        this.support = support.replace("\"", "");
        this.priorScore = priorScore.replace("\"", "");
        this.finalScore = finalScore.replace("\"", "");
        this.types = types.replace("\"", "");
    }

    public List<String> typesList() {

        ArrayList<String> typesAsList = new ArrayList<>();
        if (types != null && !types.isEmpty()) {
            String[] typesAsArray = types.split(COMMA);
            for (String type : typesAsArray) {
                typesAsList.add(type.trim());
            }
        }

        return typesAsList;
    }

    @Override
    public String toString() {
        return "URI: " + this.uri + ", Categories: " + this.types;
    }

}
