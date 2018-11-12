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

import gr.forth.ics.isl.main.demo_main;
import gr.forth.ics.isl.nlp.models.Comment;
import gr.forth.ics.isl.sailInfoBase.QAInfoBase;
import gr.forth.ics.isl.sailInfoBase.models.Subject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import org.datavec.api.util.ClassPathResource;
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.paragraphvectors.ParagraphVectors;
import org.deeplearning4j.text.documentiterator.FileLabelAwareIterator;
import org.deeplearning4j.text.documentiterator.LabelAwareIterator;
import org.deeplearning4j.text.tokenization.tokenizer.preprocessor.CommonPreprocessor;
import org.deeplearning4j.text.tokenization.tokenizerfactory.DefaultTokenizerFactory;
import org.deeplearning4j.text.tokenization.tokenizerfactory.TokenizerFactory;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.ops.transforms.Transforms;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Sgo
 */
public class Doc2vecModel extends Model {

    private ParagraphVectors paragraphVectors;
    private LabelAwareIterator iterator;
    private TokenizerFactory tokenizerFactory;
    private ClassPathResource resourceLabeled;
    private ClassPathResource resource;
    //private ArrayList<Comment> comments;

    private static final Logger log = LoggerFactory.getLogger(Doc2vecModel.class);

    public Doc2vecModel(String description, ParagraphVectors paragraphVectors, TokenizerFactory tokenizerFactory, ClassPathResource resource, ArrayList<Comment> comments) {
        this.paragraphVectors = paragraphVectors;
        this.iterator = iterator;
        this.tokenizerFactory = tokenizerFactory;
        this.resourceLabeled = resourceLabeled;
        this.resource = resource;
        this.setComments(comments);
        this.setDescription(description);
    }

    public Doc2vecModel(String description, String resourceLabeledPath, ArrayList<Comment> comments) throws Exception {
        super.setComments(comments);
        this.resourceLabeled = new ClassPathResource(resourceLabeledPath);
        this.makeParagraphVectors(resourceLabeled);
        this.resource = new ClassPathResource("/trainedModels/doc2vec");
        this.setComments(comments);
        this.setDescription(description);
    }

    public void makeParagraphVectors(ClassPathResource resourceLabeled) throws Exception {
        ClassPathResource resource = resourceLabeled;

        // build a iterator for our dataset
        this.iterator = new FileLabelAwareIterator.Builder()
                .addSourceFolder(resource.getFile())
                .build();

        this.tokenizerFactory = new DefaultTokenizerFactory();
        this.tokenizerFactory.setTokenPreProcessor(new CommonPreprocessor());

        // ParagraphVectors training configuration
        this.paragraphVectors = new ParagraphVectors.Builder()
                .learningRate(0.025)
                .minLearningRate(0.001)
                .batchSize(1000)
                .epochs(20)
                .iterate(this.iterator)
                .trainWordVectors(true)
                .tokenizerFactory(this.tokenizerFactory)
                .build();

        // Start model training
        this.paragraphVectors.fit();
        //System.out.println(this.paragraphVectors.getConfiguration().getIterations());
        WordVectorSerializer.writeParagraphVectors(this.paragraphVectors, "src/main/resources/trainedModels/doc2vec");
    }

    public String getSuffix(String uri) {
        String[] uriParts = uri.split("#");
        String suffix = uriParts[1];
        return suffix;
    }

    @Override
    public void scoreComments(String query) {
        this.paragraphVectors.setTokenizerFactory(this.tokenizerFactory);
        this.paragraphVectors.getConfiguration().setIterations(1); // please note, we set iterations to 1 here, just to speedup inference
        Collection<String> nearestLabels = this.paragraphVectors.nearestLabels(query, this.getComments().size());
        INDArray queryVec = this.paragraphVectors.inferVector(query);
        //ArrayList<Comment> resultComs = new ArrayList<>();
        for (String nl : nearestLabels) {
            for (Comment cm : this.getComments()) {
                //System.out.println(nl + " " + getSuffix(cm.getId()));
                if (nl.equals(getSuffix(cm.getId()))) {
                    //resultComs.add(cm);
                    INDArray vecLabel = this.paragraphVectors.getLookupTable().vector(nl);
                    if (vecLabel == null) {
                        throw new IllegalStateException("Label '" + nl + "' has no known vector!");
                    }
                    double sim = Transforms.cosineSim(vecLabel, queryVec);
                    cm.setScore(sim);
                    //System.out.println(nl + "Score: " + sim);
                }
            }
        }
    }

    @Override
    public void scoreComment(Comment com, String query) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public static void main(String[] args) throws Exception {

        QAInfoBase KB = new QAInfoBase();
        HashSet<Subject> hotels = KB.getAllSubjectsOfType("hippalus", "hippalusID");
        ArrayList<Comment> coms = demo_main.getComments(hotels, KB);

        System.out.println("\n TRAINING \n");

        String resourceLabeledPath = "paravec/labeled";
        Doc2vecModel d2v = new Doc2vecModel("Doc2VecModel", resourceLabeledPath, coms);
        d2v.scoreComments("Has anyone reported a problem about noise?");
        System.out.println(d2v.getTopComments(10));

        System.out.println("\n PRE-TRAINED \n");

        ClassPathResource resource = new ClassPathResource("/trainedModels/doc2vec");
        TokenizerFactory t = new DefaultTokenizerFactory();
        t.setTokenPreProcessor(new CommonPreprocessor());

        // we load externally originated model
        ParagraphVectors vectors = WordVectorSerializer.readParagraphVectors(resource.getFile());
        vectors.setTokenizerFactory(t);
        vectors.getConfiguration().setIterations(20); // please note, we set iterations to 1 here, just to speedup inference
        Doc2vecModel d2vPreTrained = new Doc2vecModel("Doc2VecModel Pretrained", vectors, t, resource, coms);
        d2vPreTrained.scoreComments("Has anyone reported a problem about noise?");
        System.out.println(d2vPreTrained.getTopComments(10));
    }

}
