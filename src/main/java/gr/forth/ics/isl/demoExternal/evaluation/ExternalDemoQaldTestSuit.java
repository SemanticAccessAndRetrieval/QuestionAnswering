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
import gr.forth.ics.isl.demoExternal.evaluation.models.QaldEvalCollection;
import gr.forth.ics.isl.demoExternal.main.ExternalKnowledgeDemoMain;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import org.codehaus.jettison.json.JSONObject;

/**
 *
 * @author Sgo
 */
public class ExternalDemoQaldTestSuit {

    private static final String evalFileName = "qald-7-test-en-wikidata";
    private static final String evalFileNameExtension = ".txt";
    private static final String filePath = "src/main/resources/external/evaluation/";

    public static void writeStringToFile(String fileName, String str, String sourceFileExtension, String targetFileExtension) throws IOException {
        fileName = fileName.replace(sourceFileExtension, sourceFileExtension);


        File file = new File(filePath + fileName + targetFileExtension);
        BufferedWriter writer;

        if (file.exists()) {
            writer = new BufferedWriter(new FileWriter(filePath + fileName + targetFileExtension, true));
        } else {
            writer = new BufferedWriter(new FileWriter(filePath + fileName + targetFileExtension));
        }

        writer.write(str + "\n");
        writer.close();
    }

    public static String validateAnswer(String strAns, ArrayList<String> systemAns, ArrayList<String> goldAns) {

        for (String equivalentURI : systemAns) {
            equivalentURI = equivalentURI.substring(1, equivalentURI.length() - 1);
            if (goldAns.contains(equivalentURI)) {
                strAns = equivalentURI;
                break;
            }
        }

        return strAns;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException, FileNotFoundException {

        QaldEvalCollection qec = new QaldEvalCollection(evalFileName, evalFileNameExtension);
        LODSyndesisChanel lod = new LODSyndesisChanel();

        ExternalKnowledgeDemoMain.initializeToolsAndResources("WNHOME");
        int cnt = 0;
        String answerFileName = evalFileName + "_answers";

        for (int i = 0; i < qec.getEvalCollection().size(); i++) {
            System.out.println(i + 1);
            try {
                JSONObject answer = ExternalKnowledgeDemoMain.getAnswerAsJson(qec.getEvalUnit(i).getQuestion());
                String stringAnswer = answer.getString("answer");
                ArrayList<String> equivalentURIs = lod.getEquivalentEntity(stringAnswer);

                stringAnswer = validateAnswer(stringAnswer, equivalentURIs, qec.getEvalUnit(i).getAnswers());

                // question_id, question_text, systems_answer, gold_answer
                String cntAnswer = qec.getEvalUnit(i).getId() + "\t" + qec.getEvalUnit(i).getQuestion() + "\t" + stringAnswer + "\t" + qec.getEvalUnit(i).getAnswers();
                System.out.println(cntAnswer);

                writeStringToFile(answerFileName, cntAnswer, evalFileNameExtension, ".txt");

                if (stringAnswer.equals("No answer found!")) {
                    cnt++;
                }

            } catch (Exception e) {
                System.out.println(e);
                String cntAnswer = qec.getEvalUnit(i).getId() + "\t" + qec.getEvalUnit(i).getQuestion() + "\t" + "No answer found!" + "\t" + qec.getEvalUnit(i).getAnswers();
                writeStringToFile(answerFileName, cntAnswer, evalFileNameExtension, ".txt");
                System.out.println(qec.getEvalUnit(i).getId() + "\t" + qec.getEvalUnit(i).getQuestion() + "\t" + "No answer found!" + "\t" + qec.getEvalUnit(i).getAnswers());
            } finally {

            }
        }

        System.out.println(cnt);
    }

}
