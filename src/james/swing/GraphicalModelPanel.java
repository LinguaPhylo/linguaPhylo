package james.swing;

import james.graphicalModel.GraphicalModelParser;
import james.graphicalModel.*;

import javax.swing.*;
import javax.swing.text.html.HTMLDocument;
import java.awt.*;
import java.util.*;

public class GraphicalModelPanel extends JPanel {

    GraphicalModelComponent component;
    JLabel dummyLabel = new JLabel("");
    GraphicalModelInterpreter intepreter;
    HTMLDocument document;

    GraphicalModelParser parser;

    JButton sampleButton = new JButton("Sample");

    JSplitPane splitPane;

    Viewable displayedElement;

    GraphicalModelPanel(GraphicalModelParser parser) {

        this.parser = parser;
        intepreter = new GraphicalModelInterpreter(parser);

        component = new GraphicalModelComponent(parser);

        setLayout(new BorderLayout());

        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, component, dummyLabel);
        splitPane.setResizeWeight(0.5);
        add(splitPane, BorderLayout.CENTER);

        GraphicalModelListener listener = new GraphicalModelListener() {
            @Override
            public void valueSelected(Value value) {

                showValue(value);
            }

            @Override
            public void generativeDistributionSelected(GenerativeDistribution g) {
                showGenerativeDistribution(g);
            }

            @Override
            public void functionSelected(Function f) {

                showFunction(f);
            }

        };

        component.addGraphicalModelListener(listener);
        parser.addGraphicalModelChangeListener(component);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS));
        buttonPanel.add(sampleButton);

        sampleButton.addActionListener(e -> {parser.sample(); showValue(parser.getRootVariable());});

        add(buttonPanel, BorderLayout.NORTH);

        add(intepreter, BorderLayout.SOUTH);

        showValue(parser.getRootVariable());
    }

    private void setDisplayedElement(Viewable viewable) {
        displayedElement = viewable;
    }

    void showValue(Value value) {
        displayedElement = value;
        final int size = splitPane.getDividerLocation();
        splitPane.setRightComponent(value.getViewer());
        splitPane.setDividerLocation(size);
    }

    private void showGenerativeDistribution(GenerativeDistribution g) {
        displayedElement = g;
        final int size = splitPane.getDividerLocation();
        splitPane.setRightComponent(g.getViewer());
        splitPane.setDividerLocation(size);
    }

    private void showFunction(Function f) {
        displayedElement = f;
        final int size = splitPane.getDividerLocation();
        splitPane.setRightComponent(f.getViewer());
        splitPane.setDividerLocation(size);
    }

    public static void main(String[] args) {

//        DoubleValue logthetaMean = new DoubleValue("mean", 3.0);
//        DoubleValue logThetaSD = new DoubleValue("sd", 1.0);
//
//        Normal normal = new Normal(logthetaMean, logThetaSD);
//
//        RandomVariable<Double> logTheta = normal.sample("logTheta");
//        IntegerValue n = new IntegerValue("n", 20);
//
//        Exp exp = new Exp();
//
//        DoubleValue theta = (DoubleValue) exp.apply(logTheta, "\u0398");
//
//        Coalescent coalescent = new Coalescent(theta, n);
//
//        RandomVariable<TimeTree> g = coalescent.sample();
//
//        JCPhyloCTMC jcPhyloCTMC = new JCPhyloCTMC(g, new DoubleValue("mu", 0.01),new IntegerValue("L", 50), new IntegerValue("dim", 4));
//
//        RandomVariable<Alignment> D = jcPhyloCTMC.sample();

        String[] lines = {
                "L = 50;",
                "dim = 4;",
                "mu = 0.01;",
                "n = 20;",
                "mean = 3.0;",
                "sd = 1.0;",
                "logTheta ~ Normal(μ=mean, σ=sd);",
                "Θ = exp(logTheta);",
                "ψ ~ Coalescent(n=n, theta=Θ);",
                "D ~ JCPhyloCTMC(L=L, dim=dim, mu=mu, tree=ψ);"};

        GraphicalModelParser parser = new GraphicalModelParser();
        parser.parseLines(lines);
        Set<Value> values = parser.getRoots();
        if (values.size() != 1) throw new RuntimeException("Expected 1 root node in the graphical model!");

        Value v = values.iterator().next();

        if (v instanceof RandomVariable) {

            GraphicalModelPanel panel = new GraphicalModelPanel(parser);
            panel.setPreferredSize(new Dimension(1200, 800));

            JFrame frame = new JFrame("Graphical Models");
            frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            frame.getContentPane().add(panel, BorderLayout.CENTER);
            frame.pack();
            Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
            frame.setLocation(dim.width / 2 - frame.getSize().width / 2, dim.height / 2 - frame.getSize().height / 2);
            frame.setVisible(true);
        } else {
            throw new RuntimeException("Expected root node to be a random variable!");
        }
    }

}