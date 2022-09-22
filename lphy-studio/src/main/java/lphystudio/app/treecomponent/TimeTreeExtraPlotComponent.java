package lphystudio.app.treecomponent;

import lphy.evolution.tree.LTTUtils;
import lphy.evolution.tree.TimeTree;
import lphystudio.app.Utils;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.prefs.Preferences;

/**
 * @author Walter Xie
 */
public class TimeTreeExtraPlotComponent extends JComponent {

    static Preferences preferences = Preferences.userNodeForPackage(TimeTreeExtraPlotComponent.class);

    static Font axisLabelFont = new Font(Font.MONOSPACED, Font.PLAIN, (Utils.MIN_FONT_SIZE+Utils.MAX_FONT_SIZE)/2);

    public static final String LTT_TITLE = "LTT Plot";
    public static final String NE_TITLE = "Ne Plot";

    final String yNeLabel = "Ne";
    final String yLTTLabel = "Lineages";
    final String xLable = "Time";

    private Rectangle2D bounds = new Rectangle2D.Double(0, 0, 1, 1);

    final TimeTreeComponent treeComponent;
    final XYDataset lttData;

    public TimeTreeExtraPlotComponent(TimeTreeComponent treeComponent) {
        this.treeComponent = treeComponent;
        TimeTree tree = treeComponent.getTimeTree();
        // compute data points here, not when paintComponent
//        if (isLTTPlot()) {
        this.lttData = computeLTT(tree);
//        } else {
//            neData = computeNe(tree);
//        }
    }

    @Override
    public void paintComponent(Graphics g) {

        JFreeChart lineChart = null;

//        if (isLTTPlot()) {
            lineChart = ChartFactory.createXYLineChart(
                    null, xLable, yLTTLabel, lttData,
                    PlotOrientation.VERTICAL, false,true,false);
//            lineChart.setTitle(new org.jfree.chart.title.TextTitle("LTT Plot", minPlotFont));
//        } else {
//            neData = computeNe(tree);
//
//            lineChart = ChartFactory.createXYLineChart(
//                    null, xLable, yNeLabel, neData,
//                    PlotOrientation.VERTICAL, false,true,false);
//        }
        XYPlot plot = Objects.requireNonNull(lineChart).getXYPlot();
        plot.getDomainAxis().setLabelFont(axisLabelFont);
        plot.getRangeAxis().setLabelFont(axisLabelFont);
        // y axis is integer
        plot.getRangeAxis().setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        plot.setBackgroundPaint(Color.white);
        plot.setRangeGridlinesVisible(true);
        plot.setRangeGridlinePaint(Color.BLACK);
        plot.setDomainGridlinesVisible(true);
        plot.setDomainGridlinePaint(Color.BLACK);

        Graphics2D g2d = (Graphics2D) g;

        Insets insets = getInsets();
        g.translate(insets.left, insets.top);
        int width = getWidth() - insets.left - insets.right;
        int height = getHeight() - insets.top - insets.bottom;

        // height is the tree view height now
        Rectangle2D available = new Rectangle2D.Double(0, 0, width, height);
        setBounds(available);

//        AffineTransform saved = g2d.getTransform();
//        AffineTransform st = AffineTransform.getScaleInstance( 1.0, 0.75 );
//        g2d.transform(st);
        Objects.requireNonNull(lineChart).draw(g2d, available);
//        g2d.setTransform(saved);
    }

    public void setBounds(Rectangle2D bounds) {
        this.bounds = bounds;
    }

    private XYDataset computeLTT(TimeTree tree) {
        if (tree.leafCount() < 3)
            throw new IllegalArgumentException("It is not a legal time tree !\n" +
                    "leaf count = " + tree.leafCount() + ", tree = " + tree);

        XYSeriesCollection dataset = new XYSeriesCollection();
        XYSeries series = new XYSeries("LTT");

        // ordered age , lineages
        NavigableMap<Double, Integer> lttMap;

        if (tree.isUltrametric()) {
            // faster
            lttMap = LTTUtils.getLTTFromUltrametricTree(tree);
        } else {
            //TODO wrong for birth death tree
            lttMap = LTTUtils.getLTTFromTimeTree(tree);
        }

        System.out.println(lttMap);

        int preY = 1;
        double age;
        // ages must be descending, otherwise plot is broken
        for (var entry : lttMap.descendingMap().entrySet()) {
            // add negative sign to ages
            // draw step line for ordinal numbers
            age = entry.getKey();
            series.add(age * -1, preY);
            // do not draw vertical line in x==0
            if (age > 0)
                series.add(age * -1, entry.getValue());
            preY = entry.getValue();
        }
        dataset.addSeries(series);

        return dataset;
    }

    private XYDataset computeNe(TimeTree tree) {
        if (tree.leafCount() < 3 && tree.isUltrametric())
            throw new IllegalArgumentException("It is not a legal time tree !\nisUltrametric = " +
                    tree.isUltrametric() + ", leaf count = " + tree.leafCount() + ", tree = " + tree);

        throw new UnsupportedOperationException("in dev");
//        XYSeriesCollection dataset = new XYSeriesCollection();
//        XYSeries series = new XYSeries("Ne");
//
//        dataset.addSeries(series);
//
//        return dataset;
    }


    public TimeTree getTimeTree() {
        return Objects.requireNonNull(treeComponent).getTimeTree();
    }

    public static boolean isShowExtraPlot() {
        return preferences.getBoolean("showExtraPlot", false);
    }
    public static void setShowExtraPlot(boolean showPlot) {
        preferences.putBoolean("showExtraPlot", showPlot);
    }

    public static boolean isLTTPlot() {
        return preferences.getBoolean("isLTT", true);
    }
    public static void setLTTPlot(boolean isLTTPlot) {
        preferences.putBoolean("isLTT", isLTTPlot);
    }

}
