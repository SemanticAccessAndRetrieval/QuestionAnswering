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

/**
 *
 * @author Sgo
 */

import com.crtomirmajer.wmd4j.WordMovers;
import edu.mit.jwi.IDictionary;
import edu.mit.jwi.item.POS;
import gr.forth.ics.isl.demo.evaluation.models.ModelHyperparameters;
import gr.forth.ics.isl.nlp.NlpAnalyzer;
import gr.forth.ics.isl.nlp.externalTools.WordNet;
import gr.forth.ics.isl.nlp.models.Comment;
import gr.forth.ics.isl.utilities.Utils;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.deeplearning4j.models.word2vec.Word2Vec;

public class WordnetWord2vecModel_II extends Model {

    private WordMovers wordMovers;
    private final Word2Vec w2_vector;
    private IDictionary dictionary;
    private ArrayList<String> resourcesToRetrieve;
    private double maxWMD = 0.0;

    public WordnetWord2vecModel_II(String description, IDictionary dict, ArrayList<String> resources, WordMovers wm, Word2Vec w2v, ArrayList<Comment> comments) {
        super.setDescription(description);
        super.setComments(comments);
        this.dictionary = dict;
        this.resourcesToRetrieve = resources;
        this.wordMovers = wm;
        this.w2_vector = w2v;
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

    public void setWordMovers(WordMovers wm) {
        this.wordMovers = wm;
    }

    @Override
    public void scoreComments(String query) {
        this.calculateMaxWMD(query, this.dictionary);

        for (Comment com : this.getComments()) {
            this.scoreComment(com, query);
        }

    }

    @Override
    public void scoreComment(Comment com, String query) {
        double maxWord2vecScore = Double.MIN_VALUE;
        double maxScore = Double.MIN_VALUE;
        String best_sentence = "";
        double tmpWord2vecScore, tmpWMD;

        for (String sentence : NlpAnalyzer.getSentences(com.getText())) {
            try {
                tmpWMD = calculateExpandedWMD(this.wordMovers, query, sentence, this.dictionary, this.w2_vector);
                if (tmpWMD == -1.0f) {
                    tmpWMD = this.maxWMD;
                }
                tmpWord2vecScore = 1.0f - tmpWMD / this.maxWMD;

                if (tmpWord2vecScore >= maxScore) {
                    maxWord2vecScore = tmpWord2vecScore;
                    //maxWordnetScore = tmpWordnetScore;
                    maxScore = tmpWord2vecScore;
                    best_sentence = sentence;
                }
            } catch (IOException ex) {
                Logger.getLogger(WordnetWord2vecModel_II.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        com.setBestSentence(best_sentence);
        com.setScore(maxScore);
    }

    private double calculateWordMoversDistance(WordMovers wm, ArrayList<String> querySet, ArrayList<String> commentSet, Word2Vec vec) {
        double distance = 0.0;

        ArrayList<String> querySetClean = new ArrayList<>();
        //Filter query words not contained in word2vec vocabulary
        for (String queryTerm : querySet) {
            if (vec.hasWord(queryTerm)) {
                querySetClean.add(queryTerm);
            }
        }

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

    private double calculateExpandedWMD(WordMovers wm, String query, String text, IDictionary dict, Word2Vec vec) throws IOException {

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

        //Calculate WMD on expanded query-comment pairs
        score = calculateWordMoversDistance(wm, new ArrayList<String>(querySynset), new ArrayList<String>(commentSynset), vec);

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

    public void calculateMaxWMD(String query, IDictionary dict) {
        double maxDistance = Double.MIN_VALUE;
        double tmpDistance;
        String crntTermPosTag;

        //Construct query's wordnet representation
        HashMap<String, String> queryMapWithPosTags = NlpAnalyzer.getCleanTokensWithPos(query);
        HashSet<String> querySynset = new HashSet<>();

        for (String queryTerm : queryMapWithPosTags.keySet()) {
            try {
                crntTermPosTag = queryMapWithPosTags.get(queryTerm);
                querySynset.addAll(getWordNetResources(crntTermPosTag, dict, queryTerm, this.resourcesToRetrieve));
            } catch (IOException ex) {
                Logger.getLogger(WordnetWord2vecModel_II.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        for (Comment com : this.getComments()) {
            //Construct comment's wordnet representation
            HashMap<String, String> commentMapWithPosTags = NlpAnalyzer.getCleanTokensWithPos(com.getText());
            HashSet<String> commentSynset = new HashSet<>();

            for (String commentTerm : commentMapWithPosTags.keySet()) {
                try {
                    crntTermPosTag = commentMapWithPosTags.get(commentTerm);
                    commentSynset.addAll(getWordNetResources(crntTermPosTag, dict, commentTerm, this.resourcesToRetrieve));
                } catch (IOException ex) {
                    Logger.getLogger(WordnetWord2vecModel_II.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            for (String sentence : NlpAnalyzer.getSentences(com.getText())) {
                try {
                    tmpDistance = calculateExpandedWMD(this.wordMovers, query, sentence, this.dictionary, this.w2_vector);

                    if (tmpDistance == -1.0f) {
                        continue;
                    }

                    if (tmpDistance >= maxDistance) {
                        maxDistance = tmpDistance;
                    }
                } catch (IOException ex) {
                    Logger.getLogger(WordnetWord2vecModel_II.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        this.maxWMD = maxDistance;
    }

    public static void main(String[] args) throws IOException, FileNotFoundException, ClassNotFoundException {
        ModelHyperparameters model = (ModelHyperparameters) Utils.getSavedObject("AVEPbased_BestModel");
        System.out.println(model);
    }

}
