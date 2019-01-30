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
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import gr.forth.ics.isl.demo.evaluation.EvalCollectionManipulator;
import gr.forth.ics.isl.demo.evaluation.models.EvaluationPair;
import gr.forth.ics.isl.demo.main.OnFocusRRR;
import gr.forth.ics.isl.main.demo_main;
import gr.forth.ics.isl.nlp.NlpAnalyzer;
import gr.forth.ics.isl.nlp.models.Comment;
import gr.forth.ics.isl.sailInfoBase.QAInfoBase;
import gr.forth.ics.isl.sailInfoBase.models.Subject;
import gr.forth.ics.isl.utilities.StringUtils;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Properties;
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.word2vec.Word2Vec;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.RepositoryException;

/**
 *
 * @author Lefteris Dimitrakis and Sgo
 */
public class Word2vecModel extends Model {

    private WordMovers wordMovers;
    private final Word2Vec w2_vector;
    private double maxWMD = 0.0;
    private ArrayList<String> contextWords = null;

    public Word2vecModel(String description, WordMovers wm, Word2Vec w2v) {
        super.setDescription(description);
        this.wordMovers = wm;
        this.w2_vector = w2v;
    }

    public Word2vecModel(String description, WordMovers wm, Word2Vec w2v, ArrayList<Comment> comments) {
        super.setDescription(description);
        super.setComments(comments);
        this.wordMovers = wm;
        this.w2_vector = w2v;
    }

    public Word2vecModel(String description, WordMovers wm, Word2Vec w2v, ArrayList<Comment> comments, ArrayList<String> contextWords) {
        super.setDescription(description);
        super.setComments(comments);
        this.wordMovers = wm;
        this.w2_vector = w2v;
        this.contextWords = contextWords;
    }

    public void setContextWords(ArrayList<String> contextWords) {
        this.contextWords = contextWords;
    }

    public ArrayList<String> getContextWords() {
        return this.contextWords;
    }

    public WordMovers getWordMovers() {
        return this.wordMovers;
    }

    public Word2Vec getWord2Vec() {
        return this.w2_vector;
    }

    public void setWordMovers(WordMovers wm) {
        this.wordMovers = wm;
    }

    public String getCleanQueryAsString(String query) {
        ArrayList<String> querySet = NlpAnalyzer.getCleanTokens(query);
        ArrayList<String> querySetClean = new ArrayList<>();
        //Filter query words not contained in word2vec vocabulary
        for (String queryTerm : querySet) {
            if (this.w2_vector.hasWord(queryTerm)) {
                querySetClean.add(queryTerm);
            }
        }

        String querySetCleanAsString = "";
        for (String queryTerm : querySetClean) {
            querySetCleanAsString += " " + queryTerm;
        }
        return querySetCleanAsString;
    }

    public ArrayList<String> getCleanQuery(String query) {
        ArrayList<String> querySet = NlpAnalyzer.getCleanTokens(query);
        ArrayList<String> querySetClean = new ArrayList<>();
        //Filter query words not contained in word2vec vocabulary
        for (String queryTerm : querySet) {
            if (this.w2_vector.hasWord(queryTerm)) {
                querySetClean.add(queryTerm);
            }
        }

        return querySetClean;
    }

    public String weightContextWords(String query, ArrayList<String> contextWords) {
        ArrayList<String> queryClean = getCleanQuery(query);
        ArrayList<String> informativeTerms = new ArrayList<>();

        for (String queryTerm : queryClean) {
            if (!contextWords.contains(queryTerm)) {
                informativeTerms.add(queryTerm);
//                informativeTerms.add(queryTerm);
//                informativeTerms.add(queryTerm);
            }
        }

        queryClean.addAll(informativeTerms);
        query = "";
        for (String term : queryClean) {
            query += " " + term;
        }

        return query.trim();
    }

    @Override
    public void scoreComments(String query) {
        query = getCleanQueryAsString(query);

        if (contextWords != null) {
            query = weightContextWords(query, contextWords);
        }

        this.calculateMaxWMD(query);

        for (Comment com : this.getComments()) {
            this.scoreComment(com, query);
        }
    }

