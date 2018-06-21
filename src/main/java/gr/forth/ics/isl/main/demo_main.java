/*
 *  This code belongs to the Semantic Access and Retrieval (SAR) group of the 
 *  Information Systems Laboratory (ISL) of the 
 *  Institute of Computer Science (ICS) of the  
 *  Foundation for Research and Technology - Hellas (FORTH)
 *  Nobody is allowed to use, copy, distribute, or modify this work.
 *  It is published for reasons of research results reproducibility.
 *  (c) 2017 Semantic Access and Retrieval group, All rights reserved
 */
package gr.forth.ics.isl.main;

import gr.forth.ics.isl.demo.evaluation.models.EvaluationPair;
import gr.forth.ics.isl.nlp.models.Comment;
import gr.forth.ics.isl.sailInfoBase.QAInfoBase;
import gr.forth.ics.isl.sailInfoBase.models.Subject;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import mitos.stemmer.trie.Trie;
import org.apache.lucene.queryparser.classic.ParseException;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.RepositoryException;

/**
 *
 * @author Lefteris Dimitrakis
 */
public class demo_main {

    //Number of top comments to retrieve
    static int topK = 10;

    static String evalFileName = "FRUCE_v2";
    //static String evalFileName = "webAP";
    //static String evalFileName = "BookingEvalCollection";
    static String evalCollection = evalFileName + ".csv";

    //The paths for the stopWords Files.
    public static String filePath_en = "src/main/resources/stoplists/stopwordsEn.txt";
    public static String filePath_gr = "src/main/resources/stoplists/stopwordsGr.txt";

    //A hashMap that contains two Trie, one for the English stopList and one for the Greek
    public static HashMap<String, Trie> stopLists = new HashMap<>();

