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
import java.util.List;
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

    public static void main(String[] args) throws FileNotFoundException, JWNLException, CloneNotSupportedException {
        ExtJWNL jwnl = new ExtJWNL();
        System.out.println(jwnl.getDerivedNouns("die"));
    }
}
