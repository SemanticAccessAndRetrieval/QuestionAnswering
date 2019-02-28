/*
 *  This code belongs to the Semantic Access and Retrieval (SAR) group of the
 *  Information Systems Laboratory (ISL) of the
 *  Institute of Computer Science (ICS) of the
 *  Foundation for Research and Technology - Hellas (FORTH)
 *  Nobody is allowed to use, copy, distribute, or modify this work.
 *  It is published for reasons of research results reproducibility.
 *  (c) 2017 Semantic Access and Retrieval group, All rights reserved
 */
package gr.forth.ics.isl.demo.evaluation;

import com.crtomirmajer.wmd4j.WordMovers;
import edu.mit.jwi.Dictionary;
import edu.mit.jwi.IDictionary;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import gr.forth.ics.isl.demo.evaluation.models.EvalPair;
import gr.forth.ics.isl.demo.evaluation.models.EvaluationPair;
import gr.forth.ics.isl.demo.evaluation.models.EvaluationResult;
import gr.forth.ics.isl.demo.models.BaselineModel;
import gr.forth.ics.isl.demo.models.Model;
import gr.forth.ics.isl.demo.models.Word2vecModel;
import gr.forth.ics.isl.demo.models.Word2vecModel_III;
import gr.forth.ics.isl.demo.models.WordnetModel;
import gr.forth.ics.isl.demo.models.WordnetWord2vecModel;
import gr.forth.ics.isl.demo.models.WordnetWord2vecModel_III;
import gr.forth.ics.isl.demoCombined.main.combinedDemoMain;
import gr.forth.ics.isl.nlp.models.Comment;
import gr.forth.ics.isl.utilities.StringUtils;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.word2vec.Word2Vec;

/**
 *
 * @author Sgo
 */
public class WebAPTestSuit implements Runnable {

