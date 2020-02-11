package james.app;

import james.graphicalModel.GraphicalModelParser;
import james.graphicalModel.*;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;

import javax.swing.*;
import java.awt.*;
import java.util.*;

public class GraphicalModelPanel extends JPanel {

    GraphicalModelComponent component;
    JLabel dummyLabel = new JLabel("");
    GraphicalModelInterpreter interpreter;
    JTabbedPane rightPane;

    GraphicalModelParser parser;

    JButton sampleButton = new JButton("Sample");

    JSplitPane splitPane;

    Object displayedElement;

    JScrollPane currentSelectionContainer = new JScrollPane();


    GraphicalModelPanel(GraphicalModelParser parser) {

        this.parser = parser;
        interpreter = new GraphicalModelInterpreter(parser);
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS));
        buttonPanel.add(sampleButton);

        sampleButton.addActionListener(e -> {
            parser.sample();
            showValue(parser.getRoots().iterator().next());
        });

        component = new GraphicalModelComponent(parser);
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
        panel.add(component);
        panel.add(buttonPanel);

        setLayout(new BorderLayout());

        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, panel, dummyLabel);
        splitPane.setResizeWeight(0.5);
        add(splitPane, BorderLayout.CENTER);

        GraphicalModelListener listener = new GraphicalModelListener() {
            @Override
            public void valueSelected(Value value) {

                showValue(value);
                rightPane.setSelectedIndex(0);
            }

            @Override
            public void generativeDistributionSelected(GenerativeDistribution g) {
                showParameterized(g);
            }

            @Override
            public void functionSelected(DeterministicFunction f) {

                showParameterized(f);
            }

        };

        component.addGraphicalModelListener(listener);
        parser.addGraphicalModelChangeListener(component);
        parser.addGraphicalModelListener(new GraphicalModelListener() {
            @Override
            public void valueSelected(Value value) {

                showValue(value);
            }

            @Override
            public void generativeDistributionSelected(GenerativeDistribution g) {

            }

            @Override
            public void functionSelected(DeterministicFunction f) {

            }
        });
        
        add(interpreter, BorderLayout.SOUTH);

        currentSelectionContainer.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        currentSelectionContainer.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        rightPane = new JTabbedPane();
        rightPane.addTab("Current", currentSelectionContainer);
        rightPane.addTab("Values", new StatePanel(parser, true, false, false));
        rightPane.addTab("Variables", new StatePanel(parser, false, true, false));
        rightPane.addTab("JSON", new JSONPanel(parser));
        rightPane.addTab("Model", new CanonicalModelPanel(parser));
        splitPane.setRightComponent(rightPane);

        showValue(parser.getRoots().iterator().next());
    }

    public JComponent getViewer(Object object) {

        if (object instanceof Viewable) {
            return ((Viewable) object).getViewer();
        }

        return new JLabel(object.toString());
    }

    void showValue(Value value) {
        displayedElement = value;

        JComponent viewer = getViewer(value);

        if (viewer.getPreferredSize().height > 1) {
            JPanel viewerPanel = new JPanel();
            viewerPanel.setOpaque(false);
            viewerPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
            viewerPanel.add(viewer);
            viewer = viewerPanel;
        }

        currentSelectionContainer.setViewportView(viewer);
        currentSelectionContainer.setBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createMatteBorder(0,0,0,0, viewer.getBackground()),
                        "<html><font color=\"#808080\" >" + value.getId() + "</font></html>"));
        repaint();
    }

    private void showParameterized(Parameterized g) {
        displayedElement = g;

        JComponent viewer = getViewer(g);
        
        if (viewer.getPreferredSize().height > 1) {
            JPanel viewerPanel = new JPanel();
            viewerPanel.setOpaque(false);
            viewerPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
            viewerPanel.add(viewer);
            viewer = viewerPanel;
        }
        currentSelectionContainer.setViewportView(viewer);
        currentSelectionContainer.setBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createMatteBorder(0,0,0,0, viewer.getBackground()),
                        "<html><font color=\"#808080\" >" + g.codeString() + "</font></html>"));
        repaint();

    }

    public static void main(String[] args) {

        String[] lines = {
//                "α = [5.0,5.0,5.0,5.0];",
//                "α_r = [2.0,4.0,2.0,4.0,2.0,2.0];",
//                "r ~ Dirichlet(concentration=α_r);",
//                "freq ~ Dirichlet(concentration=α);",
                "L = 50;",
                "μ = 0.01;",
                "n = 20;",
                "mean = 3.0;",
                "sd = 1.0;",
                "lnΘ ~ Normal(mean=mean, sd=sd);",
                "Θ = exp(lnΘ);",
//                "Q = gtr(rates=r, freq=freq);",
                "Q = [[-1.0,0.3333, 0.3333, 0.3333],[0.3333, -1.0, 0.3333, 0.3333],[0.3333, 0.3333, -1.0, 0.3333],[0.3333, 0.3333, 0.3333, -1.0]];",

                "ψ ~ Coalescent(n=n, theta=Θ);",
                "y0 = 0.0;",
                "σ2 = 0.01;",
                "D ~ PhyloCTMC(L=L, mu=μ, Q=Q, tree=ψ);",
                "y ~ PhyloBrownian(diffusionRate=σ2, y0=y0, tree=ψ);"};

        GraphicalModelParser parser = new GraphicalModelParser();
        parser.parseLines(lines);
        Set<Value> values = parser.getRoots();

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