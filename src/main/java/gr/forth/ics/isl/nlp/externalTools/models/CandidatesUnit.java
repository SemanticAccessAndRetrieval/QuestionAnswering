/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.forth.ics.isl.nlp.externalTools.models;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
/**
 *
 * @author Sgo
 */
@Getter
@Setter
public class CandidatesUnit {

    private String text;
    private List<SurfaceForm> surfaceForm;

    public CandidatesUnit() {
        this.text = new String();
        this.surfaceForm = new ArrayList<SurfaceForm>();
    }

    public CandidatesUnit(String text) {
        this.text = text;
        this.surfaceForm = new ArrayList<SurfaceForm>();
    }

    public void addSurfaceForm(SurfaceForm sf) {
        this.surfaceForm.add(sf);
    }

    @Override
    public String toString() {
        return "Text: " + this.text + "\n"
                + "Surface Form: " + this.surfaceForm;
    }
}