    public static void main(String[] args) throws IOException, ParseException, FileNotFoundException, ClassNotFoundException, RepositoryException, MalformedQueryException, QueryEvaluationException {

        QAInfoBase KB = new QAInfoBase();
        ArrayList<String> targetSelection = new ArrayList<>(Arrays.asList("http://ics.forth.gr/isl/hippalus/#tazuru",
                "http://ics.forth.gr/isl/hippalus/#arima_onsen_tosen_goshobo",
                "http://ics.forth.gr/isl/hippalus/#the_b_kobe"));

        HashSet<Subject> lala = KB.getAllSubjectsOfTypeWithURIs("owl", "NamedIndividual", targetSelection);
        System.out.println(getCommentsOnFocus(KB, targetSelection));
//        HashSet<Subject> hotels = KB.getAllSubjectsOfType("hippalus", "hippalusID");
//        ArrayList<Comment> comments = getComments(hotels, KB);
//
//        int total = 0;
//
//        for (Comment c : comments) {
//            total += c.getText().split(" ").length;
//        }
//        System.out.println(total / 40.0f);*/
        //Create the list of stopWords to use
//        StringUtils.generateStopLists(filePath_en, filePath_gr);
//
//        File gModel = new File("C:/Users/Sgo/Desktop/Developer/Vector Models/GoogleNews-vectors-negative300.bin.gz");
//        Word2Vec vec = WordVectorSerializer.readWord2VecModel(gModel);
//        WordMovers wm = WordMovers.Builder().wordVectors(vec).build();
//
//        String wnhome = System.getenv("WNHOME");
//        String path = wnhome + File.separator + "dict";
//        URL url = new URL("file", null, path);
//        // construct the dictionary object and open it
//        IDictionary dict = new Dictionary(url);
//        dict.open();
//
//        QAInfoBase KB = new QAInfoBase();
//        HashSet<Subject> hotels = KB.getAllSubjectsOfType("hippalus", "hippalusID");
//
//        System.out.println("External Resources were loaded successfully");
//
//        // Get all the comments
//        ArrayList<Comment> comments = getComments(hotels, KB);
//
//        // Choose wordnet sources to be used
//        ArrayList<String> wordnetResources = new ArrayList<>();
//        wordnetResources.add("synonyms");
//        wordnetResources.add("antonyms");
//        wordnetResources.add("hypernyms");
//
//        // Retrieve hyperparameters
//        ModelHyperparameters bestModel = (ModelHyperparameters) Utils.getSavedObject("AVEPbased_BestModel");
//        float word2vec_w = bestModel.getWord2vecWeight();
//        float wordNet_w = bestModel.getWordNetWeight();
//        // Choose weights to be used in model IV
//        HashMap<String, Float> model_weights = new HashMap<>();
//        model_weights.put("wordnet", wordNet_w);
//        model_weights.put("word2vec", word2vec_w);
//
//        // This structure will contain the ground truth relevance between each
//        // query and each comment
//        HashMap<String, HashMap<String, EvaluationPair>> gt = readEvaluationSet(evalCollection);
//        ArrayList<Comment> resultComs;
//
//        // Get the ground truth for the current query
//        HashMap<String, EvaluationPair> evalPairsWithCrntQueryId = null;
//
//        System.out.println("\n=======");
//        System.out.println("WORDNET");
//        System.out.println("=======\n");
//
//        WordnetModel wordnet = new WordnetModel("Wordnet model", dict, wordnetResources, comments);
//
//        wordnet.scoreComments("Has anyone reported a problem about cleanliness?");
//        resultComs = wordnet.getTopComments(comments.size());
//        // Get the ground truth for the current query
//        evalPairsWithCrntQueryId = gt.get("q3");
//        // for all retrieved comments print top two in the eval set
//        printEvalOnlyComments(resultComs, evalPairsWithCrntQueryId);
//
//        wordnet.scoreComments("Is the hotel staff helpful?");
//        resultComs = wordnet.getTopComments(comments.size());
//        // Get the ground truth for the current query
//        evalPairsWithCrntQueryId = gt.get("q6");
//        printEvalOnlyComments(resultComs, evalPairsWithCrntQueryId);
//
//        System.out.println("\n=======");
//        System.out.println("WORDNET II");
//        System.out.println("=======\n");
//
//        WordnetModel_II wordnet_II = new WordnetModel_II("Wordnet model II", dict, wordnetResources, comments);
//
//        wordnet_II.scoreComments("Has anyone reported a problem about cleanliness?");
//        resultComs = wordnet_II.getTopComments(comments.size());
//        // Get the ground truth for the current query
//        evalPairsWithCrntQueryId = gt.get("q3");
//        printEvalOnlyComments(resultComs, evalPairsWithCrntQueryId);
//
//        wordnet_II.scoreComments("Is the hotel staff helpful?");
//        resultComs = wordnet_II.getTopComments(comments.size());
//        // Get the ground truth for the current query
//        evalPairsWithCrntQueryId = gt.get("q6");
//        printEvalOnlyComments(resultComs, evalPairsWithCrntQueryId);
//
//        System.out.println("\n=======");
//        System.out.println("WOR2VEC");
//        System.out.println("=======\n");
//
//        Word2vecModel word2vec = new Word2vecModel("Word2vec model", wm, vec, comments);
//
//        word2vec.scoreComments("Has anyone reported a problem about cleanliness?");
//        resultComs = word2vec.getTopComments(comments.size());
//        // Get the ground truth for the current query
//        evalPairsWithCrntQueryId = gt.get("q3");
//        printEvalOnlyComments(resultComs, evalPairsWithCrntQueryId);
//
//        word2vec.scoreComments("Is the hotel staff helpful?");
//        resultComs = word2vec.getTopComments(comments.size());
//        // Get the ground truth for the current query
//        evalPairsWithCrntQueryId = gt.get("q6");
//        printEvalOnlyComments(resultComs, evalPairsWithCrntQueryId);
//
//        System.out.println("\n=======");
//        System.out.println("WOR2VEC II");
//        System.out.println("=======\n");
//
//        Word2vecModel_II word2vec_II = new Word2vecModel_II("Word2vec model", wm, vec, comments);
//
//        word2vec_II.scoreComments("Has anyone reported a problem about cleanliness?");
//        resultComs = word2vec_II.getTopComments(comments.size());
//        // Get the ground truth for the current query
//        evalPairsWithCrntQueryId = gt.get("q3");
//        printEvalOnlyComments(resultComs, evalPairsWithCrntQueryId);
//
//        word2vec_II.scoreComments("Is the hotel staff helpful?");
//        resultComs = word2vec_II.getTopComments(comments.size());
//        // Get the ground truth for the current query
//        evalPairsWithCrntQueryId = gt.get("q6");
//        printEvalOnlyComments(resultComs, evalPairsWithCrntQueryId);

        /*
        while (true) {
            try {
                Scanner in = new Scanner(System.in);

                //Get the user's question
                String question = JOptionPane.showInputDialog("Submit your question", "");

                //Get best performing model
                ModelHyperparameters retrievalModel = (ModelHyperparameters) Utils.getSavedObject("AVEPbased_BestModel");
                //Get the weights for the scoring
                //System.out.println("Enter word2vec weight: ");
                //float word2vec_w = in.nextFloat();
                float word2vec_w = retrievalModel.getWord2vecWeight();
                System.out.println("word2vec weight: " + word2vec_w);
                //System.out.println("Enter wordNet weight: ");
                //float wordNet_w = in.nextFloat();
                float wordNet_w = retrievalModel.getWordNetWeight();
                System.out.println("wordNet weight: " + wordNet_w);
                //Get the threshold for relevancy
                //System.out.println("Enter threshold: ");
                //float threshold = in.nextFloat();
                float threshold = retrievalModel.getThreshold();
                System.out.println("threshold: " + threshold);

                // Get all the comments
                ArrayList<Comment> comments = getComments(hotels, KB);

                double max_dist = Double.MIN_VALUE;
                //Calculate score for each comment
                //Also calculate max word mover distance
                for (Comment com : comments) {
                    com.calculateScores(wm, question, vec, dict, word2vec_w, wordNet_w);
                    if (com.getWordScore() >= max_dist) {
                        max_dist = com.getWordScore();
                    }
                }

                //Normalize WordMoverDistance, and update comments with the final scores
                for (Comment com : comments) {
                    com.calculateWordScore(max_dist);
                    com.calculateScore(word2vec_w, wordNet_w);
                }

                // Get the best comments based on their score (currently all of them)
                ArrayList<Comment> topComments = getTopKComments(comments, topK);


                ArrayList<Comment> relevantComments = new ArrayList<>();
                HashMap<String, ArrayList<Comment>> commentsPerHotel = new HashMap<>();
                ArrayList<Comment> tmp;
                int rank = 1;

                for (Comment c : topComments) {
                    System.out.println("\nRank:" + rank + "\nComment:" + c.getText() + "\nScore:" + String.format("%.3f", c.getScore()) + "\nFor Hotel:" + c.getHotelId());
                    rank++;

                    // Filter comments based on the threshold
                    if (c.getScore() >= threshold) {
                        relevantComments.add(c);
                    }
                    // Group comments based on the hotel (commentsPerHotel)
                    if (!commentsPerHotel.containsKey(c.getHotelId())) {
                        tmp = new ArrayList<>();
                        tmp.add(c);
                        commentsPerHotel.put(c.getHotelId(), tmp);
                    } else {
                        tmp = commentsPerHotel.get(c.getHotelId());
                        tmp.add(c);
                        commentsPerHotel.put(c.getHotelId(), tmp);
                    }
                }

                //System.out.println(relevantComments);
                // Open comment exploratory dialogue based on comment relevance.
                System.out.println("\nI have found " + relevantComments.size() + " comments.(" + String.format("%.1f", ((float) relevantComments.size() / topComments.size()) * 100.0) + "%)");
                System.out.println("The most related comment is for hotel " + relevantComments.get(0).getHotelName() + " and says: " + relevantComments.get(0).getText());

                String showNextComment = "";
                int commentIdx = 0;
                while (true) {
                    System.out.println("Would you like to see the next most related comment? (yes/no)");
                    in = new Scanner(System.in);
                    showNextComment = in.nextLine();
                    if (showNextComment.equals("yes")) {
                        commentIdx++;
                        if (commentIdx == relevantComments.size()) {
                            break;
                        }
                        System.out.println("The next most related comment is for hotel " + relevantComments.get(commentIdx).getHotelName() + " and says: " + relevantComments.get(commentIdx).getText());
                    } else {
                        break;
                    }
                }

                // Sort comments by date (from the most recent to the least)
                Collections.sort(relevantComments, new Comparator<Comment>() {
                    public int compare(Comment c1, Comment c2) {
                        if (c1.getDate() == null || c2.getDate() == null) {
                            return 0;
                        }
                        return -c1.getDate().compareTo(c2.getDate());
                    }
                });
                // Open comment exploratory dialogue based on comment recency.
                System.out.println("The most recent comment says: " + relevantComments.get(0).getText());

                showNextComment = "";
                commentIdx = 0;
                while (true) {
                    System.out.println("Would you like to see the next most recent comment? (yes/no)");
                    in = new Scanner(System.in);
                    showNextComment = in.nextLine();
                    if (showNextComment.equals("yes")) {
                        commentIdx++;
                        if (commentIdx == relevantComments.size()) {
                            break;
                        }
                        System.out.println("The next most recent comment is for hotel " + relevantComments.get(commentIdx).getHotelName() + " and says: " + relevantComments.get(commentIdx).getText());
                    } else {
                        break;
                    }
                }

                // Print further information
                for (String hotel_id : commentsPerHotel.keySet()) {
                    System.out.println("=================================================");
                    System.out.println("Comments for hotel: " + hotel_id + "\n");
                    for (Comment c : commentsPerHotel.get(hotel_id)) {
                        System.out.println("TEXT: " + c.getText());
                        System.out.println("BEST_SENT.: " + c.getBestSentence());
                        System.out.println("SCORE: " + String.format("%.3f", c.getScore()) + "\n");
                    }
                }

            } catch (Exception e) {
                System.out.println(e.getMessage());
                continue;
            }
        }*/

    }

