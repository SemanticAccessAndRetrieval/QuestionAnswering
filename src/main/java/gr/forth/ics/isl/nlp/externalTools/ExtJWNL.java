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
 * This class is made for using the methods of extJWNL lib. Contains methods
 * like get all derived nouns of a verb.
 *
 * @author Sgo
 */
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.data.IndexWord;
import net.sf.extjwnl.data.POS;
import net.sf.extjwnl.data.Pointer;
import net.sf.extjwnl.data.PointerType;
import net.sf.extjwnl.data.Synset;
import net.sf.extjwnl.data.Word;
import net.sf.extjwnl.dictionary.Dictionary;

public class ExtJWNL {

    private final Dictionary dictionary;

    public ExtJWNL() throws JWNLException {
        this.dictionary = Dictionary.getDefaultResourceInstance();
    }

    /**
     * Returns the derived adjective with the same word form for the most common
     * sense of the given noun if exists.
     *
     * @param noun the noun
     */
    public ArrayList<String> getDerivedNouns(String verb) {
        try {
            IndexWord verbIW = this.dictionary.lookupIndexWord(POS.VERB, verb);

            List<Synset> senses = verbIW.getSenses();

            Synset mainSense = senses.get(0);

            List<Pointer> pointers = mainSense.getPointers(PointerType.DERIVATION);

            ArrayList<String> wordExt = new ArrayList<>();

            for (Pointer pointer : pointers) {
                Synset derivedSynset = pointer.getTargetSynset();

                if (derivedSynset.getPOS() == POS.NOUN) {
                    List<Word> words = derivedSynset.getWords();
                    for (int i = 0; i < words.size(); i++) {
                        String tmpWord = words.get(i).getLemma(); // get only i=0 for less results (most common sense)
                        if (!wordExt.contains(tmpWord)) {
                            wordExt.add(tmpWord);
                        }
                    }
                }
            }

            return wordExt;
        } catch (JWNLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public HashMap<String, ArrayList<String>> getDerived(HashMap<String, String> word_pos, ArrayList<String> expansionResources) {

        HashMap<String, ArrayList<String>> word_synset = new HashMap<>();
        for (String token : word_pos.keySet()) {
            String tmp_pos = word_pos.get(token);
            //Get the wordnet POS based on coreNLP POS
            edu.mit.jwi.item.POS tmp_word_pos = WordNet.getWordNetPos(tmp_pos);
            HashSet<String> tmp = new HashSet<>();
            // an 8eloume ta lemma? logika..
            if (expansionResources.contains("lemma")) {
                tmp.add(token);
            }

            if (tmp_word_pos == null) {
                ArrayList<String> syn = new ArrayList<>();
                syn.addAll(tmp);
                word_synset.put(token, syn);
                continue;
            }

            // epipleon elegxos an 8eloume ta verbs antistoixa kai sto allo
            if (tmp_word_pos.toString().equalsIgnoreCase("verb") && expansionResources.contains("verb")) {
                try {
                    IndexWord verbIW = this.dictionary.lookupIndexWord(POS.VERB, token);

                    if (verbIW == null) {
                        ArrayList<String> syn = new ArrayList<>();
                        syn.addAll(tmp);
                        word_synset.put(token, syn);
                        continue;
                    }

                    List<Synset> senses = verbIW.getSenses();

                    Synset mainSense = senses.get(0);

                    List<Pointer> pointers = mainSense.getPointers(PointerType.DERIVATION);

                    for (Pointer pointer : pointers) {
                        Synset derivedSynset = pointer.getTargetSynset();
                        if (derivedSynset.getPOS() == POS.ADJECTIVE) {
                            //System.out.println(derivedSynset.getWords());
                            // tmp.add(derivedSynset.getWords().get(0).getLemma());
                        }
                        if (derivedSynset.getPOS() == POS.NOUN) {
                            //System.out.println(derivedSynset.getWords());
                            String tmp_noun = derivedSynset.getWords().get(0).getLemma().toLowerCase();
                            for (String word : tmp_noun.split(" ")) {
                                tmp.add(word.trim());
                            }
                            //tmp.add(derivedSynset.getWords().get(0).getLemma().toLowerCase());
                        }
                    }

                    ArrayList<String> syn = new ArrayList<>();
                    syn.addAll(tmp);
                    word_synset.put(token, syn);
                } catch (JWNLException ex) {
                    Logger.getLogger(ExtJWNL.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else if (tmp_word_pos.toString().equalsIgnoreCase("noun") && expansionResources.contains("noun")) {
                try {
                    IndexWord nounIW = this.dictionary.lookupIndexWord(POS.NOUN, token);

                    if (nounIW == null) {
                        ArrayList<String> syn = new ArrayList<>();
                        syn.addAll(tmp);
                        word_synset.put(token, syn);
                        continue;
                    }

                    List<Synset> senses = nounIW.getSenses();

                    Synset mainSense = senses.get(0);

                    List<Pointer> pointers = mainSense.getPointers(PointerType.DERIVATION);

                    for (Pointer pointer : pointers) {
                        Synset derivedSynset = pointer.getTargetSynset();
                        if (derivedSynset.getPOS() == POS.ADJECTIVE) {
                            //System.out.println(derivedSynset.getWords());
                            // tmp.add(derivedSynset.getWords().get(0).getLemma());
                        }
                        if (derivedSynset.getPOS() == POS.VERB) {
                            //System.out.println(derivedSynset.getWords());
                            String tmp_verb = derivedSynset.getWords().get(0).getLemma().toLowerCase();
                            for (String word : tmp_verb.split(" ")) {
                                tmp.add(word.trim());
                            }

                            //tmp.add(derivedSynset.getWords().get(0).getLemma().toLowerCase());
                        }
                    }

                    ArrayList<String> syn = new ArrayList<>();
                    syn.addAll(tmp);
                    word_synset.put(token, syn);
                } catch (JWNLException ex) {
                    Logger.getLogger(ExtJWNL.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                ArrayList<String> syn = new ArrayList<>();
                syn.addAll(tmp);
                word_synset.put(token, syn);
            }
        }
        return word_synset;
    }

    public static void main(String[] args) throws FileNotFoundException, JWNLException, CloneNotSupportedException {
        ExtJWNL jwnl = new ExtJWNL();
        System.out.println(jwnl.getDerivedNouns("die"));
    }
}
