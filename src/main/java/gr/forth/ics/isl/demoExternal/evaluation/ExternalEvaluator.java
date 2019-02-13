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

import gr.forth.ics.isl.demoExternal.core.AnswerExtraction;
import gr.forth.ics.isl.demoExternal.core.EntitiesDetection;
import gr.forth.ics.isl.demoExternal.core.ModulesErrorHandling;
import gr.forth.ics.isl.demoExternal.core.QuestionAnalysis;
import gr.forth.ics.isl.demoExternal.main.ExternalKnowledgeDemoMain;
import static gr.forth.ics.isl.demoExternal.main.ExternalKnowledgeDemoMain.chanel;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 *
 * @author Lefteris Dimitrakis
 */
public class ExternalEvaluator {

    public static void main(String[] args) throws IOException, JSONException {
        /*
        ExternalKnowledgeDemoMain.initializeToolsAndResources("WNHOME");

            TreeMap<Integer, String> questionId_question;

        questionId_question = readQuestionsFile("simpleQuestions");


        int no_entities = 0;
        for (int question_id : questionId_question.keySet()) {
            if (question_id == 1001) {
                break;
            }
            if (question_id > 500) {
                //ExternalKnowledgeDemoMain.getEntities(questionId_question.get(question_id), "coreNLP");
                HashMap<String, String> tmp = getNamedEntities(questionId_question.get(question_id), String.valueOf(question_id), "both");
                if (tmp.isEmpty()) {
                    no_entities++;
                }
            }
        }
        System.out.println(no_entities);
         */

        try {
            ExternalKnowledgeDemoMain.initializeToolsAndResources("WNHOME");

            TreeMap<Integer, String> questionId_question;

            questionId_question = readQuestionsFile("simpleQuestionsReformulated");

            TreeMap<Integer, JSONObject> questionId_answer = partialPipelineEvaluation(questionId_question, 875, 125);

            try {
                writeSystemAnswersToFile(questionId_answer, "simpleQuestions_WithoutVerb_1000");
                writeSystemAnswersAsJsonToFile(questionId_answer, "simpleQuestions_WithoutVerb_Detailed1000");
            } catch (JSONException ex) {
                Logger.getLogger(ExternalEvaluator.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (IOException ex) {
            Logger.getLogger(ExternalEvaluator.class.getName()).log(Level.SEVERE, null, ex);
        }

        //validateAnswers("simpleQuestions_OurMethod_300", "simpleQuestions_answers");
        //getStatsForUnansweredQuestions("simpleQuestions_OurMethod_Detailed300");
    }

    public static void validateAnswers(String system_ans_filename, String gold_ans_filename) throws JSONException {

        TreeMap<Integer, JSONObject> qID_answers = readAnswersFiles(system_ans_filename, gold_ans_filename);
        JSONObject answers;

        JSONObject system_answer, gold_answer;
        int total_questions = 0;
        int answered_questions = 0;
        int correct_answered_questions = 0;
        int unanswered_questions = 0;

        for (int question_id : qID_answers.keySet()) {
            total_questions++;

            answers = qID_answers.get(question_id);

            if (answers.getJSONObject("system_answer").length() == 0) {
                unanswered_questions++;
                System.out.println("There is NOT system answer for: q" + question_id);
            } else {
                answered_questions++;
                System.out.println("There is system answer for: q" + question_id);
                system_answer = answers.getJSONObject("system_answer");
                gold_answer = answers.getJSONObject("gold_answer");

                try {
                    if (isMatchingTriples(system_answer, gold_answer)) {
                        correct_answered_questions++;
                        System.out.println("Correct answer for: q" + question_id);
                    }
                } catch (JSONException ex) {
                    Logger.getLogger(ExternalEvaluator.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

        }

        System.out.println("Total questions:" + total_questions);
        System.out.println("Unanswered questions:" + unanswered_questions);
        System.out.println("Answered questions:" + answered_questions);
        System.out.println("Correct answered questions:" + correct_answered_questions);

    }

    public static TreeMap<Integer, JSONObject> evaluatePipeline(TreeMap<Integer, String> questionId_question, int num_of_questions) {

        TreeMap<Integer, JSONObject> questionId_systemAnswer = new TreeMap<>();
        JSONObject tmp_answer;

        for (int question_id : questionId_question.keySet()) {
            tmp_answer = ExternalKnowledgeDemoMain.getAnswerAsJson(questionId_question.get(question_id));
            questionId_systemAnswer.put(question_id, tmp_answer);
            System.out.println("==== Question_id: " + question_id + "====");

            if (question_id == num_of_questions) {
                break;
            }
        }
        return questionId_systemAnswer;
    }

    public static TreeMap<Integer, JSONObject> partialPipelineEvaluation(TreeMap<Integer, String> questionId_question, int start_question_id, int num_of_questions) throws JSONException {

        TreeMap<Integer, JSONObject> questionId_systemAnswer = new TreeMap<>();
        JSONObject tmp_answer;

        int answered = 0;
        int unanswered = 0;
        for (int question_id : questionId_question.keySet()) {
            if (question_id > start_question_id) {
                if (question_id == 19 || question_id == 55 || question_id == 90 || question_id == 133 || question_id == 151 || question_id == 159 || question_id == 164 || question_id == 165 || question_id == 199 || question_id == 224 || question_id == 240 || question_id == 289 || question_id == 290 || question_id == 308 || question_id == 411 || question_id == 424 || question_id == 443 || question_id == 466 || question_id == 490 || question_id == 506 || question_id == 507 || question_id == 519 || question_id == 523 || question_id == 525 || question_id == 537 || question_id == 615 || question_id == 622 || question_id == 652 || question_id == 841 || question_id == 848 || question_id == 880 || question_id == 969) {
                    //if (question_id == 133 || question_id == 159 || question_id == 165) {
                    continue;
                }
                System.out.println("==== Question_id: " + question_id + "====");
                tmp_answer = ExternalKnowledgeDemoMain.getAnswerAsJson(questionId_question.get(question_id));
                if (tmp_answer.has("errorMessage") || !new JSONObject(tmp_answer.getString("triple")).has("subject")) {
                    unanswered++;
                } else {
                    answered++;
                }
                questionId_systemAnswer.put(question_id, tmp_answer);

                System.out.println("Total: " + (unanswered + answered) + " Unanswered: " + unanswered + " Answered: " + answered);
                if ((question_id - start_question_id) == num_of_questions) {
                    break;
                }
            }
        }
        return questionId_systemAnswer;
    }

    public static void writeSystemAnswersToFile(TreeMap<Integer, JSONObject> questionId_systemAnswer, String filename) throws JSONException {
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new FileWriter("src/main/resources/external/evaluation/" + filename + ".txt", false));
            StringBuilder sb;
            JSONObject system_answer;
            for (int question_id : questionId_systemAnswer.keySet()) {
                sb = new StringBuilder();
                sb.append("q").append(question_id);

                if (questionId_systemAnswer.get(question_id).has("errorMessage") || !new JSONObject(questionId_systemAnswer.get(question_id).getString("triple")).has("subject")) {
                    sb.append("\n");
                    bw.write(sb.toString());
                } else {
                    system_answer = new JSONObject(questionId_systemAnswer.get(question_id).getString("triple"));

                    sb.append("\t").append(system_answer.getString("subject")).append("\t").append(system_answer.getString("predicate")).append("\t").append(system_answer.getString("object")).append("\n");
                    bw.write(sb.toString());
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(ExternalEvaluator.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                bw.close();
            } catch (IOException ex) {
                Logger.getLogger(ExternalEvaluator.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public static void writeSystemAnswersAsJsonToFile(TreeMap<Integer, JSONObject> questionId_systemAnswer, String filename) throws JSONException {
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new FileWriter("src/main/resources/external/evaluation/" + filename + ".txt", false));
            StringBuilder sb;
            JSONObject system_answer;
            for (int question_id : questionId_systemAnswer.keySet()) {
                sb = new StringBuilder();
                sb.append("q").append(question_id).append("\t");
                sb.append(questionId_systemAnswer.get(question_id).toString()).append("\n");

                bw.write(sb.toString());

            }
        } catch (IOException ex) {
            Logger.getLogger(ExternalEvaluator.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                bw.close();
            } catch (IOException ex) {
                Logger.getLogger(ExternalEvaluator.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public static TreeMap<Integer, String> readQuestionsFile(String filename) throws FileNotFoundException, IOException {

        BufferedReader br = new BufferedReader(new FileReader("src/main/resources/external/evaluation/" + filename + ".txt"));

        TreeMap<Integer, String> questionId_question = new TreeMap<>();
        String[] id_question;
        try {
            String line = br.readLine();
            while (line != null) {

                if (!line.trim().isEmpty()) {
                    id_question = line.split("\t");
                    //keep only the number of the question id e.g. q1 -> 1
                    questionId_question.put(Integer.parseInt(id_question[0].replaceAll("q", "")), id_question[1]);
                }
                line = br.readLine();
            }
        } finally {
            br.close();
        }
        return questionId_question;
    }

    public static void getStatsForUnansweredQuestions(String system_ans_filename) throws FileNotFoundException, IOException, JSONException {
        TreeMap<Integer, JSONObject> result = new TreeMap<>();

        BufferedReader br_sa = null;
        HashMap<String, JSONObject> system_answers = new HashMap<>();

        br_sa = new BufferedReader(new FileReader("src/main/resources/external/evaluation/" + system_ans_filename + ".txt"));

        String line = br_sa.readLine();
        JSONObject tmpJson;

        // Read system answers and store them in a HashMap of <Question_is, JsonObject containing a triple: subject, predicate, object>
        while (line != null) {
            String[] questionAnswer = line.split("\t");
            system_answers.put(questionAnswer[0], new JSONObject(questionAnswer[1]));
            line = br_sa.readLine();
        }

        int no_question_type = 0;
        int no_entities = 0;
        int no_useful_words = 0;
        int no_uris = 0;
        int no_triples = 0;
        for (JSONObject obj : system_answers.values()) {
            if (obj.has("errorMessage")) {
                String message = obj.getString("errorMessage");

                if (message.equalsIgnoreCase("[QuestionAnalysis] Unrecognized type of question.")) {
                    System.out.println(obj.getString("question"));
                    no_question_type++;
                } else if (message.equalsIgnoreCase("[QuestionAnalysis] No Named Entities recognized.")) {
                    //System.out.println(obj.getString("question"));
                    no_entities++;
                } else if (message.equalsIgnoreCase("[QuestionAnalysis] No available useful words.")) {
                    //System.out.println(obj.getString("question"));
                    no_useful_words++;
                } else if (message.startsWith("[EntitiesDetection] No retrieved URIs for entity")) {
                    no_uris++;
                } else if (message.equalsIgnoreCase("[AnswerExtraction] No candidate triples found.")) {
                    System.out.println(obj.getString("question"));
                    no_triples++;
                }

            }
        }

        System.out.println("Unrecognized type of question: " + no_question_type);
        System.out.println("No named entities recognized: " + no_entities);
        System.out.println("No available useful words: " + no_useful_words);
        System.out.println("No retrieved URIs: " + no_uris);
        System.out.println("No retrieved triples: " + no_triples);
    }

    public static TreeMap<Integer, JSONObject> readAnswersFiles(String system_ans_filename, String gold_ans_filename) {
        TreeMap<Integer, JSONObject> result = new TreeMap<>();
        try {
            BufferedReader br_sa = null;
            HashMap<String, JSONObject> system_answers = new HashMap<>();
            BufferedReader br_ga = null;
            HashMap<String, JSONObject> gold_answers = new HashMap<>();

            br_sa = new BufferedReader(new FileReader("src/main/resources/external/evaluation/" + system_ans_filename + ".txt"));
            br_ga = new BufferedReader(new FileReader("src/main/resources/external/evaluation/" + gold_ans_filename + ".txt"));

            String line = br_sa.readLine();
            JSONObject tmpJson;
            String max_questionId = "";

            // Read system answers and store them in a HashMap of <Question_is, JsonObject containing a triple: subject, predicate, object>
            while (line != null) {
                String[] questionAnswer = line.split("\t");
                // if there is no system answer i.e. no triple found
                if (questionAnswer.length == 1) {
                    system_answers.put(questionAnswer[0], new JSONObject());
                } else {
                    tmpJson = new JSONObject();
                    tmpJson.put("subject", questionAnswer[1]);
                    tmpJson.put("predicate", questionAnswer[2]);
                    tmpJson.put("object", questionAnswer[3]);
                    system_answers.put(questionAnswer[0], tmpJson);
                }

                max_questionId = questionAnswer[0];
                line = br_sa.readLine();
            }

            line = br_ga.readLine();
            // Read gold answers and store them in a HashMap of <Question_is, JsonObject containing a triple: subject, predicate, object>
            while (line != null) {
                String[] questionTriple = line.split("\t");
                tmpJson = new JSONObject();
                tmpJson.put("subject", questionTriple[1]);
                tmpJson.put("predicate", questionTriple[2]);
                tmpJson.put("object", questionTriple[3]);
                gold_answers.put(questionTriple[0], tmpJson);
                if (questionTriple[0].equalsIgnoreCase(max_questionId)) {
                    break;
                }
                line = br_ga.readLine();
            }

            JSONObject tmp_answers;

            // Combine the answer into a JsonObject containing two attributes: system_answer and gold_answer
            // Construct a TreeMap of <question_id, combined JsonObject>
            for (String quest_id : system_answers.keySet()) {
                tmp_answers = new JSONObject();
                JSONObject s_answer = system_answers.get(quest_id);
                tmp_answers.put("system_answer", s_answer);
                JSONObject g_answer = gold_answers.get(quest_id);
                tmp_answers.put("gold_answer", g_answer);

                result.put(Integer.parseInt(quest_id.replaceAll("q", "")), tmp_answers);
            }
            return result;
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ExternalEvaluator.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ExternalEvaluator.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JSONException ex) {
            Logger.getLogger(ExternalEvaluator.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    //TODO: To include also relation matching. When the object coreference service inlude sameAs for relations
    public static boolean isMatchingTriples(JSONObject system_triple, JSONObject gold_triple) throws JSONException {
        boolean matched_subject = false;
        boolean matched_predicate = true;
        boolean matched_object = false;

        String system_subject, gold_subject;
        //Retrieve the subject of the system answer triple and the gold answer triple
        system_subject = system_triple.getString("subject").trim();
        if (system_subject.startsWith("<") && system_subject.endsWith(">")) {
            system_subject = system_subject.substring(1, system_subject.length() - 1);
        }
        gold_subject = gold_triple.getString("subject").trim();

        // Retrieve the same as URIs for the two subjects
        ArrayList<String> system_equivalent_subjects = retrieveEquivalentEntityURIs(system_subject);
        ArrayList<String> gold_equivalent_subjects = retrieveEquivalentEntityURIs(gold_subject);

        // Check for URI match by checking all URI combinations
        for (String system_sub : system_equivalent_subjects) {
            for (String gold_sub : gold_equivalent_subjects) {
                if (AnswerExtraction.isMatchingUris(system_sub, gold_sub)) {
                    matched_subject = true;
                    break;
                }
            }
            if (matched_subject) {
                break;
            }
        }

        String system_object, gold_object;
        //Retrieve the object of the system answer triple and the gold answer triple
        system_object = system_triple.getString("object").trim();
        if (system_object.startsWith("<") && system_object.endsWith(">")) {
            system_object = system_object.substring(1, system_object.length() - 1);
        }
        gold_object = gold_triple.getString("object").trim();

        // Retrieve the same as URIs for the two objects
        ArrayList<String> system_equivalent_objects = retrieveEquivalentEntityURIs(system_object);
        ArrayList<String> gold_equivalent_objects = retrieveEquivalentEntityURIs(gold_object);

        // Check for URI match by checking all URI combinations
        for (String system_obj : system_equivalent_objects) {
            for (String gold_obj : gold_equivalent_objects) {
                if (AnswerExtraction.isMatchingUris(system_obj, gold_obj)) {
                    matched_object = true;
                    break;
                }
            }
            if (matched_object) {
                break;
            }
        }

        // If the whole triple is matched, return true
        if (matched_subject && matched_predicate && matched_object) {
            return true;
        } else {
            return false;
        }
    }

    public static ArrayList<String> retrieveEquivalentEntityURIs(String URI) {
        return chanel.getEquivalentEntityEvaluation(URI);
    }

    public static HashMap<String, String> getNamedEntities(String question, String question_id, String tool) {
        // ==== Question Analysis Step ====
        QuestionAnalysis q_analysis = new QuestionAnalysis();
        question = q_analysis.extractCleanQuestion(question);
        EntitiesDetection e_detection = new EntitiesDetection();
        if (tool.equalsIgnoreCase("dbpedia")) {
            HashMap<String, String> cand_entities_uris = e_detection.extractEntitiesWithSpotlight(question);

            if (cand_entities_uris.isEmpty()) {
                // Extract the Named Entities from the question with their type e.g. Location, Person etc.
                HashMap<String, String> word_NamedEntity = e_detection.extractCorenlpEntitiesWithType(question);

                if (word_NamedEntity.isEmpty()) {
                    System.out.println(question_id + "\t" + question + "\t" + word_NamedEntity + "\t" + "[dbpedia][corenlp] [noEntities][noEntities]");
                    return word_NamedEntity;
                } else {
                    // ==== Entities Detection Step ====
                    EntitiesDetection entities_detection = new EntitiesDetection();

                    // Retrieve for each entity its candidate URIs from LODSyndesis
                    entities_detection.retrieveCorenlpEntitiesCandidateURIs(word_NamedEntity.keySet());
                    /*
                    JSONObject e_dErrorHandling = ModulesErrorHandling.entitiesDetectionErrorHandling(entities_detection);

                    String status = "";
                    try {
                        status = e_dErrorHandling.getString("status");
                    } catch (JSONException ex) {
                        Logger.getLogger(ExternalKnowledgeDemoMain.class.getName()).log(Level.SEVERE, null, ex);
                    }

                    if (status.equalsIgnoreCase("error")) {
   */
                    if (entities_detection.getCorenlpEntitiesWithCandidateURIs() != null && !entities_detection.getCorenlpEntitiesWithCandidateURIs().isEmpty()) {
                        HashMap<String, String> entity_URI = new HashMap<>();
                        try {
                        // Hashmap to store each entity and the selected URI (the highest scored)
                       entity_URI = entities_detection.getMatchingCorenlpURIs(word_NamedEntity.keySet());
                        } catch (Exception ex) {
                            System.out.println(question_id + "\t" + question + "\t" + new HashMap<>() + "\t" + "[dbpedia][corenlp] [noEntities][noEntitiesLOD]");
                            return new HashMap<>();
                        }
                        System.out.println(question_id + "\t" + question + "\t" + entity_URI + "\t" + "[dbpedia][corenlp] [noEntities]");
                        return entity_URI;

                    } else {
                        System.out.println(question_id + "\t" + question + "\t" + new HashMap<>() + "\t" + "[dbpedia][corenlp] [noEntities][noEntitiesLOD]");
                        return new HashMap<>();
                    }
                }
            } else {
                System.out.println(question_id + "\t" + question + "\t" + cand_entities_uris + "\t" + "[dbpedia][dbpedia]");
                return cand_entities_uris;
            }
        } else if (tool.equalsIgnoreCase("coreNLP")) {
            // Extract the Named Entities from the question with their type e.g. Location, Person etc.
            HashMap<String, String> word_NamedEntity = e_detection.extractCorenlpEntitiesWithType(question);

            if (word_NamedEntity.isEmpty()) {
                HashMap<String, String> cand_entities_uris = e_detection.extractEntitiesWithSpotlight(question);
                if (cand_entities_uris.isEmpty()) {
                    System.out.println(question_id + "\t" + question + "\t" + cand_entities_uris + "\t" + "[corenlp][dbpedia] [noEntities][noEntities]");
                } else {
                    System.out.println(question_id + "\t" + question + "\t" + cand_entities_uris + "\t" + "[corenlp][dbpedia] [noEntities]");
                }
                return cand_entities_uris;
            } else {
                // ==== Entities Detection Step ====
                EntitiesDetection entities_detection = new EntitiesDetection();

                // Retrieve for each entity its candidate URIs from LODSyndesis
                entities_detection.retrieveCorenlpEntitiesCandidateURIs(word_NamedEntity.keySet());

                /*
                JSONObject e_dErrorHandling = ModulesErrorHandling.entitiesDetectionErrorHandling(entities_detection);

                String status = "";
                try {
                    status = e_dErrorHandling.getString("status");
                } catch (JSONException ex) {
                    Logger.getLogger(ExternalKnowledgeDemoMain.class.getName()).log(Level.SEVERE, null, ex);
                }

                if (status.equalsIgnoreCase("error")) {
                */
                if (entities_detection.getCorenlpEntitiesWithCandidateURIs() != null && !entities_detection.getCorenlpEntitiesWithCandidateURIs().isEmpty()) {

                HashMap<String, String> cand_entities_uris = entities_detection.extractEntitiesWithSpotlight(question);
                    if (cand_entities_uris.isEmpty()) {
                        System.out.println(question_id + "\t" + question + "\t" + cand_entities_uris + "\t" + "[corenlp][dbpedia] [noEntitiesLOD][noEntities]");
                    } else {
                        System.out.println(question_id + "\t" + question + "\t" + cand_entities_uris + "\t" + "[corenlp][dbpedia] [noEntitiesLOD]");
                    }

                    return cand_entities_uris;
                } else {

                    // Hashmap to store each entity and the selected URI (the highest scored)
                    HashMap<String, String> entity_URI = entities_detection.getMatchingCorenlpURIs(word_NamedEntity.keySet());

                    System.out.println(question_id + "\t" + question + "\t" + entity_URI + "\t" + "[corenlp][corenlp]");
                    return entity_URI;
                }
            }

        } else {

            // Extract the Named Entities from the question with their type e.g. Location, Person etc.
            HashMap<String, String> word_NamedEntity = e_detection.extractCorenlpEntitiesWithType(question);
            HashMap<String, String> cand_entities_uris = e_detection.extractEntitiesWithSpotlight(question);

            if (word_NamedEntity.isEmpty() && cand_entities_uris.isEmpty()) {
                System.out.println(question_id + "\t" + question + "\t" + cand_entities_uris + "\t" + "[corenlp-both] [noEntities][noEntities]");
                return new HashMap<>();
            } else if (word_NamedEntity.isEmpty()) {
                System.out.println(question_id + "\t" + question + "\t" + cand_entities_uris + "\t" + "[corenlp-both] [noEntities][]");
                return cand_entities_uris;
            } else if (cand_entities_uris.isEmpty()) {
                // ==== Entities Detection Step ====
                EntitiesDetection entities_detection = new EntitiesDetection();

                // Retrieve for each entity its candidate URIs from LODSyndesis
                entities_detection.retrieveCorenlpEntitiesCandidateURIs(word_NamedEntity.keySet());

                JSONObject e_dErrorHandling = ModulesErrorHandling.entitiesDetectionErrorHandling(entities_detection);

                String status = "";
                try {
                    status = e_dErrorHandling.getString("status");
                } catch (JSONException ex) {
                    Logger.getLogger(ExternalKnowledgeDemoMain.class.getName()).log(Level.SEVERE, null, ex);
                }

                if (status.equalsIgnoreCase("error")) {
                    System.out.println(question_id + "\t" + question + "\t" + cand_entities_uris + "\t" + "[corenlp-both] [noEntitiesLOD][noEntities]");
                    return new HashMap<>();
                } else {
                    // Hashmap to store each entity and the selected URI (the highest scored)
                    HashMap<String, String> entity_URI = entities_detection.getMatchingCorenlpURIs(word_NamedEntity.keySet());
                    System.out.println(question_id + "\t" + question + "\t" + entity_URI + "\t" + "[corenlp-both] [][noEntities]");
                    return entity_URI;
                }
            } else {
                // ==== Entities Detection Step ====
                EntitiesDetection entities_detection = new EntitiesDetection();

                // Retrieve for each entity its candidate URIs from LODSyndesis
                 entities_detection.retrieveCorenlpEntitiesCandidateURIs(word_NamedEntity.keySet());

                JSONObject e_dErrorHandling = ModulesErrorHandling.entitiesDetectionErrorHandling(entities_detection);

                String status = "";
                try {
                    status = e_dErrorHandling.getString("status");
                } catch (JSONException ex) {
                    Logger.getLogger(ExternalKnowledgeDemoMain.class.getName()).log(Level.SEVERE, null, ex);
                }

                if (status.equalsIgnoreCase("error")) {
                    System.out.println(question_id + "\t" + question + "\t" + cand_entities_uris + "\t" + "[corenlp-both] [noEntitiesLOD][]");
                    return cand_entities_uris;
                } else {
                    // Hashmap to store each entity and the selected URI (the highest scored)
                    HashMap<String, String> entity_URI = entities_detection.getMatchingCorenlpURIs(word_NamedEntity.keySet());

                    HashMap<String, String> combined_entities = entities_detection.extractCombinedEntities(question, entity_URI, cand_entities_uris);
                    System.out.println(question_id + "\t" + question + "\t" + combined_entities + "\t" + "[corenlp-both]");

                    return combined_entities;
                }
            }
        }
    }

}
