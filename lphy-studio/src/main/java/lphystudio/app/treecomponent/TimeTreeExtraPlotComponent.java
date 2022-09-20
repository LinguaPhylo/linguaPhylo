package lphystudio.app.treecomponent;

import lphy.evolution.tree.LTTUtils;
import lphy.evolution.tree.TimeTree;
import lphy.evolution.tree.TimeTreeNode;
import lphystudio.app.Utils;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.*;
import java.util.prefs.Preferences;

/**
 * @author Walter Xie
 */
public class TimeTreeExtraPlotComponent extends JComponent {

    static Preferences preferences = Preferences.userNodeForPackage(TimeTreeExtraPlotComponent.class);

    static Font minPlotFont = new Font(Font.MONOSPACED, Font.PLAIN, Utils.MIN_FONT_SIZE);

    public static final String LTT_TITLE = "LTT Plot";
    public static final String NE_TITLE = "Ne Plot";

    final String yNeLabel = "Ne";
    final String yLTTLabel = "Lineages";
    final String xLable = "Time";

    private Rectangle2D bounds = new Rectangle2D.Double(0, 0, 1, 1);

    TimeTreeComponent treeComponent;
    XYDataset dataset;

    public TimeTreeExtraPlotComponent(TimeTreeComponent treeComponent) {
        this.treeComponent = treeComponent;
    }

    @Override
    public void paintComponent(Graphics g) {

        JFreeChart lineChart = null;
        TimeTree tree = treeComponent.getTimeTree();

//        if (isLTTPlot()) {
            dataset = computeLTT(tree);

            lineChart = ChartFactory.createXYLineChart(
                    null, xLable, yLTTLabel, dataset,
                    PlotOrientation.VERTICAL, false,true,false);
//            lineChart.setTitle(new org.jfree.chart.title.TextTitle("LTT Plot", minPlotFont));
//        } else {
//            dataset = computeNe(tree);
//
//            lineChart = ChartFactory.createXYLineChart(
//                    null, xLable, yNeLabel, dataset,
//                    PlotOrientation.VERTICAL, false,true,false);
//        }
        XYPlot plot = Objects.requireNonNull(lineChart).getXYPlot();
//            plot.getDomainAxis().setLabelFont(minPlotFont);
//            plot.getRangeAxis().setLabelFont(minPlotFont);
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
            lttMap = LTTUtils.getLTTFromUltrametricTree(tree);
        } else { //TODO wrong for birth death tree
            lttMap = new TreeMap<>();
            List<TimeTreeNode> nodes = tree.getNodes();
            // must add root first
            lttMap.put(tree.getRoot().getAge(), 2);

            // assuming the nodes list contains tips and then nodes, and the last is root.
            for (int i = nodes.size(); i-- > 0; ) {
                TimeTreeNode n = nodes.get(i);
                // all ages > or == 0
                final double age = n.getAge();
                // skip if not root and age > 0
                if (!n.isRoot() && age > 0) {

                    if (lttMap.containsKey(age)) {
                        // existing age
                        final int l = lttMap.get(age);
                        lttMap.put(age, l + 1);
                    } else if (age < lttMap.firstKey()) {
                        // new age, but younger (closer to 0) than all existing ages
                        lttMap.put(age, lttMap.firstEntry().getValue() + 1);
                    } else {
                        // new age, but between existing ages
                        for (var entry : lttMap.entrySet()) {
                            // compare to existing ages in descending order
                            final int l = lttMap.get(entry.getKey());
                            if (age > entry.getKey()) {
                                // +1 lineage for every existing ages if it is not leaf node
                                // here leaf node age > 0
                                if (!n.isLeaf())
                                    lttMap.put(entry.getKey(), l + 1);
                                else
                                    lttMap.put(entry.getKey(), l - 1);
                            } else {
                                // add this new count
                                if (!n.isLeaf())
                                    // +1 lineage if it is not leaf node
                                    lttMap.put(age, l + 1);
                                else
                                    // -1 lineage if it is leaf node
                                    lttMap.put(age, l - 1);
                                break;
                            }
                        }
                    } // end if
                } // end if isRoot
            } // end for
            System.out.println(Arrays.toString(nodes.stream().mapToDouble(TimeTreeNode::getAge).toArray()));

        } // end if isUltrametric()

        System.out.println(lttMap);

        int preY = 1;
        // ages must be descending, otherwise plot is broken
        for (var entry : lttMap.descendingMap().entrySet()) {
            // add negative sign to ages
            // draw step line for ordinal numbers
            series.add(entry.getKey() * -1, preY);
            series.add(entry.getKey() * -1, entry.getValue());
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
