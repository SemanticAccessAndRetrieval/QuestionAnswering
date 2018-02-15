/*
 *  This code belongs to the Semantic Access and Retrieval (SAR) group of the
 *  Information Systems Laboratory (ISL) of the
 *  Institute of Computer Science (ICS) of the
 *  Foundation for Research and Technology - Hellas (FORTH)
 *  Nobody is allowed to use, copy, distribute, or modify this work.
 *  It is published for reasons of research results reproducibility.
 *  (c) 2017 Semantic Access and Retrieval group, All rights reserved
 */
package gr.forth.ics.isl.nlp.externalTools;

/**
 *
 * @author Sgo
 */
import edu.mit.jwi.Dictionary;
import edu.mit.jwi.IDictionary;
import edu.mit.jwi.item.IIndexWord;
import edu.mit.jwi.item.IPointer;
import edu.mit.jwi.item.ISynset;
import edu.mit.jwi.item.ISynsetID;
import edu.mit.jwi.item.IWord;
import edu.mit.jwi.item.IWordID;
import edu.mit.jwi.item.POS;
import edu.mit.jwi.item.Pointer;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * This class is used to support our own wordnet API
 *
 * @author Sgo
 */
public class WordNet {

    /**
     * This method is used to retrieve all antonyms of the input word.
     *
     * @param dict dictionary to be used
     * @param inputWord word for which we will search for antonyms
     * @param pos part of speech tag of the input word
     * @throws IOException
     */
    public static HashSet<String> getAntonyms(IDictionary dict, String inputWord, POS pos) throws IOException {

        HashSet<String> antonyms = new HashSet<>();

        // get index word index
        IIndexWord idxWord = dict.getIndexWord(inputWord, pos);
        // get word id
        List<IWordID> wordIDs = idxWord.getWordIDs();
        for (int i = 0; i < wordIDs.size(); i++) {
            IWordID wordID = wordIDs.get(i);
            // get word
            IWord word = dict.getWord(wordID);
            // get related words
            Map<IPointer, List<IWordID>> words = word.getRelatedMap();
            for (IPointer wp : words.keySet()) {
                //System.out.println(wp.getSymbol());
                //System.out.println(wp.getName());
                for (IWordID wid : words.get(wp)) {
                    // if word is an antonym
                    if (wp.getName().equals("Antonym")) {
                        antonyms.add(dict.getWord(wid).getLemma());
                        //System.out.println(dict.getWord(wid).getLemma());
                    }
                }
            }
        }

        return antonyms;
    }

    /**
     * This method is used to retrieve all hypernyms of the input word.
     *
     * @param dict dictionary to be used
     * @param inputWord word for which we will search for hypernyms
     * @param pos part of speech tag of the input word
     * @throws IOException
     */
    public static HashSet<String> getHypernyms(IDictionary dict, String inputWord, POS pos) {

        HashSet<String> hypernyms = new HashSet<>();

        // get  the  synset
        IIndexWord idxWord = dict.getIndexWord(inputWord, pos);
        List<IWordID> wordIDs = idxWord.getWordIDs();
        for (int i = 0; i < wordIDs.size(); i++) {
            IWordID wordID = wordIDs.get(i);
            IWord word = dict.getWord(wordID);
            ISynset synset = word.getSynset();
            // get  the  related synsets
            List<ISynsetID> relSynsets = synset.getRelatedSynsets(Pointer.HYPERNYM);
            //  collect  each  hypernym s  id and  synonyms
            List<IWord> words;
            for (ISynsetID sid : relSynsets) {
                words = dict.getSynset(sid).getWords();
                for (Iterator<IWord> j = words.iterator(); j.hasNext();) {
                    hypernyms.add(j.next().getLemma());
                }
            }
        }

        return hypernyms;
    }

    /**
     * This method is used to retrieve all synonyms of the input word.
     *
     * @param dict dictionary to be used
     * @param inputWord word for which we will search for synonyms
     * @param pos part of speech tag of the input word
     * @throws IOException
     */
    public static HashSet<String> getSynonyms(IDictionary dict, String inputWord, POS pos) {

        HashSet<String> synonyms = new HashSet<>();

        // look up  first  sense of the input word
        IIndexWord idxWord = dict.getIndexWord(inputWord, pos);
        List<IWordID> wordIDs = idxWord.getWordIDs();
        for (int i = 0; i < wordIDs.size(); i++) {
            IWordID wordID = wordIDs.get(i);
            IWord word = dict.getWord(wordID);
            ISynset synset = word.getSynset();
            //  iterate  over  words  associated  with  the  synset
            for (IWord w : synset.getWords()) {
                synonyms.add(w.getLemma());
                //System.out.println(w.getLemma());
            }
        }

        return synonyms;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        String wnhome = System.getenv("WNHOME");
        String path = wnhome + File.separator + "dict";
        URL url = new URL("file", null, path);

        // construct the dictionary object and open it
        IDictionary dict = new Dictionary(url);
        dict.open();

        // WordNet stemmer
        //WordnetStemmer stemmer = new WordnetStemmer(dict);
        //SimpleStemmer simpStem = new SimpleStemmer();
        //System.out.println(simpStem.findStems("noise", POS.NOUN));
        //System.out.println(stemmer.findStems("noisy", null));
        System.out.println("SYNONYMS");
        System.out.println("========");
        System.out.println(getSynonyms(dict, "noisy", POS.ADJECTIVE));
        System.out.println("HYPERNYMS");
        System.out.println("=========");
        System.out.println(getHypernyms(dict, "noisy", POS.ADJECTIVE));
        System.out.println("ANTONYMS");
        System.out.println("========");
        System.out.println(getAntonyms(dict, "noisy", POS.ADJECTIVE));
    }

}