    @Override
    public void scoreComment(Comment com, String query) {
        double maxScore = Double.MIN_VALUE;
        double tmpDistance, tmpScore;
        String best_sentence = "";

        for (String sentence : NlpAnalyzer.getSentences(com.getText())) {
            tmpDistance = calculateWordMoversDistance(this.wordMovers, query, sentence, this.w2_vector);

            if (tmpDistance == -1.0f) {
                tmpDistance = this.maxWMD;
            }
            tmpScore = 1.0f - tmpDistance / this.maxWMD;

            if (tmpScore >= maxScore) {
                maxScore = tmpScore;
                best_sentence = sentence;
            }
        }
        com.setBestSentence(best_sentence);
        com.setScore(maxScore);
    }

    private double calculateWordMoversDistance(WordMovers wm, String query, String text, Word2Vec vec) {
        double distance = 0.0;

        ArrayList<String> querySet = NlpAnalyzer.getCleanTokens(query);
        String queryClean = "";
        //Filter query words not contained in word2vec vocabulary
        for (String queryTerm : querySet) {
            if (vec.hasWord(queryTerm)) {
                queryClean += " " + queryTerm;
            }
        }

        ArrayList<String> commentSet = NlpAnalyzer.getCleanTokens(text);
        String commentClean = "";
        //Filter comment words not contained in word2vec vocabulary
        for (String commentTerm : commentSet) {
            if (vec.hasWord(commentTerm)) {
                commentClean += " " + commentTerm;
            }
        }
        try {
            distance = wm.distance(commentClean, queryClean);

        } catch (Exception e) {
            //System.out.println("Comment: " + commentClean);
            //System.out.println("Query: " + queryClean);
            //e.printStackTrace();
            //System.out.println(e.getMessage());
            return -1.0;
        }
        return distance;
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

    public static void main(String[] args) throws RepositoryException, IOException, MalformedQueryException, QueryEvaluationException {

        StringUtils.generateStopLists(StringUtils.filePath_en, StringUtils.filePath_gr);
        QAInfoBase KB = new QAInfoBase();
        HashSet<Subject> hotels = new HashSet<>();
        hotels = KB.getAllSubjectsOfType("hippalus", "hippalusID");
        ArrayList<Comment> comments = demo_main.getComments(hotels, KB);
        //System.out.println(comments.size());

        File gModel = new File("C:/Users/Sgo/Desktop/Developer/Vector Models/GoogleNews-vectors-negative300.bin.gz");
        Word2Vec vec = WordVectorSerializer.readWord2VecModel(gModel);
        WordMovers wm = WordMovers.Builder().wordVectors(vec).build();

        Properties props = new Properties();
        props.put("annotators", "tokenize, ssplit, pos, lemma");
        props.put("tokenize.language", "en");
        OnFocusRRR.pipeline = new StanfordCoreNLP(props);

        HashMap<String, HashMap<String, EvaluationPair>> gt = EvalCollectionManipulator.readEvaluationSet("FRUCE_v2.csv");
        ArrayList<Comment> resultComs;

        // Get the ground truth for the current query
        HashMap<String, EvaluationPair> evalPairsWithCrntQueryId = null;

        ArrayList<String> contextWords = new ArrayList<>();
        contextWords.add("problem");
        contextWords.add("issue");
        contextWords.add("report");
        contextWords.add("hotel");
        contextWords.add("complaint");
        contextWords.add("anyone");
        contextWords.add("complain");
        System.out.println("Context Words : False");

        Word2vecModel word2vec = new Word2vecModel("Word2vec model", wm, vec, comments);
        word2vec.scoreComments("Has anyone reported a problem about cleanliness?");
        demo_main.printEvalOnlyComments(word2vec.getTopComments(comments.size()), gt.get("q3"));
        System.out.println();
        word2vec.scoreComments("Is the hotel staff helpful?");
        demo_main.printEvalOnlyComments(word2vec.getTopComments(comments.size()), gt.get("q6"));
        System.out.println();

        System.out.println("Context Words : True");

        Word2vecModel word2vecCW = new Word2vecModel("Word2vec model", wm, vec, comments, contextWords);
        word2vecCW.scoreComments("Has anyone reported a problem about cleanliness?");
        demo_main.printEvalOnlyComments(word2vecCW.getTopComments(comments.size()), gt.get("q3"));
        System.out.println();
        word2vecCW.scoreComments("Is the hotel staff helpful?");
        demo_main.printEvalOnlyComments(word2vecCW.getTopComments(comments.size()), gt.get("q6"));
        System.out.println();

    }
}
