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

import com.crtomirmajer.wmd4j.WordMovers;
import edu.mit.jwi.IDictionary;
import edu.mit.jwi.item.POS;
import static gr.forth.ics.isl.demo.evaluation.EvalCollectionManipulator.getNumOfRels;
import gr.forth.ics.isl.demo.evaluation.EvaluationMetrics;
import gr.forth.ics.isl.demo.evaluation.models.EvaluationPair;
import gr.forth.ics.isl.demo.evaluation.models.ModelHyperparameters;
import gr.forth.ics.isl.nlp.NlpAnalyzer;
import gr.forth.ics.isl.nlp.externalTools.WordNet;
import gr.forth.ics.isl.nlp.models.Comment;
import gr.forth.ics.isl.utilities.StringUtils;
import gr.forth.ics.isl.utilities.Utils;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.deeplearning4j.models.word2vec.Word2Vec;

/**
 *
 * @author Lefteris Dimitrakis
 */
public class WordnetWord2vecModel extends Model {

    private WordMovers wordMovers;
    private final Word2Vec w2_vector;
    private IDictionary dictionary;
    private ArrayList<String> resourcesToRetrieve;
    private HashMap<String, Float> modelWeights;
    private double maxWMD = 0.0;

    public WordnetWord2vecModel(String description, IDictionary dict, ArrayList<String> resources, WordMovers wm, Word2Vec w2v, HashMap<String, Float> weights, ArrayList<Comment> comments) {
        super.setDescription(description);
        super.setComments(comments);
        this.dictionary = dict;
        this.resourcesToRetrieve = resources;
        this.wordMovers = wm;
        this.w2_vector = w2v;
        this.modelWeights = weights;
    }

    public WordnetWord2vecModel(String description, IDictionary dict, ArrayList<String> resources, WordMovers wm, Word2Vec w2v, HashMap<String, Float> weights) {
        super.setDescription(description);
        this.dictionary = dict;
        this.resourcesToRetrieve = resources;
        this.wordMovers = wm;
        this.w2_vector = w2v;
        this.modelWeights = weights;
    }

    public IDictionary getDictionary() {
        return this.dictionary;
    }

    public void setDictionary(IDictionary dict) {
        this.dictionary = dict;
    }

    public ArrayList<String> getResourcesToRetrieve() {
        return this.resourcesToRetrieve;
    }

    public void setResourcesToRetrieve(ArrayList<String> resources) {
        this.resourcesToRetrieve = resources;
    }

    public WordMovers getWordMovers() {
        return this.wordMovers;
    }

    public Word2Vec getWord2Vec() {
        return this.w2_vector;
    }

    public HashMap<String, Float> getModelWeights() {
        return this.modelWeights;
    }

    public void setModelWeights(HashMap<String, Float> modelWeights) {
        this.modelWeights = modelWeights;
    }

    public void setWordMovers(WordMovers wm) {
        this.wordMovers = wm;
    }

    public ArrayList<Comment> scoreComments(String query, ArrayList<Comment> comments) {
        super.setComments(comments);
        this.calculateMaxWMD(query);

        for (Comment com : this.getComments()) {
            this.scoreComment(com, query);
        }

        return this.getComments();
    }

    @Override
    public void scoreComments(String query) {
        this.calculateMaxWMD(query);

        for (Comment com : this.getComments()) {
            this.scoreComment(com, query);
        }

    }

