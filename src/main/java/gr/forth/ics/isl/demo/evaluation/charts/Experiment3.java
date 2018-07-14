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
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Stroke;
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
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

/**
 *
 * @author Sgo
 */
public class Experiment3 extends JFrame {

    public Experiment3(String title, ArrayList<String> allModelsNames, String evalMetric, String collectionName) throws IOException, FileNotFoundException, ClassNotFoundException {
        super(title);

        // Create dataset
        XYDataset dataset = createDataset(allModelsNames, evalMetric, collectionName);

        // Create chart
        JFreeChart chart = ChartFactory.createXYLineChart(
                evalMetric,
                "Recall Level", evalMetric, dataset, PlotOrientation.VERTICAL,
                true, true, false);

        //Changes background color
        XYPlot plot = (XYPlot) chart.getPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.getDomainAxis().setLabelFont(new Font("", 20, 20));
        plot.getRangeAxis().setLabelFont(new Font("", 20, 20));

        setLineStyle(dataset, plot);

        // Create Panel
        ChartPanel panel = new ChartPanel(chart, true);
        setContentPane(panel);
    }

    private void setLineStyle(XYDataset dataset, XYPlot plot) {
        XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) plot.getRenderer();

        if (dataset != null) {
            for (int i = 0; i < dataset.getSeriesCount(); i++) {
                Stroke stroke = null;
                // put your own stroke definition for the given 'i' series below:
                //Stroke stroke = new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 10f, new float[]{10f, 10f}, 0f);
                if (i == 0) {
                    stroke = new BasicStroke(1f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 10f, new float[]{1f, 1f}, 4f);
                } else if (i == (dataset.getSeriesCount() - 1)) {
                    stroke = new BasicStroke(4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 10f, new float[]{1f, 1f}, 4f);
                } else {
                    stroke = new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, i * 6f, new float[]{(i * 10f) - 4f, 5f}, 4f);
                }
//                switch (i) {
//                    case 0:
//                        renderer.setSeriesShape(i, ShapeUtilities.createDownTriangle(2));
//                        break;
//                    case 1:
//                        renderer.setSeriesShape(i, ShapeUtilities.createRegularCross(2, 2));
//                        break;
//                    case 2:
//                        renderer.setSeriesShape(i, ShapeUtilities.createDiamond(2));
//                        break;
//                    case 3:
//                        renderer.setSeriesShape(i, ShapeUtilities.createDiagonalCross(2, 2));
//                        break;
//                    default:
//                        break;
//                }
                //renderer.setSeriesShapesVisible(i, true);
                renderer.setSeriesPaint(i, Color.BLACK);
                renderer.setSeriesStroke(i, stroke);
                renderer.setSeriesLinesVisible(i, true);
                renderer.setBaseLegendTextFont(new Font("", 20, 20));
            }
        }
    }

    private XYDataset createDataset(ArrayList<String> allModelsNames, String evalMetric, String collectionName) throws IOException, FileNotFoundException, ClassNotFoundException {

        XYSeriesCollection dataset = new XYSeriesCollection();
        ArrayList<Double> model_data;

        // for all models
        for (String modelName : allModelsNames) {

            // get specified data
            if (evalMetric.equals("R-Precision")) {
                model_data = (ArrayList<Double>) Utils.getSavedObject(modelName + "_all_Precisions_R_" + collectionName);
            } else if (evalMetric.equals("AveP")) {
                model_data = (ArrayList<Double>) Utils.getSavedObject(modelName + "_all_Aveps_R_" + collectionName);
            } else if (evalMetric.equals("Bpref")) {
                model_data = (ArrayList<Double>) Utils.getSavedObject(modelName + "_all_Bprefs_R_" + collectionName);
            } else if (evalMetric.equals("nDCG")) {
                model_data = (ArrayList<Double>) Utils.getSavedObject(modelName + "_all_nDCGs_R_" + collectionName);
            } else {
                return null;
            }

            String modelNameAbr = "";
            if (modelName.equals("Baseline model (Jaccard Similarity)")) {
                modelNameAbr = "I";
            } else if (modelName.equals("Wordnet model")) {
                modelNameAbr = "II";
            } else if (modelName.equals("Word2vec model")) {
                modelNameAbr = "III";
            } else if (modelName.equals("Word2vec and Wordnet")) {
                modelNameAbr = "IV";
            }

            // create data series
            XYSeries series = new XYSeries(modelNameAbr);

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
                String collectionName = "FRUCE_v2";
                //String collectionName = "BookingEvalCollection";
                //String collectionName = "webAP";

                // ArrayList for all models to be ploted
                ArrayList<String> allModelsNames = new ArrayList<>();
                allModelsNames.add("Baseline model (Jaccard Similarity)");
                allModelsNames.add("Wordnet model");
                allModelsNames.add("Word2vec model");
                //allModelsNames.add("Word2vec model II");
                allModelsNames.add("Word2vec and Wordnet");
                //allModelsNames.add("Word2vec and Wordnet II");

                // Precision chart
                Experiment3 Precision = new Experiment3("Precision over different R cut-offs", allModelsNames, "R-Precision", collectionName);
                Precision.setSize(800, 400);
                Precision.setLocationRelativeTo(null);
                Precision.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
                Precision.setVisible(true);

                // Avep chart
                Experiment3 Avep = new Experiment3("AveP over different R cut-offs", allModelsNames, "AveP", collectionName);
                Avep.setSize(800, 400);
                Avep.setLocationRelativeTo(null);
                Avep.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
                Avep.setVisible(true);

                // Bpref chart
                Experiment3 Bpref = new Experiment3("Bpref over different R cut-offs", allModelsNames, "Bpref", collectionName);
                Bpref.setSize(800, 400);
                Bpref.setLocationRelativeTo(null);
                Bpref.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
                Bpref.setVisible(true);

                // nDCG chart
                Experiment3 nDCG = new Experiment3("nDCG over different R cut-offs", allModelsNames, "nDCG", collectionName);
                nDCG.setSize(800, 400);
                nDCG.setLocationRelativeTo(null);
                nDCG.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
                nDCG.setVisible(true);

            } catch (IOException ex) {
                Logger.getLogger(Experiment2.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(Experiment2.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
    }
}
