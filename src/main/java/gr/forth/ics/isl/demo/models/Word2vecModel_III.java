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
import java.util.HashSet;
import java.util.Properties;
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.word2vec.Word2Vec;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.ops.transforms.Transforms;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.RepositoryException;

/**
 *
 * @author Lefteris Dimitrakis
 */
public class Word2vecModel_III extends Model {

    private WordMovers wordMovers;
    private final Word2Vec w2_vector;
    private double maxWMD = 0.0;
    private ArrayList<String> contextWords = null;

    public Word2vecModel_III(String description, WordMovers wm, Word2Vec w2v, ArrayList<Comment> comments, ArrayList<String> contextWords) {
        super.setDescription(description);
        super.setComments(comments);
        this.wordMovers = wm;
        this.w2_vector = w2v;
        this.contextWords = contextWords;
    }

    public Word2vecModel_III(String description, WordMovers wm, Word2Vec w2v, ArrayList<Comment> comments) {
        super.setDescription(description);
        super.setComments(comments);
        this.wordMovers = wm;
        this.w2_vector = w2v;
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

    public ArrayList<String> getIdealExpantionTerms(ArrayList<String> candidateExpTerms, INDArray queryVec) {
        ArrayList<String> expantionTerms = new ArrayList();
        Double maxCosineSim = 0.0;
        Double tmpCosineSim = 0.0;
        INDArray expantionVec = null;
        for (String candidate : candidateExpTerms) {
            if (expantionTerms.isEmpty()) {
                expantionTerms.add(candidate);
                expantionVec = this.w2_vector.getWordVectorsMean(expantionTerms);
                maxCosineSim = Transforms.cosineSim(queryVec, expantionVec);
                continue;
            } else {
                expantionTerms.add(candidate);
                expantionVec = this.w2_vector.getWordVectorsMean(expantionTerms);
                tmpCosineSim = Transforms.cosineSim(queryVec, expantionVec);
                if (tmpCosineSim > maxCosineSim) {
                    maxCosineSim = tmpCosineSim;
                    continue;
                } else {
                    expantionTerms.remove(candidate);
                }
            }
        }
        return expantionTerms;
    }

    public ArrayList<String> weightContextWords(ArrayList<String> query, ArrayList<String> contextWords) {
        ArrayList<String> informativeTerms = new ArrayList<>();

        for (String queryTerm : query) {
            if (!contextWords.contains(queryTerm)) {
                informativeTerms.add(queryTerm);
            }
        }

        query.addAll(informativeTerms);

        return query;
    }

    public String expandQuery(String query) {
        ArrayList<String> queryClean = getCleanQuery(query);

        if (contextWords != null) {
            queryClean = weightContextWords(queryClean, contextWords);
        }

        INDArray queryVec = this.w2_vector.getWordVectorsMean(queryClean);

        ArrayList<String> candidateExpTerms = (ArrayList<String>) this.w2_vector.wordsNearest(queryVec, 10);
        candidateExpTerms.removeAll(queryClean);

        ArrayList<String> expantionTerms = getIdealExpantionTerms(candidateExpTerms, queryVec);

        String expandedQueryToString = "";

        for (String term : queryClean) {
            expandedQueryToString += " " + term;
        }

        for (String term : expantionTerms) {
            expandedQueryToString += " " + term;
        }

        return expandedQueryToString.trim();
    }

    @Override
    public void scoreComments(String query) {
        String expandedQuery = expandQuery(query);
        //System.out.println(expandedQuery);
        this.calculateMaxWMD(expandedQuery);

        for (Comment com : this.getComments()) {
            this.scoreComment(com, expandedQuery);
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

    private double calculateWordMoversDistance(WordMovers wm, String queryClean, String text, Word2Vec vec) {
        double distance = 0.0;

//        //Filter query words not contained in word2vec vocabulary
//        for (String queryTerm : querySet) {
//            if (vec.hasWord(queryTerm)) {
//                queryClean += " " + queryTerm;
//            }
//        }
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
        System.out.println(comments.size());

        File gModel = new File("C:/Users/Sgo/Desktop/Developer/Vector Models/GoogleNews-vectors-negative300.bin.gz");
        Word2Vec vec = WordVectorSerializer.readWord2VecModel(gModel);
        WordMovers wm = WordMovers.Builder().wordVectors(vec).build();

        Properties props = new Properties();
        props.put("annotators", "tokenize, ssplit, pos, lemma");
        props.put("tokenize.language", "en");
        OnFocusRRR.pipeline = new StanfordCoreNLP(props);

        ArrayList<String> contextWords = new ArrayList<>();
        contextWords.add("problem");
        contextWords.add("issue");
        contextWords.add("report");
        contextWords.add("hotel");
        contextWords.add("complaint");
        contextWords.add("anyone");
        contextWords.add("complain");

        Word2vecModel_III word2vec = new Word2vecModel_III("Word2vec model", wm, vec, comments);
        word2vec.scoreComments("Has anyone reported a problem about noise?");
        System.out.println(word2vec.getTopComments(10));
        word2vec.scoreComments("Has anyone reported a problem about cleanliness?");
        System.out.println(word2vec.getTopComments(10));

        Word2vecModel_III word2vecCW = new Word2vecModel_III("Word2vec model CW", wm, vec, comments, contextWords);
        word2vecCW.scoreComments("Has anyone reported a problem about noise?");
        System.out.println(word2vecCW.getTopComments(10));
        word2vecCW.scoreComments("Has anyone reported a problem about cleanliness?");
        System.out.println(word2vecCW.getTopComments(10));
    }
}
