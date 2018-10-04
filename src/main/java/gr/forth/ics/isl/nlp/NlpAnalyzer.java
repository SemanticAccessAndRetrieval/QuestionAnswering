/* 
 *  This code belongs to the Semantic Access and Retrieval (SAR) group of the 
 *  Information Systems Laboratory (ISL) of the 
 *  Institute of Computer Science (ICS) of the  
 *  Foundation for Research and Technology - Hellas (FORTH)
 *  Nobody is allowed to use, copy, distribute, or modify this work.
 *  It is published for reasons of research results reproducibility.
 *  (c) 2017 Semantic Access and Retrieval group, All rights reserved
 */









package gr.forth.ics.isl.nlp;


import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import gr.forth.ics.isl.demoCombined.main.combinedDemoMain;
import gr.forth.ics.isl.nlp.models.RelatedSentences;
import gr.forth.ics.isl.nlp.models.Word;
import gr.forth.ics.isl.utilities.StringUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

/**
 *
 * @author Lefteris Dimitrakis
 */
public class NlpAnalyzer {

    /**
     * This function is used to split the text into distinct sentences.
     *
     * @param text. A given text.
     * @return an ArrayList that contains the distinct sentences of the text.
     */
    public static ArrayList<String> getSentences(String text) {
        Properties props = new Properties();

        props.put("annotators", "tokenize, ssplit");
        props.put("tokenize.language", "en");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

        //apply
        Annotation document = new Annotation(text);
        pipeline.annotate(document);

        List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);

        ArrayList<String> string_sentences = new ArrayList<>();

        //Add in the arrayList all the extracted sentences
        for (CoreMap sentence : sentences) {
            string_sentences.add(sentence.toString());
        }