    static String evalFilePath = "evalFilePath";
    static String evalCollection = "webAP.csv";
    static String resultFileName = "resultFileName";
    static String resultFilePath = "resultFilePath";
    static String wnhomePath = "wnhomePath";
    static String gModelPath = "gModelPath";
    static int threshold = 2;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException, FileNotFoundException, ClassNotFoundException {

        ////////////////////SET PROPERTIES/////////////////////////////////////
        Properties properties = new Properties();
        properties.load(WebAPTestSuitHQ.class.getResourceAsStream("/configuration/userReviewsExperimentsConfig.properties"));

        evalFilePath = properties.getProperty(evalFilePath);
        resultFileName = properties.getProperty(resultFileName);;
        resultFilePath = properties.getProperty(resultFilePath);
        wnhomePath = properties.getProperty(wnhomePath);
        gModelPath = properties.getProperty(gModelPath);

        //////////////////////WORD2VEC//////////////////////////////////////////
        File gModel = new File(gModelPath);
        Word2Vec vec = WordVectorSerializer.readWord2VecModel(gModel);
        WordMovers wm = WordMovers.Builder().wordVectors(vec).build();

        // Retrieve hyperparameters
        //ModelHyperparameters bestModel = (ModelHyperparameters) Utils.getSavedObject("AVEPbased_BestModel");
        float word2vec_w = 0.4f;// bestModel.getWord2vecWeight();
        float wordNet_w = 0.6f;//bestModel.getWordNetWeight();
        System.out.println("Wordnet weight: " + wordNet_w);
        System.out.println("Word2vec weight: " + word2vec_w);
        // Choose weights to be used in combination model
        HashMap<String, Float> model_weights = new HashMap<>();
        model_weights.put("wordnet", wordNet_w);
        model_weights.put("word2vec", word2vec_w);

        System.out.println("Statistical resources were loaded successfully");
        //////////////////////WORDNET///////////////////////////////////////////
        // Create WodNet Dictionary
        String wnhome = System.getenv(wnhomePath);
        String path = wnhome + File.separator + "dict";
        URL url = new URL("file", null, path);
        // construct the dictionary object and open it
        IDictionary dict = new Dictionary(url);
        // open WordNet dictionary
        dict.open();

        // Choose wordnet sources to be used
        ArrayList<String> wordnetResources = new ArrayList<>();

        System.out.println("Handmade resources were loaded successfully");

        ////////////////////////////////////////////////////////////////////////
        // Create the list of stopWords to use
        StringUtils.generateStopListsFromExternalSource(combinedDemoMain.filePath_en, combinedDemoMain.filePath_gr);

        // configure and initialize SCNLP pipeline
        Properties props = new Properties();
        props.put("annotators", "tokenize, ssplit, pos, lemma");
        props.put("tokenize.language", "en");
        combinedDemoMain.pipeline = new StanfordCoreNLP(props);

        System.out.println("SCNLP pipeline initialized successfully");

        // ground truth relevance between each query-passage pair
        HashMap<String, HashMap<String, EvaluationPair>> gt = EvalCollectionManipulator.readEvaluationSetExternal(evalCollection);

        System.out.println("Num of Queries: " + gt.size());

        HashMap<String, String> queryList = getQueryList(gt); // retrieve list of queries
        gt = null;

        System.out.println("Query list was loaded successfully");

        // context words
        ArrayList<String> contextWords = new ArrayList<>();
        contextWords.add("give");
        contextWords.add("information");
        contextWords.add("discribe");
        contextWords.add("state");
        // new
        //contextWords.add("identify");
        //contextWords.add("example");
        //contextWords.add("examples");
        //contextWords.add("way");
        //contextWords.add("ways");

        System.out.println("Context words were loaded successfully");

        // models to be evaluated
        ArrayList<String> models = new ArrayList<String>();
        //models.add("BSL");
        //models.add("WQE_woAH");
        //models.add("WQE_woH");
        //models.add("WQE");
        //models.add("W2V");
        models.add("W2V_cw");
        //models.add("W2V_sqe");
        models.add("W2V_cw_sqe");
        //models.add("CMB");
        models.add("CMB_cw");
        //models.add("CMB_sqe");
        models.add("CMB_cw_sqe");

        System.out.println("Model Names were loaded successfully");
        System.out.println("===================================================");

//        Model model = null; // model variable
//        ModelStats modelStats = null; // model statistics variable
        for (String modelName : models) {
            Thread crntThread = new Thread() {
                @Override
                public void run() {
                    // initialize curent model
                    if (modelName.equals("BSL")) {

                        BaselineModel model = new BaselineModel("BSL"); // Instantiate baseline model

                        System.out.println(model.getDescription() + " was initialized successfully");

                        try {
                            produceBigResultsWebAP(model, queryList);
                        } catch (IOException ex) {
                            Logger.getLogger(WebAPTestSuit.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        System.out.println(model.getDescription() + " was evaluated successfully");

                    } else if (modelName.equals("WQE_woAH")) {

                        ArrayList<String> wordnetResources = new ArrayList<>();
                        wordnetResources.add("synonyms");
                        WordnetModel model = new WordnetModel("WQE_woAH", dict, wordnetResources);

                        System.out.println(model.getDescription() + " was initialized successfully");

                        try {
                            produceBigResultsWebAP(model, queryList);
                        } catch (IOException ex) {
                            Logger.getLogger(WebAPTestSuit.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        System.out.println(model.getDescription() + " was evaluated successfully");

                    } else if (modelName.equals("WQE_woH")) {

                        ArrayList<String> wordnetResources = new ArrayList<>();
                        wordnetResources.add("synonyms");
                        wordnetResources.add("antonyms");
                        WordnetModel model = new WordnetModel("WQE_woH", dict, wordnetResources);

                        System.out.println(model.getDescription() + " was initialized successfully");

                        try {
                            produceBigResultsWebAP(model, queryList);
                        } catch (IOException ex) {
                            Logger.getLogger(WebAPTestSuit.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        System.out.println(model.getDescription() + " was evaluated successfully");

                    } else if (modelName.equals("WQE")) {

                        ArrayList<String> wordnetResources = new ArrayList<>();
                        wordnetResources.add("synonyms");
                        wordnetResources.add("antonyms");
                        wordnetResources.add("hypernyms");
                        WordnetModel model = new WordnetModel("WQE", dict, wordnetResources);

                        System.out.println(model.getDescription() + " was initialized successfully");

                        try {
                            produceBigResultsWebAP(model, queryList);
                        } catch (IOException ex) {
                            Logger.getLogger(WebAPTestSuit.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        System.out.println(model.getDescription() + " was evaluated successfully");

                    } else if (modelName.equals("W2V")) {

                        Word2vecModel model = new Word2vecModel("W2V", wm, vec);

                        System.out.println(model.getDescription() + " was initialized successfully");

                        try {
                            produceBigResultsWebAP(model, queryList);
                        } catch (IOException ex) {
                            Logger.getLogger(WebAPTestSuit.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        System.out.println(model.getDescription() + " was evaluated successfully");

                    } else if (modelName.equals("W2V_cw")) {

                        Word2vecModel model = new Word2vecModel("W2V_cw", wm, vec);
                        ((Word2vecModel) model).setContextWords(contextWords);

                        System.out.println(model.getDescription() + " was initialized successfully");

                        try {
                            produceBigResultsWebAP(model, queryList);
                        } catch (IOException ex) {
                            Logger.getLogger(WebAPTestSuit.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        System.out.println(model.getDescription() + " was evaluated successfully");

                    } else if (modelName.equals("W2V_sqe")) {

                        Word2vecModel_III model = new Word2vecModel_III("W2V_sqe", wm, vec);

                        System.out.println(model.getDescription() + " was initialized successfully");

                        try {
                            produceBigResultsWebAP(model, queryList);
                        } catch (IOException ex) {
                            Logger.getLogger(WebAPTestSuit.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        System.out.println(model.getDescription() + " was evaluated successfully");

                    } else if (modelName.equals("W2V_cw_sqe")) {

                        Word2vecModel_III model = new Word2vecModel_III("W2V_cw_sqe", wm, vec);
                        ((Word2vecModel_III) model).setContextWords(contextWords);

                        System.out.println(model.getDescription() + " was initialized successfully");

                        try {
                            produceBigResultsWebAP(model, queryList);
                        } catch (IOException ex) {
                            Logger.getLogger(WebAPTestSuit.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        System.out.println(model.getDescription() + " was evaluated successfully");

                    } else if (modelName.equals("CMB")) {

                        ArrayList<String> wordnetResources = new ArrayList<>();
                        wordnetResources.add("synonyms");
                        wordnetResources.add("antonyms");
                        wordnetResources.add("hypernyms");
                        WordnetWord2vecModel model = new WordnetWord2vecModel("CMB", dict, wordnetResources, wm, vec, model_weights);

                        System.out.println(model.getDescription() + " was initialized successfully");

                        try {
                            produceBigResultsWebAP(model, queryList);
                        } catch (IOException ex) {
                            Logger.getLogger(WebAPTestSuit.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        System.out.println(model.getDescription() + " was evaluated successfully");

                    } else if (modelName.equals("CMB_cw")) {

                        ArrayList<String> wordnetResources = new ArrayList<>();
                        wordnetResources.add("synonyms");
                        wordnetResources.add("antonyms");
                        wordnetResources.add("hypernyms");
                        WordnetWord2vecModel model = new WordnetWord2vecModel("CMB_cw", dict, wordnetResources, wm, vec, model_weights);
                        ((WordnetWord2vecModel) model).setContextWords(contextWords);

                        System.out.println(model.getDescription() + " was initialized successfully");

                        try {
                            produceBigResultsWebAP(model, queryList);
                        } catch (IOException ex) {
                            Logger.getLogger(WebAPTestSuit.class.getName()).log(Level.SEVERE, null, ex);
                        }

                        System.out.println(model.getDescription() + " was evaluated successfully");

                    } else if (modelName.equals("CMB_sqe")) {

                        ArrayList<String> wordnetResources = new ArrayList<>();
                        wordnetResources.add("synonyms");
                        wordnetResources.add("antonyms");
                        wordnetResources.add("hypernyms");
                        WordnetWord2vecModel_III model = new WordnetWord2vecModel_III("CMB_sqe", dict, wordnetResources, wm, vec, model_weights);

                        System.out.println(model.getDescription() + " was initialized successfully");

                        try {
                            produceBigResultsWebAP(model, queryList);
                        } catch (IOException ex) {
                            Logger.getLogger(WebAPTestSuit.class.getName()).log(Level.SEVERE, null, ex);
                        }

                        System.out.println(model.getDescription() + " was evaluated successfully");

                    } else if (modelName.equals("CMB_cw_sqe")) {

                        ArrayList<String> wordnetResources = new ArrayList<>();
                        wordnetResources.add("synonyms");
                        wordnetResources.add("antonyms");
                        wordnetResources.add("hypernyms");
                        WordnetWord2vecModel_III model = new WordnetWord2vecModel_III("CMB_cw_sqe", dict, wordnetResources, wm, vec, model_weights);
                        ((WordnetWord2vecModel_III) model).setContextWords(contextWords);

                        System.out.println(model.getDescription() + " was initialized successfully");

                        try {
                            produceBigResultsWebAP(model, queryList);
                        } catch (IOException ex) {
                            Logger.getLogger(WebAPTestSuit.class.getName()).log(Level.SEVERE, null, ex);
                        }

                        System.out.println(model.getDescription() + " was evaluated successfully");

                    } else {
                        System.out.println("There is no such model: " + modelName);
                    }

//            // produce result file of curent model
//            produceBigResultsWebAP(model, queryList);
//            System.out.println(model.getDescription() + " was evaluated successfully");
                }
            };
            crntThread.start();
        }

        System.out.println("Evaluation was completed successfully");
    }

    public static ArrayList<Comment> extractPassagesFromString(StringBuilder sb, int fileID) {
        String[] sentences = sb.toString().split("\n");
        ArrayList<Comment> passages = new ArrayList<>();
        String docName = null;
        String queryID = null;
        int crntDocPassagesID = 0;
        int passageID;

        for (String sentence : sentences) {

            if (sentence.contains("<TARGET_QID>") && sentence.contains("</TARGET_QID>")) {

                queryID = sentence.replaceAll("<TARGET_QID>", "").replaceAll("</TARGET_QID>", "");

            } else if (sentence.contains("<ORIGINAL_DOCNO>") && sentence.contains("</ORIGINAL_DOCNO>")) {

                docName = sentence.replaceAll("<ORIGINAL_DOCNO>", "").replaceAll("</ORIGINAL_DOCNO>", "");

            } else if (sentence.contains("<SENTENCE>") && sentence.contains("</SENTENCE>")) {

                sentence = sentence.replaceAll("<SENTENCE>", "").replaceAll("</SENTENCE>", "");

                if (sentence.split(" ").length < 4) {
                    continue;
                }

                crntDocPassagesID++;
                passageID = Integer.valueOf(queryID + "" + fileID + "" + crntDocPassagesID);
                Comment passage = new Comment(docName + queryID, docName + queryID, String.valueOf(passageID), sentence);
                passages.add(passage);

            } else {

                continue;

            }
        }

        return passages;
    }

    public static ArrayList<Comment> extractPassagesFromFiles(String queryID) throws FileNotFoundException, IOException {
        String folderPath = evalFilePath + queryID + "/";
        File folder = new File(folderPath);
        File[] listOfFiles = folder.listFiles();

        ArrayList<Comment> passages = new ArrayList<>();

        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile()) {
                StringBuilder sb = readFile(folder.getPath() + "/", listOfFiles[i].getName());
                passages.addAll(extractPassagesFromString(sb, i));
            }
        }

        return passages;
    }

    public static StringBuilder readFile(String path, String fileName) throws FileNotFoundException, IOException {

        String line;
        BufferedReader br = new BufferedReader(new FileReader(path + fileName));
        StringBuilder sb = new StringBuilder();

        while ((line = br.readLine()) != null) {
            sb.append(line + "\n");
        }

        return sb;
    }

    public static ArrayList<Comment> getCandidatePassages(String queryId) throws IOException {

        ArrayList<Comment> passages = extractPassagesFromFiles(queryId);
        ArrayList<Comment> cleanPassages = new ArrayList<>();
        ArrayList<String> uniquePassages = new ArrayList<>();

        for (Comment passage : passages) {
            if (cleanPassages.isEmpty()) {
                cleanPassages.add(passage);
                uniquePassages.add(passage.getText());
            } else {
                if (uniquePassages.contains(passage.getText())) {
                    continue;
                } else {
                    cleanPassages.add(passage);
                    uniquePassages.add(passage.getText());
                }
            }
        }

        return cleanPassages;

    }

    public static ArrayList<EvalPair> extractPairsFromString(StringBuilder sb, int fileID) {
        String[] sentences = sb.toString().split("\n");
        ArrayList<EvalPair> pairs = new ArrayList<>();
        String queryID = null;
        int crntDocPassagesID = 0;
        int passageID;
        int relevance = 0;

        for (String sentence : sentences) {

            if (sentence.contains("<NONE>")) {

                relevance = 0;

            } else if (sentence.contains("<FAIR>")) {

                relevance = 1;

            } else if (sentence.contains("<GOOD>")) {

                relevance = 2;

            } else if (sentence.contains("<EXCEL>")) {

                relevance = 3;

            } else if (sentence.contains("<PERFECT>")) {

                relevance = 4;

            } else if (sentence.contains("<TARGET_QID>") && sentence.contains("</TARGET_QID>")) {

                queryID = sentence.replaceAll("<TARGET_QID>", "").replaceAll("</TARGET_QID>", "");

            } else if (sentence.contains("<ORIGINAL_DOCNO>") && sentence.contains("</ORIGINAL_DOCNO>")) {

                continue;

            } else if (sentence.contains("<SENTENCE>") && sentence.contains("</SENTENCE>")) {

                sentence = sentence.replaceAll("<SENTENCE>", "").replaceAll("</SENTENCE>", "");

                if (sentence.split(" ").length < 4) {
                    continue;
                }
                //System.out.println(sentence);
                crntDocPassagesID++;
                passageID = Integer.valueOf(queryID + "" + fileID + "" + crntDocPassagesID);
                EvalPair ep = new EvalPair(queryID, String.valueOf(passageID), relevance);
                //System.out.println(queryID + " " + String.valueOf(passageID) + " " + relevance);
                pairs.add(ep);

            } else {

                continue;

            }
        }

        return pairs;
    }

    public static ArrayList<EvalPair> extractPairsFromFiles(String queryID) throws FileNotFoundException, IOException {
        String folderPath = evalFilePath + queryID + "/";
        File folder = new File(folderPath);
        File[] listOfFiles = folder.listFiles();

        ArrayList<EvalPair> pairs = new ArrayList<>();

        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile()) {
                StringBuilder sb = readFile(folder.getPath() + "/", listOfFiles[i].getName());
                pairs.addAll(extractPairsFromString(sb, i));
            }
        }

        return pairs;
    }

    public static HashMap<String, EvalPair> extractGroundTruth(String queryId) throws IOException {
        HashMap<String, EvalPair> pairsWithPassageId = new HashMap<>();
        ArrayList<EvalPair> pairs = extractPairsFromFiles(queryId);

        for (EvalPair p : pairs) {
            pairsWithPassageId.put(p.getPassageId(), p);
        }

        return pairsWithPassageId;
    }

    public static void produceResultsWebAP(Model model, HashMap<String, String> queryList, HashMap<String, HashMap<String, EvaluationPair>> gt) throws IOException {

        int resultId = 0;

        //for each query
        for (String qID : queryList.keySet()) {

            //Set passages to be ranked for the current query
            ArrayList<Comment> passages = getCandidatePassages(qID);
            model.setComments(passages); // set candidate passages

            System.out.println(passages.size());

            // Get the ground truth for the current query
            HashMap<String, EvalPair> evalPairsWithCrntQueryId = extractGroundTruth(qID);

            // init test set for the current query
            ArrayList<EvaluationResult> resultSet = new ArrayList<>();

            //Get the user's question
            String question = queryList.get(qID);

            model.scoreComments(question);
            ArrayList<Comment> rankedPassages = model.getTopComments(model.getComments().size());

            // for all retrieved comments
            for (Comment resultCom : rankedPassages) {
                // keep truck of comment's true and calculated relevance value
                // if comment is unjudged skip it
                EvalPair p = evalPairsWithCrntQueryId.get(resultCom.getId());
                EvaluationResult result = new EvaluationResult(resultId, Integer.valueOf(p.getQueryId()), question, Integer.valueOf(p.getPassageId()), resultCom.getText(), resultCom.getScore(), resultCom.getBestSentence());
                result.setPairRelevance(p.getRelevance());
                resultId++;
            }

            writeResultsToFile(resultSet, model.getDescription());
            System.out.println(qID);
        }
    }

    public static void producePartialResultsWebAP(ArrayList<String> crntQueryList, Model model, HashMap<String, String> queryList) throws IOException {
        for (String qID : crntQueryList) {

            //System.out.println("======" + queryNum++ + " queries have been evaluated so far==========");
            //Set passages to be ranked for the current query
            ArrayList<Comment> passages = getCandidatePassages(qID);
            model.setComments(passages); // set candidate passages
            System.out.println("Num of Passages for this query: " + passages.size());

            System.out.println("Candidate Passages for Query " + qID + " were loaded successfully");

            // Get the ground truth for the current query
            HashMap<String, EvalPair> evalPairsWithCrntQueryId = extractGroundTruth(qID);

            System.out.println("Evaluation Pairs for Query " + qID + " were loaded successfully");

            //Get the user's question
            String question = queryList.get(qID);

            model.scoreComments(question);
            ArrayList<Comment> rankedPassages = model.getTopComments(model.getComments().size());

            // for all retrieved comments
            for (Comment resultCom : rankedPassages) {
                // keep truck of comment's true and calculated relevance value
                // if comment is unjudged skip it
                //System.out.println(resultId + "passages so far...");
                EvalPair p = evalPairsWithCrntQueryId.get(resultCom.getId());
                long pairID = Long.valueOf(p.getQueryId() + p.getPassageId());
                EvaluationResult result = new EvaluationResult(pairID, Integer.valueOf(p.getQueryId()), question, Integer.valueOf(p.getPassageId()), resultCom.getText(), resultCom.getScore(), resultCom.getBestSentence());
                result.setPairRelevance(p.getRelevance());

                writeResultToFile(result, model.getDescription());
            }

            System.out.println("Query " + qID + " was evaluated successfully");
            System.out.println("===============================================================");
        }
    }

    public static Model getModelInstance(Model model) {

        if (model instanceof BaselineModel) {

            return new BaselineModel(model.getDescription());

        } else if (model instanceof WordnetModel) {

            return new WordnetModel(model.getDescription(), ((WordnetModel) model).getDictionary(), ((WordnetModel) model).getResourcesToRetrieve());

        } else if (model instanceof Word2vecModel) {

            Word2vecModel m = new Word2vecModel(model.getDescription(), ((Word2vecModel) model).getWordMovers(), ((Word2vecModel) model).getWord2Vec());
            m.setComments(model.getComments());
            if (((Word2vecModel) model).getContextWords() != null) {
                m.setContextWords(((Word2vecModel) model).getContextWords());
                return m;
            } else {
                return m;
            }

        } else if (model instanceof Word2vecModel_III) {

            Word2vecModel_III m = new Word2vecModel_III(model.getDescription(), ((Word2vecModel_III) model).getWordMovers(), ((Word2vecModel_III) model).getWord2Vec());
            m.setComments(model.getComments());
            if (((Word2vecModel_III) model).getContextWords() != null) {
                m.setContextWords(((Word2vecModel_III) model).getContextWords());
                return m;
            } else {
                return m;
            }

        } else if (model instanceof WordnetWord2vecModel) {

            WordnetWord2vecModel m = new WordnetWord2vecModel(model.getDescription(), ((WordnetWord2vecModel) model).getDictionary(), ((WordnetWord2vecModel) model).getResourcesToRetrieve(), ((WordnetWord2vecModel) model).getWordMovers(), ((WordnetWord2vecModel) model).getWord2Vec(), ((WordnetWord2vecModel) model).getModelWeights());
            m.setComments(model.getComments());
            if (((WordnetWord2vecModel) model).getContextWords() != null) {
                m.setContextWords(((WordnetWord2vecModel) model).getContextWords());
                return m;
            } else {
                return m;
            }

        } else if (model instanceof WordnetWord2vecModel_III) {

            WordnetWord2vecModel_III m = new WordnetWord2vecModel_III(model.getDescription(), ((WordnetWord2vecModel_III) model).getDictionary(), ((WordnetWord2vecModel_III) model).getResourcesToRetrieve(), ((WordnetWord2vecModel_III) model).getWordMovers(), ((WordnetWord2vecModel_III) model).getWord2Vec(), ((WordnetWord2vecModel_III) model).getModelWeights());
            m.setComments(model.getComments());
            if (((WordnetWord2vecModel_III) model).getContextWords() != null) {
                m.setContextWords(((WordnetWord2vecModel_III) model).getContextWords());
                return m;
            } else {
                return m;
            }

        }
        return null;
    }

    public static void produceBigResultsWebAP(Model model, HashMap<String, String> queryList) throws IOException {

        //for each query
        ArrayList<String> thread1Set = new ArrayList<>();
        ArrayList<String> thread2Set = new ArrayList<>();
        ArrayList<String> thread3Set = new ArrayList<>();
        ArrayList<String> thread4Set = new ArrayList<>();
        int cnt = 0;

        for (String qID : queryList.keySet()) {
            if (cnt < 20) {
                thread1Set.add(qID);
            } else if (cnt < 40) {
                thread2Set.add(qID);
            } else if (cnt < 60) {
                thread3Set.add(qID);
            } else if (cnt < 80) {
                thread4Set.add(qID);
            }

            cnt++;
        }

        Thread thread1 = new Thread() {
            public void run() {
                try {
                    producePartialResultsWebAP(thread1Set, getModelInstance(model), new HashMap<>(queryList));
                } catch (IOException ex) {
                    Logger.getLogger(WebAPTestSuit.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        };

        Thread thread2 = new Thread() {
            public void run() {
                try {
                    producePartialResultsWebAP(thread2Set, getModelInstance(model), new HashMap<>(queryList));
                } catch (IOException ex) {
                    Logger.getLogger(WebAPTestSuit.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        };

        Thread thread3 = new Thread() {
            public void run() {
                try {
                    producePartialResultsWebAP(thread3Set, getModelInstance(model), new HashMap<>(queryList));
                } catch (IOException ex) {
                    Logger.getLogger(WebAPTestSuit.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        };

        Thread thread4 = new Thread() {
            public void run() {
                try {
                    producePartialResultsWebAP(thread4Set, getModelInstance(model), new HashMap<>(queryList));
                } catch (IOException ex) {
                    Logger.getLogger(WebAPTestSuit.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        };

        thread1.start();
        thread2.start();
        thread3.start();
        thread4.start();
    }

    public static void writeResultToFile(EvaluationResult result, String model) throws IOException {

        String folderPath = resultFilePath + resultFileName + "_" + model + "/";
        File folder = new File(folderPath);

        if (!folder.exists()) {
            folder.mkdirs();
        }

        File file = new File(folder.getPath() + "/" + result.getQueryId() + ".tsv");
        BufferedWriter writer;

        if (file.exists()) {
            writer = new BufferedWriter(new FileWriter(file, true));
        } else {
            writer = new BufferedWriter(new FileWriter(file));
        }

        writer.write(result.toString());

        writer.close();
    }

    public static void writeResultsToFile(ArrayList<EvaluationResult> results, String model) throws IOException {

        File file = new File(resultFilePath + resultFileName + "_" + model + ".tsv");
        BufferedWriter writer;

        if (file.exists()) {
            writer = new BufferedWriter(new FileWriter(resultFilePath + resultFileName + "_" + model + ".tsv", true));
        } else {
            writer = new BufferedWriter(new FileWriter(resultFilePath + resultFileName + "_" + model + ".tsv"));
        }

        for (EvaluationResult er : results) {
            writer.write(er.toString());
        }

        writer.close();
    }

    public static void printEvaluationCollection(HashMap<String, HashMap<String, EvaluationPair>> evalCollection) {
        for (String queryId : evalCollection.keySet()) {
            for (String pairId : evalCollection.get(queryId).keySet()) {
                EvaluationPair pair = evalCollection.get(queryId).get(pairId);
                System.out.println("qID: " + queryId + " pairID: " + pairId + " pairRel: " + pair.getRelevance());
                System.out.println(pair.getQuery().getText());
                System.out.println(pair.getComment().getText());
                System.out.println("");
            }
        }
    }

    public static ArrayList<Comment> getPassagesFromWebAP() {
        ArrayList<Comment> comments = new ArrayList<>();

        String corpusPath = "src/main/resources/corpus/";
        String csvFile = corpusPath + "webAP.txt";

        BufferedReader br = null;
        String line = "";
        String cvsSplitBy = ";;";

        try {

            br = new BufferedReader(new FileReader(csvFile));
            while ((line = br.readLine()) != null) {

                // use comma as separator
                String[] comment = line.split(cvsSplitBy);

                String commentId = comment[0];
                String commentText = comment[1];

                Comment tmpComment = new Comment("", "", commentId, commentText);
                comments.add(tmpComment);

            }

            return comments;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static HashMap<String, String> getQueryList(HashMap<String, HashMap<String, EvaluationPair>> gt) {
        HashMap<String, String> queryList = new HashMap<>();

        for (String query_id : gt.keySet()) {
            HashMap<String, EvaluationPair> evalPairs = gt.get(query_id);
            queryList.put(query_id, evalPairs.values().iterator().next().getQuery().getText());
        }

        return queryList;
    }

    @Override
    public void run() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
