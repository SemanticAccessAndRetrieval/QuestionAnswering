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

import gr.forth.ics.isl.demoExternal.LODsyndesis.LODSyndesisChanel;
import gr.forth.ics.isl.demoExternal.core.AnswerExtraction;
import gr.forth.ics.isl.demoExternal.evaluation.models.ExternalEvalUnit;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Sgo
 */


public class ExternalEvalCollectionManipulator {

    private String evalFileName = "annotated_fb_data_valid";
    private String evalFileNameExtension = ".txt";

    public ExternalEvalCollectionManipulator(String evalFileName, String evalFileNameExtension) {
        this.evalFileName = evalFileName;
        this.evalFileNameExtension = evalFileNameExtension;
    }

    public String mapPredicate(String prd) {
        String[] prdSplited = prd.split("/");
        String prdMapping = "http://rdf.freebase.com/ns/";
        for (int i = 1; i < prdSplited.length; i++) {
            if (i < prdSplited.length - 1) {
                prdMapping += prdSplited[i] + ".";
            } else {
                prdMapping += prdSplited[i];
            }
        }
        return prdMapping;
    }

    public boolean existInLODSyndesis(LODSyndesisChanel chanel, String[] triple) {

        ArrayList<ArrayList<String>> fact;
        fact = chanel.checkFact(triple[0], triple[1] + " " + AnswerExtraction.getSuffixOfURI(triple[2]));

        if (fact.size() == 0) {
            return false;
        } else {
            return true;
        }
    }

    public void produceFilteredEvalCollection() throws FileNotFoundException, IOException {
        LODSyndesisChanel chanel = new LODSyndesisChanel();
        BufferedReader br = new BufferedReader(new FileReader("src/main/resources/external/evaluation/" + evalFileName + evalFileNameExtension));
        BufferedWriter bw = new BufferedWriter(new FileWriter("src/main/resources/external/evaluation/" + evalFileName + "_filtered" + evalFileNameExtension, false));
        try {
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();
            int count = 0;
            while (line != null) {

                String[] triple = line.split("\t");
                String question = triple[3];
                triple[1] = mapPredicate(triple[1]);

                if (existInLODSyndesis(chanel, triple)) {
                    String tripleClean = triple[0] + "\t" + triple[1] + "\t" + triple[2] + "\t" + question + "\n";
                    bw.write(tripleClean);
                    System.out.println(question);
                    System.out.println(++count + ": " + tripleClean);
                }

                line = br.readLine();
            }
        } finally {
            bw.close();
            br.close();
        }
    }

    public void produceFilteredQuestions() throws FileNotFoundException, IOException {

        BufferedReader br = new BufferedReader(new FileReader("src/main/resources/external/evaluation/" + evalFileName + "_filtered" + evalFileNameExtension));
        BufferedWriter bw = new BufferedWriter(new FileWriter("src/main/resources/external/evaluation/" + "questions" + evalFileNameExtension, false));

        int question_id = 1;
        try {
            StringBuilder sb;
            String line = br.readLine();
            int count = 0;
            while (line != null) {

                String[] triple = line.split("\t");
                String question = triple[3];
                sb = new StringBuilder();
                sb.append("q").append(question_id).append("\t").append(question).append("\n");
                bw.write(sb.toString());
                question_id++;
                line = br.readLine();
            }
        } finally {
            bw.close();
            br.close();
        }
    }

    public void produceFilteredAnswers() throws FileNotFoundException, IOException {

        BufferedReader br = new BufferedReader(new FileReader("src/main/resources/external/evaluation/" + evalFileName + "_filtered" + evalFileNameExtension));
        BufferedWriter bw = new BufferedWriter(new FileWriter("src/main/resources/external/evaluation/" + "answers" + evalFileNameExtension, false));

        int question_id = 1;
        try {
            StringBuilder sb;
            String line = br.readLine();
            int count = 0;
            while (line != null) {

                String[] triple = line.split("\t");
                String question = triple[3];
                sb = new StringBuilder();
                sb.append("q").append(question_id).append("\t").append(triple[0]).append("\t").append(triple[1]).append("\t").append(triple[2]).append("\n");
                bw.write(sb.toString());
                question_id++;
                line = br.readLine();
            }
        } finally {
            bw.close();
            br.close();
        }
    }

    public ArrayList<ExternalEvalUnit> readExternalEvalCollection() {
        try {
            BufferedReader br = null;
            ArrayList<ExternalEvalUnit> evalUnits = new ArrayList<>();
            br = new BufferedReader(new FileReader("src/main/resources/external/evaluation/" + evalFileName + "_filtered" + evalFileNameExtension));

            String line = br.readLine();
            int count = 0;
            while (line != null) {
                String[] tripleAndQuestion = line.split("\t");
                ExternalEvalUnit evalUnit = new ExternalEvalUnit(tripleAndQuestion);
                evalUnits.add(evalUnit);
                line = br.readLine();
            }
            return evalUnits;
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ExternalEvalCollectionManipulator.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ExternalEvalCollectionManipulator.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        ExternalEvalCollectionManipulator cm = new ExternalEvalCollectionManipulator("annotated_fb_data_valid", ".txt");
        //cm.produceFilteredEvalCollection();
        ArrayList<ExternalEvalUnit> evalUnits = cm.readExternalEvalCollection();
        System.out.println(evalUnits);
        System.out.println(evalUnits.size());
    }

}