    @Override
    public void scoreComment(Comment com, String query) {
        double maxWord2vecScore = Double.MIN_VALUE;
        double maxWordnetScore = Double.MIN_VALUE;
        double maxScore = Double.MIN_VALUE;
        String best_sentence = "";
        double tmpWord2vecScore, tmpWordnetScore, tmpScore, tmpWMD;

        for (String sentence : NlpAnalyzer.getSentences(com.getText())) {
            try {
                tmpWMD = calculateWordMoversDistance(this.wordMovers, query, sentence, this.w2_vector);
                if (tmpWMD == -1.0f) {
                    tmpWMD = this.maxWMD;
                }
                tmpWord2vecScore = 1.0f - tmpWMD / this.maxWMD;

                tmpWordnetScore = calculateSynsetSimilarity(query, sentence, this.dictionary);

                tmpScore = calculatePartialScore(tmpWordnetScore, tmpWord2vecScore);
                if (tmpScore >= maxScore) {
                    maxWord2vecScore = tmpWord2vecScore;
                    maxWordnetScore = tmpWordnetScore;
                    maxScore = tmpScore;
                    best_sentence = sentence;
                }
            } catch (IOException ex) {
                Logger.getLogger(WordnetWord2vecModel.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        com.setBestSentence(best_sentence);
        com.setScore(maxScore);
    }

    private double calculateWordMoversDistance(WordMovers wm, String query, String text, Word2Vec vec) {
        double distance = 0.0;

        ArrayList<String> querySet = NlpAnalyzer.getCleanTokens(query);
        ArrayList<String> querySetClean = new ArrayList<>();
        //Filter query words not contained in word2vec vocabulary
        for (String queryTerm : querySet) {
            if (vec.hasWord(queryTerm)) {
                querySetClean.add(queryTerm);
            }
        }

        ArrayList<String> commentSet = NlpAnalyzer.getCleanTokens(text);
        ArrayList<String> commentSetClean = new ArrayList<>();
        //Filter comment words not contained in word2vec vocabulary
        for (String commentTerm : commentSet) {
            if (vec.hasWord(commentTerm)) {
                commentSetClean.add(commentTerm);
            }
        }
        try {
            distance = wm.distance(commentSetClean.toArray(new String[0]), querySetClean.toArray(new String[0]));

        } catch (Exception e) {
            //System.out.println("Comment: " + commentClean);
            //System.out.println("Query: " + queryClean);
            //e.printStackTrace();
            //System.out.println(e.getMessage());
            return -1.0;
        }
        return distance;
    }

    private double calculateSynsetSimilarity(String query, String text, IDictionary dict) throws IOException {

        double score;
        String crntTermPosTag;

        //Construct query's wordnet representation
        HashMap<String, String> queryMapWithPosTags = NlpAnalyzer.getCleanTokensWithPos(query);
        HashSet<String> querySynset = new HashSet<>();

        for (String queryTerm : queryMapWithPosTags.keySet()) {
            crntTermPosTag = queryMapWithPosTags.get(queryTerm);
            querySynset.addAll(getWordNetResources(crntTermPosTag, dict, queryTerm, this.resourcesToRetrieve));
        }

        //Construct comment's wordnet representation
        HashMap<String, String> commentMapWithPosTags = NlpAnalyzer.getCleanTokensWithPos(text);
        HashSet<String> commentSynset = new HashSet<>();

        for (String commentTerm : commentMapWithPosTags.keySet()) {
            crntTermPosTag = commentMapWithPosTags.get(commentTerm);
            commentSynset.addAll(getWordNetResources(crntTermPosTag, dict, commentTerm, this.resourcesToRetrieve));
        }

        //Calculate Jaccard Similarity
        score = StringUtils.JaccardSim((String[]) commentSynset.toArray(new String[0]), (String[]) querySynset.toArray(new String[0]));

        return score;
    }

    public HashSet<String> getWordNetResources(String pos, IDictionary dict, String token, ArrayList<String> resources) throws IOException {

        //Get the wordnet POS based on coreNLP POS
        POS word_pos = getWordNetPos(pos);

        if (word_pos == null) {
            return new HashSet<>();
        }

        HashSet<String> synset = new HashSet<>();
        HashSet<String> crntSynset;

        if (resources.contains("synonyms")) {
            crntSynset = WordNet.getSynonyms(dict, token, word_pos);
            if (crntSynset != null) {
                synset.addAll(crntSynset);
            }
        }

        if (resources.contains("antonyms")) {
            crntSynset = WordNet.getAntonyms(dict, token, word_pos);
            if (crntSynset != null) {
                synset.addAll(crntSynset);
            }
        }
        if (resources.contains("hypernyms")) {
            crntSynset = WordNet.getHypernyms(dict, token, word_pos);
            if (crntSynset != null) {
                synset.addAll(crntSynset);
            }
        }
        return synset;
    }

    //Get the wordnet POS based on coreNLP POS
    public POS getWordNetPos(String pos) {
        if (pos.startsWith("J")) {
            return POS.ADJECTIVE;
        } else if (pos.startsWith("R")) {
            return POS.ADVERB;
        } else if (pos.startsWith("N")) {
            return POS.NOUN;
        } else if (pos.startsWith("V")) {
            return POS.VERB;
        }
        return null;
    }

    public void calculateMaxWMD(String query) {
        double maxDistance = Double.MIN_VALUE;
        double tmpDistance;

        for (Comment com : this.getComments()) {
            for (String sentence : NlpAnalyzer.getSentences(com.getText())) {
                tmpDistance = calculateWordMoversDistance(this.wordMovers, query, sentence, this.w2_vector);

                if (tmpDistance == -1.0f) {
                    continue;
                }

                if (tmpDistance >= maxDistance) {
                    maxDistance = tmpDistance;
                }
            }
        }
        this.maxWMD = maxDistance;
    }

    public double calculatePartialScore(double wordnetScore, double word2vecScore) {
        float wordnet_w = this.modelWeights.get("wordnet");
        float word2vec_w = this.modelWeights.get("word2vec");

        return (wordnet_w * wordnetScore + word2vec_w * word2vecScore);

    }

    public ModelHyperparameters train(HashMap<String, HashMap<String, EvaluationPair>> gt, ArrayList<String> wordnetResources,
            HashMap<String, Float> model_weights, int relThreshold, String metric) {
        ModelHyperparameters bestModel = null;

        // Create a List of queries
        HashMap<String, String> queryList = new HashMap<>();
        for (String query_id : gt.keySet()) {
            HashMap<String, EvaluationPair> evalPairs = gt.get(query_id);
            queryList.put(query_id, evalPairs.values().iterator().next().getQuery().getText());
        }

        for (float word2vec_w = 0.0f; word2vec_w <= 1.0f; word2vec_w = word2vec_w + 0.1f) {
            for (float wordNet_w = 0.0f; wordNet_w <= 1.0f; wordNet_w = wordNet_w + 0.1f) {

                word2vec_w = Math.round(word2vec_w * 10.0f) / 10.0f;
                wordNet_w = Math.round(wordNet_w * 10.0f) / 10.0f;

                if (word2vec_w + wordNet_w != 1.0f) {
                    continue;
                }

                model_weights = new HashMap<>();
                model_weights.put("wordnet", wordNet_w);
                model_weights.put("word2vec", word2vec_w);
                WordnetWord2vecModel combination = new WordnetWord2vecModel("Word2vec and Wordnet", this.getDictionary(), wordnetResources, this.getWordMovers(), this.getWord2Vec(), model_weights, this.getComments());

                double metricValue = 0.0; // Avep
                ArrayList<Integer> testSet = new ArrayList<>(); // true binary relevance for a query

                //for each query
                for (String qID : queryList.keySet()) {
                    // Get the ground truth for the current query
                    HashMap<String, EvaluationPair> evalPairsWithCrntQueryId = gt.get(qID);
                    int R = getNumOfRels(evalPairsWithCrntQueryId, relThreshold);

                    // init test set for the current query
                    testSet = new ArrayList<>();

                    //Get the user's question
                    String question = queryList.get(qID);

                    System.out.println("========================");

                    //Print the weights for the scoring
                    System.out.println("word2vec weight: " + word2vec_w);
                    System.out.println("wordNet weight: " + wordNet_w);

                    combination.scoreComments(question);
                    ArrayList<Comment> rankedComments = combination.getTopComments(this.getComments().size());

                    // for all retrieved comments
                    for (Comment resultCom : rankedComments) {
                        // keep truck of comment's true and calculated relevance value
                        // if comment is unjudged skip it
                        EvaluationPair p = evalPairsWithCrntQueryId.get(resultCom.getId());
                        if (p != null) {
                            if (resultCom.getScore() > 0.0001f) {
                                testSet.add(p.getRelevance()); // true binarry relevance
                            } else {
                                testSet.add(0); // true binarry relevance
                            }

                            //System.out.println(p.getRelevance() + ", " + resultCom.getText());
                            //System.out.println(p.getComment().getId() + " -- " + resultCom.getId());
                        }
                    }

                    //System.out.println(testSet); // print test set with the order of result set
                    // Calculate the AveP of our system's answer
                    if (metric.equals("AVEP")) {
                        metricValue += EvaluationMetrics.AVEP(testSet, R, relThreshold);
                    } else if (metric.equals("P@2")) {
                        metricValue += EvaluationMetrics.R_Precision(testSet, 2, relThreshold);
                    } else {
                        System.out.println("There is no support for metric " + metric);
                    }

                }
                // Calculate mean AveP for all queries
                metricValue /= queryList.size();
                System.out.println(metricValue);

                // Add mean Avep score to the current model
                ModelHyperparameters crntModel = new ModelHyperparameters(metricValue, word2vec_w, wordNet_w);

                if (bestModel == null) {
                    bestModel = new ModelHyperparameters(metricValue, word2vec_w, wordNet_w);
                } else if (crntModel.compareTo(bestModel) > 0) {
                    bestModel = new ModelHyperparameters(metricValue, word2vec_w, wordNet_w);
                }
            }
        }

        HashMap<String, Float> modelWeights = new HashMap<>();
        modelWeights.put("wordnet", bestModel.getWordNetWeight());
        modelWeights.put("word2vec", bestModel.getWord2vecWeight());
        this.setModelWeights(modelWeights);

        System.out.println("=== Best Performing Model ===");
        System.out.println(bestModel);

        return bestModel;
    }

    public static void main(String[] args) throws IOException, FileNotFoundException, ClassNotFoundException {
        ModelHyperparameters model = (ModelHyperparameters) Utils.getSavedObject("AVEPbased_BestModel");
        System.out.println(model);
    }

}
