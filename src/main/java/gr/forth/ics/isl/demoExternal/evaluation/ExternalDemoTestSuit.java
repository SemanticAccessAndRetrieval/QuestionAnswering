/*
 *  This code belongs to the Semantic Access and Retrieval (SAR) group of the
 *  Information Systems Laboratory (ISL) of the
 *  Institute of Computer Science (ICS) of the
 *  Foundation for Research and Technology - Hellas (FORTH)
 *  Nobody is allowed to use, copy, distribute, or modify this work.
 *  It is published for reasons of research results reproducibility.
 *  (c) 2017 Semantic Access and Retrieval group, All rights reserved
 */
package gr.forth.ics.isl.demoExternal.evaluation;

import gr.forth.ics.isl.demoExternal.evaluation.models.ExternalEvalUnit;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Sgo
 */


public class ExternalDemoTestSuit {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        ExternalEvalCollectionManipulator cm = new ExternalEvalCollectionManipulator("annotated_fb_data_valid", ".txt");
        try {

            cm.produceFilteredQuestions();
            cm.produceFilteredAnswers();

        } catch (IOException ex) {
            Logger.getLogger(ExternalDemoTestSuit.class.getName()).log(Level.SEVERE, null, ex);
        }

        ArrayList<ExternalEvalUnit> evalUnits = cm.readExternalEvalCollection();
        for (ExternalEvalUnit evalUnit : evalUnits) {
            //TODO: Evaluate model
        }
    }

}