    public static ArrayList<Comment> getCommentsFromWebAP() {
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

    public static ArrayList<Comment> getCommentsFromBooking(HashSet<Subject> reviews) {
        ArrayList<Comment> comments = new ArrayList<>();

        for (Subject sub : reviews) {
            HashMap<String, HashSet<String>> crntCommentsProps = sub.getUndeclaredDataTypePropsWithValues();

            String date = crntCommentsProps.get("http://ics.forth.gr/isl/hippalus/#hasDate").iterator().next();
            String text = crntCommentsProps.get("http://ics.forth.gr/isl/hippalus/#hasText").iterator().next();

            Comment tmpComment = new Comment("", "", sub.getUri(), text, date);
            comments.add(tmpComment);
        }

        return comments;
    }

    public static ArrayList<Comment> getComments(HashSet<Subject> allSubjectsOfType, QAInfoBase KB) throws RepositoryException, MalformedQueryException, QueryEvaluationException {
        ArrayList<Comment> comments = new ArrayList<>();

        for (Subject sub : allSubjectsOfType) {
            HashMap<String, HashSet<String>> crntHotelObjectProps = sub.getObjectPropsWithObjectValues();
            for (String prop : crntHotelObjectProps.keySet()) {
                HashSet<String> crntHotelCommentIds = crntHotelObjectProps.get(prop);
                for (String commentId : crntHotelCommentIds) {
                    HashMap<String, HashSet<String>> commentProps = KB.getAllUndeclaredDataTypePropertiesWithValuesOf(commentId);

                    String date = commentProps.get("http://ics.forth.gr/isl/hippalus/#hasDate").iterator().next();
                    String text = commentProps.get("http://ics.forth.gr/isl/hippalus/#hasText").iterator().next();

                    Comment tmpComment = new Comment(sub.getLabel(), sub.getUri(), commentId, text, date);
                    comments.add(tmpComment);
                }
            }
        }

        return comments;
    }
    public static String getLabelOfUri(String uri) {
        String[] uriParts = uri.split("/");
        String[] location = uriParts[uriParts.length - 1].split("#");
        String label = location[location.length - 1];
        return label;
    }

    public static ArrayList<Comment> getCommentsOnFocus(QAInfoBase KB, ArrayList<String> uris) throws RepositoryException, MalformedQueryException, QueryEvaluationException {

        HashSet<ArrayList<String>> commentTuples = KB.getCommentsOnFocus(uris);

        ArrayList<Comment> comments = new ArrayList<>();

        for (ArrayList<String> commentTuple : commentTuples) {
            commentTuple.get(0);
            Comment comment = new Comment(getLabelOfUri(commentTuple.get(0)), commentTuple.get(0), commentTuple.get(1), commentTuple.get(2), commentTuple.get(3));
            comments.add(comment);
        }

        return comments;
    }

    public static HashMap<String, Comment> getCommentsAsMap(HashSet<Subject> allSubjectsOfType, QAInfoBase KB) throws RepositoryException, MalformedQueryException, QueryEvaluationException {
        HashMap<String, Comment> comments = new HashMap<>();

        for (Subject sub : allSubjectsOfType) {
            HashMap<String, HashSet<String>> crntHotelObjectProps = sub.getObjectPropsWithObjectValues();
            for (String prop : crntHotelObjectProps.keySet()) {
                HashSet<String> crntHotelCommentIds = crntHotelObjectProps.get(prop);
                for (String commentId : crntHotelCommentIds) {
                    HashMap<String, HashSet<String>> commentProps = KB.getAllUndeclaredDataTypePropertiesWithValuesOf(commentId);

                    String date = commentProps.get("http://ics.forth.gr/isl/hippalus/#hasDate").iterator().next();
                    String text = commentProps.get("http://ics.forth.gr/isl/hippalus/#hasText").iterator().next();

                    Comment tmpComment = new Comment(sub.getLabel(), sub.getUri(), commentId, text, date);
                    comments.put(tmpComment.getId(), tmpComment);
                }
            }
        }

        return comments;
    }

    public static ArrayList<Comment> getTopKComments(ArrayList<Comment> comments, int topK) {
        ArrayList<Comment> topComments = new ArrayList<>();

        // Sort comments by score (in decreasing order)
        Collections.sort(comments, new Comparator<Comment>() {
            public int compare(Comment c1, Comment c2) {
                return -Double.compare(c1.getScore(), c2.getScore());
            }
        });

        // Get the top Comments
        if (topK <= comments.size()) {
            topComments = new ArrayList<>(comments.subList(0, topK));
        } else {
            topComments = comments;
        }

        return topComments;
    }

    public static void printEvalOnlyComments(ArrayList<Comment> resultComs, HashMap<String, EvaluationPair> evalPairsWithCrntQueryId) {
        int cnt = 0;
        for (Comment resultCom : resultComs) {
            // keep truck of comment's true and calculated relevance value
            // if comment is unjudged skip it
            EvaluationPair p = evalPairsWithCrntQueryId.get(resultCom.getId());
            if (p != null) {
                if (cnt < 2) {
                    cnt++;
                    System.out.println(p.getComment().getText());
                }
            }
        }
    }
}
