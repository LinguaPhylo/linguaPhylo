package james.swing;

import james.Coalescent;
import james.TimeTree;
import james.core.LogNormal;
import james.graphicalModel.RandomVariable;
import james.graphicalModel.Value;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Random;

public class GraphicalModelPanel extends JPanel {

    GraphicalModelComponent component;
    JLabel selectedValueLabel = new JLabel("");

    JSplitPane splitPane;

    GraphicalModelPanel(RandomVariable variable) {
        component = new GraphicalModelComponent(variable);

        selectedValueLabel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        setLayout(new BorderLayout());

        //add(component, BorderLayout.CENTER);
        //add(selectedValueLabel, BorderLayout.EAST);

        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,component,selectedValueLabel);
        splitPane.setResizeWeight(0.75);
        add(splitPane, BorderLayout.CENTER);

        component.addGraphicalModelListener(value -> showValue(value));

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        variable.print(pw);
        pw.flush();

        JTextArea textArea = new JTextArea(sw.toString());
        textArea.setFont(new Font("monospaced", Font.PLAIN, 16));
        textArea.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        add(textArea, BorderLayout.SOUTH);
    }

    private void showValue(Value value) {
        final int size = splitPane.getDividerLocation();
        splitPane.setRightComponent(value.getViewer());
        splitPane.setDividerLocation(size);
    }

    public static void main(String[] args) {

        Random random = new Random();

        Value<Double> thetaM = new Value<>("M", 3.0);
        Value<Double> thetaS = new Value<>("S", 1.0);
        LogNormal logNormal = new LogNormal(thetaM, thetaS, random);

        RandomVariable<Double> theta = logNormal.sample("\u0398");
        Value<Integer> n = new Value<>("n", 20);

        Coalescent coalescent = new Coalescent(theta, n, random);

        RandomVariable<TimeTree> g = coalescent.sample();

        PrintWriter p = new PrintWriter(System.out);
        g.print(p);

        GraphicalModelPanel panel = new GraphicalModelPanel(g);
        panel.setPreferredSize(new Dimension(800, 800));

        JFrame frame = new JFrame("Graphical Models");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(panel, BorderLayout.CENTER);
        frame.pack();
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setLocation(dim.width/2-frame.getSize().width/2, dim.height/2-frame.getSize().height/2);
        frame.setVisible(true);

    }

}
