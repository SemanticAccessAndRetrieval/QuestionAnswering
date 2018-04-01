/*
 *  This code belongs to the Semantic Access and Retrieval (SAR) group of the
 *  Information Systems Laboratory (ISL) of the
 *  Institute of Computer Science (ICS) of the
 *  Foundation for Research and Technology - Hellas (FORTH)
 *  Nobody is allowed to use, copy, distribute, or modify this work.
 *  It is published for reasons of research results reproducibility.
 *  (c) 2017 Semantic Access and Retrieval group, All rights reserved
 */
package gr.forth.ics.isl.demo.evaluation.charts;

import gr.forth.ics.isl.utilities.Utils;
import java.awt.Color;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

/**
 * Experiment 1: We plot the data as a function of (floating point) estimated
 * and true (binary) relevance to see how well, each model, can classify the
 * reviews.
 *
 * @author Sgo
 */
public class Experiment1 extends JFrame {

    public Experiment1(String title, String modelName) throws IOException, FileNotFoundException, ClassNotFoundException {
        super(title);

        // Create dataset
        XYDataset dataset = createDataset(modelName);

        // Create chart
        JFreeChart chart = ChartFactory.createScatterPlot(
                modelName,
                "True Binary Relevance", "Estimated Relevance", dataset, PlotOrientation.VERTICAL,
                true, true, false);

        //Changes background color
        XYPlot plot = (XYPlot) chart.getPlot();
        plot.setBackgroundPaint(new Color(255, 228, 196));

        // Create Panel
        ChartPanel panel = new ChartPanel(chart);
        setContentPane(panel);
    }

    private XYDataset createDataset(String modelName) throws IOException, FileNotFoundException, ClassNotFoundException {
        XYSeriesCollection dataset = new XYSeriesCollection();

        ArrayList<Double> model_ScoreSet = (ArrayList<Double>) Utils.getSavedObject(modelName + "_ScoreSet");
        ArrayList<Integer> model_TestSet = (ArrayList<Integer>) Utils.getSavedObject(modelName + "_TestSet");

        XYSeries seriesRel = new XYSeries("Relevant");
        XYSeries seriesIrel = new XYSeries("Irelevant");

        for (int i = 0; i < model_TestSet.size(); i++) {
            if (model_TestSet.get(i).equals(0)) {
                seriesIrel.add(model_TestSet.get(i), model_ScoreSet.get(i));
            } else {
                seriesRel.add(model_TestSet.get(i), model_ScoreSet.get(i));
            }
        }

        dataset.addSeries(seriesRel);
        dataset.addSeries(seriesIrel);

        return dataset;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                Experiment1 baseline = new Experiment1("Japanese Hotel Reviews: Estimated vs True Relevance", "Baseline model (Jaccard Similarity)");
                baseline.setSize(800, 400);
                baseline.setLocationRelativeTo(null);
                baseline.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
                baseline.setVisible(true);

                Experiment1 wordnet = new Experiment1("Japanese Hotel Reviews: Estimated vs True Relevance", "Wordnet model");
                wordnet.setSize(800, 400);
                wordnet.setLocationRelativeTo(null);
                wordnet.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
                wordnet.setVisible(true);

                Experiment1 word2vec = new Experiment1("Japanese Hotel Reviews: Estimated vs True Relevance", "Word2vec model");
                word2vec.setSize(800, 400);
                word2vec.setLocationRelativeTo(null);
                word2vec.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
                word2vec.setVisible(true);

                Experiment1 wordnet_word2vec = new Experiment1("Japanese Hotel Reviews: Estimated vs True Relevance", "Word2vec and Wordnet");
                wordnet_word2vec.setSize(800, 400);
                wordnet_word2vec.setLocationRelativeTo(null);
                wordnet_word2vec.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
                wordnet_word2vec.setVisible(true);
            } catch (IOException ex) {
                Logger.getLogger(Experiment1.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(Experiment1.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
    }
}
