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
 *
 * @author Sgo
 */
public class Experiment2 extends JFrame {

    public Experiment2(String title, ArrayList<String> allModelsNames, String evalMetric) throws IOException, FileNotFoundException, ClassNotFoundException {
        super(title);

        // Create dataset
        XYDataset dataset = createDataset(allModelsNames, evalMetric);

        // Create chart
        JFreeChart chart = ChartFactory.createXYLineChart(
                evalMetric,
                "True Binary Relevance", "Estimated Relevance", dataset, PlotOrientation.VERTICAL,
                true, true, false);

        //Changes background color
        XYPlot plot = (XYPlot) chart.getPlot();
        plot.setBackgroundPaint(new Color(255, 228, 196));

        // Create Panel
        ChartPanel panel = new ChartPanel(chart);
        setContentPane(panel);
    }

    private XYDataset createDataset(ArrayList<String> allModelsNames, String evalMetric) throws IOException, FileNotFoundException, ClassNotFoundException {

        XYSeriesCollection dataset = new XYSeriesCollection();
        ArrayList<Double> model_data;

        // for all models
        for (String modelName : allModelsNames) {

            // get specified data
            if (evalMetric.equals("Precision_R")) {
                model_data = (ArrayList<Double>) Utils.getSavedObject(modelName + "_all_Precisions_R");
            } else if (evalMetric.equals("Avep")) {
                model_data = (ArrayList<Double>) Utils.getSavedObject(modelName + "_all_Aveps_R");
            } else if (evalMetric.equals("Bpref")) {
                model_data = (ArrayList<Double>) Utils.getSavedObject(modelName + "_all_Bprefs_R");
            } else {
                return null;
            }

            // create data series
            XYSeries series = new XYSeries(modelName);

            for (int i = 0; i < model_data.size(); i++) {
                series.add(i + 1, model_data.get(i));
            }

            // create dataset
            dataset.addSeries(series);
        }

        return dataset;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                // ArrayList for all models to be ploted
                ArrayList<String> allModelsNames = new ArrayList<>();
                allModelsNames.add("Baseline model (Jaccard Similarity)");
                allModelsNames.add("Wordnet model");
                allModelsNames.add("Word2vec model");
                allModelsNames.add("Word2vec and Wordnet");

                // Precision chart
                Experiment2 Precision = new Experiment2("Precision over different R cut-offs", allModelsNames, "Precision_R");
                Precision.setSize(800, 400);
                Precision.setLocationRelativeTo(null);
                Precision.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
                Precision.setVisible(true);

                // Avep chart
                Experiment2 Avep = new Experiment2("Avep over different R cut-offs", allModelsNames, "Avep");
                Avep.setSize(800, 400);
                Avep.setLocationRelativeTo(null);
                Avep.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
                Avep.setVisible(true);

                // Bpref chart
                Experiment2 Bpref = new Experiment2("Bpref over different R cut-offs", allModelsNames, "Bpref");
                Bpref.setSize(800, 400);
                Bpref.setLocationRelativeTo(null);
                Bpref.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
                Bpref.setVisible(true);

            } catch (IOException ex) {
                Logger.getLogger(Experiment1.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(Experiment1.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
    }
}
