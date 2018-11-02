/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.forth.ics.isl.nlp.externalTools.models;

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
public class SurfaceForm {

    private String name;
    private int offset;
    private List<ResourceCandidate> resources;

    public SurfaceForm(String name, int offset) {
        this.name = name.replace("\"", "");
        this.offset = offset;
        this.resources = new ArrayList<ResourceCandidate>();
    }

    public void addResource(ResourceCandidate resource) {
        this.resources.add(resource);
    }

    @Override
    public String toString() {
        return "Name: " + this.name + ", "
                + "Offset: " + this.offset + ", "
                + "Candidate Resources: " + resources;
    }
}
