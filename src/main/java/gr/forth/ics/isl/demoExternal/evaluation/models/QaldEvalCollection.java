/*
 *  This code belongs to the Semantic Access and Retrieval (SAR) group of the
 *  Information Systems Laboratory (ISL) of the
 *  Institute of Computer Science (ICS) of the
 *  Foundation for Research and Technology - Hellas (FORTH)
 *  Nobody is allowed to use, copy, distribute, or modify this work.
 *  It is published for reasons of research results reproducibility.
 *  (c) 2017 Semantic Access and Retrieval group, All rights reserved
 */
package gr.forth.ics.isl.demoExternal.evaluation.models;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Sgo
 */


public class QaldEvalCollection {

    private String fileName = "";
    private String fileExtention = "";
    private ArrayList<QaldEvalUnit> evalCollection;

    public QaldEvalCollection(String fileName, String fileExtention) {
        this.fileName = fileName;
        this.fileExtention = fileExtention;
        this.evalCollection = new ArrayList<>();
        initCollection(fileName, fileExtention);
    }

    public ArrayList<String> getAnswers(String answers) {
        ArrayList<String> answersList = new ArrayList<>();
        answers = answers.substring(1, answers.length() - 1);
        String[] answersArray = answers.split(",");

        for (String ans : answersArray) {
            answersList.add(ans.trim());
        }

        //System.out.println(answersList);
        return answersList;
    }
    public void initCollection(String fileName, String fileExtention) {

        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader("src/main/resources/external/evaluation/questionAnsPairs/" + fileName + fileExtention));
            try {
                String line = br.readLine();
                while (line != null) {
                    //System.out.println(line);
                    String[] lineSp = line.split("\t");
                    QaldEvalUnit qeu = new QaldEvalUnit(Integer.valueOf(lineSp[0]), lineSp[4]);
                    qeu.setAnswerType(lineSp[5]);
                    qeu.setAnswers(getAnswers(lineSp[6]));
                    this.evalCollection.add(qeu);

                    //System.out.println(qeu);
                    line = br.readLine();
                }
            } catch (IOException ex) {
                Logger.getLogger(QaldEvalCollection.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                try {
                    br.close();
                } catch (IOException ex) {
                    Logger.getLogger(QaldEvalCollection.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(QaldEvalCollection.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                br.close();
            } catch (IOException ex) {
                Logger.getLogger(QaldEvalCollection.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public QaldEvalUnit getEvalUnit(int i) {
        return this.evalCollection.get(i);
    }

    public ArrayList<QaldEvalUnit> getEvalCollection() {
        return this.evalCollection;
    }
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        QaldEvalCollection qec = new QaldEvalCollection("qald-7-train-en-wikidata", ".txt");

    }

}