        //Return an Array with these sentences
        return string_sentences;

    }

    /**
     * This function is used to split the text into distinct tokens. After that,
     * we recognize their Named Entity type, the Part-of-Speech and we put them
     * inside, an ArrayList<Word>. Word, is a class that holds the following
     * information: the text of the current word, its POS, NER tags and the id
     * of the . sentence contains those words.
     *
     * @param text. A given text.
     * @return An ArrayList of type Word.
     */
    public static ArrayList<Word> getWordsWithPosNer(String text) {
        Properties props = new Properties();

        props.put("annotators", "tokenize, ssplit, pos, lemma, ner");
        props.put("tokenize.language", "en");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

        //apply
        Annotation document = new Annotation(text);
        pipeline.annotate(document);

        List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);

        ArrayList<Word> word_pos_ner = new ArrayList<>();

        String tmp_word = "";
        String tmp_ner = "";
        String tmp_pos = "";
        int sentence_id = 0;

        //For each sentence
        for (CoreMap sentence : sentences) {
            //For each word in the sentence
            for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {

                //Get the TEXT of the token
                String word = token.get(CoreAnnotations.TextAnnotation.class);

                //Get the NER tag of the token
                String ner = token.get(CoreAnnotations.NamedEntityTagAnnotation.class);

                //Get the POS tag of the token
                String pos = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);

                //If it is the first found word
                //Initiate the tmp_variables
                if (tmp_word.equals("") /*&& !ner.equals("O")*/) {
                    tmp_ner = ner;
                    tmp_pos = pos;
                    tmp_word = word;
                } else {
                    //If the Named Entities Tags of consecutive words match: concatenate them
                    if (tmp_ner.equals(ner)) {
                        tmp_word += " " + word;
                        //ELSE add the current words in the ArrayList and set as tmp the current word
                    } else {
                        word_pos_ner.add(new Word(tmp_word, tmp_pos, tmp_ner, sentence_id));
                        tmp_word = word;
                        tmp_pos = pos;
                        tmp_ner = ner;
                    }
                }

            }
            //If we move to the next sentence
            word_pos_ner.add(new Word(tmp_word, tmp_pos, tmp_ner, sentence_id));
            tmp_word = "";
            tmp_pos = "";
            tmp_ner = "";
            sentence_id++;
        }

        return word_pos_ner;

    }
    
    public static ArrayList<Word> getUniqWordsWithPosNer(String text) {
        Properties props = new Properties();

        props.put("annotators", "tokenize, ssplit, pos, lemma, ner");
        props.put("tokenize.language", "en");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

        //apply
        Annotation document = new Annotation(text);
        pipeline.annotate(document);

        List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);

        ArrayList<Word> word_pos_ner = new ArrayList<>();

        int sentence_id = 0;

        //For each sentence
        for (CoreMap sentence : sentences) {
            //For each word in the sentence
            for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {

                //Get the TEXT of the token
                String word = token.get(CoreAnnotations.TextAnnotation.class);

                //Get the NER tag of the token
                String ner = token.get(CoreAnnotations.NamedEntityTagAnnotation.class);

                //Get the POS tag of the token
                String pos = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);

                word_pos_ner.add(new Word(word, pos, ner, sentence_id));

            }
            sentence_id++;
        }

        return word_pos_ner;

    }

    /**
     * This function is responsible to return all the related sentences (and the
     * associated Words) from a range a sentences wrt the answer type of the
     * given question.
     *
     * We assume that a sentence is related to the question iff at least one of
     * it's words (or consecutive words) has the same NER type with the expected
     * NER type of the question.
     *
     * @param type. The answer type (NER).
     * @param words. ArrayList with all the Words (with info) in the paragraph.
     * @param sentences. The distinct sentences of the current paragraph.
     * @return HashMap<String,ArrayList<Word>>, HashMap of the form: key =
     * related_sentence, value = ArrayList with all words of the sentence that
     * have the expected Ner.
     */
    //public static HashMap<String, ArrayList<Word>> getRelatedSentencesAndWords(String type, ArrayList<Word> words, ArrayList<String> sentences) {
    public static RelatedSentences getRelatedSentencesAndWords(String type, ArrayList<Word> words, ArrayList<String> sentences) {

        HashMap<String, ArrayList<Word>> sentencesWithWords = new HashMap<>();

        for (Word word : words) {
            //If the Ner of the current word match with the expected Ner of the question's answer
            if (word.getNer().equals(type)) {
                //Add the sentence that contains the particular word, in the related sentences
                
                String sentence = sentences.get(word.getSentenceId());

                //If the current sentence is not already in the HasMap
                if (!sentencesWithWords.containsKey(sentence)) {
                    ArrayList<Word> tmp = new ArrayList<>();
                    tmp.add(word);
                    sentencesWithWords.put(sentence, tmp);
                } else {
                    ArrayList<Word> tmp = sentencesWithWords.get(sentence);
                    tmp.add(word);
                    sentencesWithWords.put(sentence, tmp);
                }
            }
        }
        return new RelatedSentences(sentencesWithWords);
        //return sentencesWithWords;
    }

    /*
    public static ArrayList<String> getCleanTokensUIMA(String text) {
        try {
            TokenizerFactory tokenizerFactory = new UimaTokenizerFactory();
            Tokenizer tokenizer = tokenizerFactory.create(text);

            //iterate over the tokens
            while (tokenizer.hasMoreTokens()) {
                String token = tokenizer.nextToken();
            }

        } catch (ResourceInitializationException ex) {
            Logger.getLogger(NlpAnalyzer.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }*/

    // Apply: 1) Tokenization 2) Lemmatization 3) Remove punctuations 4) Remove stopwords
    public static ArrayList<String> getCleanTokens(String text) {
//        Properties props = new Properties();
//
//        props.put("annotators", "tokenize, ssplit, pos, lemma");
//        props.put("tokenize.language", "en");
//        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
        //apply
        Annotation document = new Annotation(text);
        combinedDemoMain.pipeline.annotate(document);
        //pipeline.annotate(document);

        List<CoreLabel> tokens = document.get(CoreAnnotations.TokensAnnotation.class);

        ArrayList<String> final_tokens = new ArrayList<>();

        String tmp_token = "";
        for (CoreLabel tok : tokens) {
            tmp_token = tok.get(CoreAnnotations.LemmaAnnotation.class).replaceAll("[^a-zA-Z ]", "").toLowerCase().trim();
            if (!tmp_token.isEmpty() && !StringUtils.isStopWord(tmp_token)) {
                final_tokens.add(tmp_token);
            }
        }

        return final_tokens;
    }

    public static HashMap<String, String> getCleanTokensWithPos(String text) {
        //Properties props = new Properties();

        //props.put("annotators", "tokenize, ssplit, pos, lemma, ner");
        //props.put("tokenize.language", "en");
        //StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

        //apply
        Annotation document = new Annotation(text);
        combinedDemoMain.pipeline.annotate(document);

        List<CoreLabel> tokens = document.get(CoreAnnotations.TokensAnnotation.class);

        HashMap<String, String> final_tokens = new HashMap<>();

        String tmp_token = "";
        for (CoreLabel tok : tokens) {
            //tmp_token = tok.value().replaceAll("[^a-zA-Z ]", "").toLowerCase().trim();
            tmp_token = tok.get(CoreAnnotations.LemmaAnnotation.class).replaceAll("[^a-zA-Z ]", "").toLowerCase().trim();
            if (!tmp_token.isEmpty() && !StringUtils.isStopWord(tmp_token)) {
                //Get the POS tag of the token
                String pos = tok.get(CoreAnnotations.PartOfSpeechAnnotation.class);
                final_tokens.put(tmp_token, pos);

            }

        }

        return final_tokens;

    }

        //To implement
    public static void scoreRelatedSentences(HashMap<String, ArrayList<Word>> sentences){
    
            System.out.println(sentences);
        
    }

}
