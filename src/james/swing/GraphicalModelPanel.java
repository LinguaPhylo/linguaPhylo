package james.swing;

import james.Coalescent;
import james.TimeTree;
import james.core.Alignment;
import james.core.JCPhyloCTMC;
import james.core.distributions.Normal;
import james.core.functions.Exp;
import james.graphicalModel.*;

import javax.swing.*;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.*;
import javax.swing.text.html.HTMLDocument;
import java.awt.*;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;

public class GraphicalModelPanel extends JPanel {

    GraphicalModelComponent component;
    JLabel dummyLabel = new JLabel("");
    GraphicalModelTextPane modelTextPane;
    HTMLDocument document;

    JButton sampleButton = new JButton("Sample");

    JSplitPane splitPane;

    RandomVariable variable;

    Viewable displayedElement;

    Map<GenerativeDistribution, RandomVariable> variables = new HashMap<>();

    GraphicalModelPanel(RandomVariable variable) {

        this.variable = variable;

        modelTextPane = new GraphicalModelTextPane(this);
        repopulateVariables();

        component = new GraphicalModelComponent(variable);
        
        setLayout(new BorderLayout());

        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,component,dummyLabel);
        splitPane.setResizeWeight(0.5);
        add(splitPane, BorderLayout.CENTER);

        component.addGraphicalModelListener(new GraphicalModelListener() {
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
        });

        modelTextPane.addGraphicalModelListener(new GraphicalModelListener() {
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
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel,BoxLayout.LINE_AXIS));
        buttonPanel.add(sampleButton);

        sampleButton.addActionListener(e -> sample());
        
        add(buttonPanel,BorderLayout.NORTH);

        add(modelTextPane, BorderLayout.SOUTH);

        showValue(variable);
    }

    private void setDisplayedElement(Viewable viewable) {
        displayedElement = viewable;
    }

    private void repopulateVariables() {
        variables.clear();
        Value.traverseGraphicalModel(variable, new GraphicalModelNodeVisitor() {
            @Override
            public void visitValue(Value value) {
                if (value instanceof RandomVariable) {
                    RandomVariable rv = (RandomVariable)value;
                    variables.put(rv.getGenerativeDistribution(), rv);
                }
                value.addValueListener(() -> modelTextPane.updateModelText());
            }

            public void visitGenDist(GenerativeDistribution genDist) {}

            public void visitFunction(Function f) {}
        }, false);
        modelTextPane.updateModelText();
    }

    private void sample() {
        variable = sampleAll(variable.getGenerativeDistribution());
        repopulateVariables();

        component.setVariable(variable);
        if (displayedElement instanceof RandomVariable) {
            GenerativeDistribution dist = ((RandomVariable)displayedElement).getGenerativeDistribution();
            RandomVariable rv = getVariableByDistribution(dist);
            showValue(rv);
        }
    }

    private RandomVariable sampleAll(GenerativeDistribution generativeDistribution) {
        Map<String, Value> params = generativeDistribution.getParams();

        Map<String, Value> newlySampledParams = new HashMap<>();
        for (Map.Entry<String, Value> e : params.entrySet()) {
            if (e.getValue() instanceof RandomVariable) {
                RandomVariable v = (RandomVariable) e.getValue();

                RandomVariable nv = sampleAll(v.getGenerativeDistribution());
                nv.setId(v.getId());
                newlySampledParams.put(e.getKey(), nv);
            } else if (e.getValue().getFunction() != null) {
                Value v = e.getValue();
                Function f = e.getValue().getFunction();

                Value nv = sampleAll(f);
                nv.setId(v.getId());
                newlySampledParams.put(e.getKey(), nv);
            }
        }
        for (Map.Entry<String, Value> e : newlySampledParams.entrySet()) {
            generativeDistribution.setParam(e.getKey(), e.getValue());
        }
        
        return generativeDistribution.sample();
    }

    private Value sampleAll(Function function) {
        Map<String, Value> params = function.getParams();

        Map<String, RandomVariable> newlySampledParams = new HashMap<>();
        for (Map.Entry<String, Value> e : params.entrySet()) {
            if (e.getValue() instanceof RandomVariable) {
                RandomVariable v = (RandomVariable) e.getValue();

                RandomVariable nv = sampleAll(v.getGenerativeDistribution());
                nv.setId(v.getId());
                newlySampledParams.put(e.getKey(), nv);
            } else if (e.getValue().getFunction() != null) {
                Value v = e.getValue();
                Function f = e.getValue().getFunction();

                Value nv = sampleAll(f);
                nv.setId(v.getId());
            }
        }
        return (Value) function.apply(newlySampledParams.entrySet().iterator().next().getValue());
        
    }

    private RandomVariable getVariableByDistribution(GenerativeDistribution dist) {
        return variables.get(dist);
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

        Random random = new Random();

        DoubleValue logthetaMean = new DoubleValue("mean", 3.0);
        DoubleValue logThetaSD = new DoubleValue("sd", 1.0);

        Normal normal = new Normal(logthetaMean, logThetaSD, random);

        RandomVariable<Double> logTheta = normal.sample("logTheta");
        IntegerValue n = new IntegerValue("n", 20);

        Exp exp = new Exp();

        DoubleValue theta = (DoubleValue) exp.apply(logTheta, "\u0398");

        Coalescent coalescent = new Coalescent(theta, n, random);

        RandomVariable<TimeTree> g = coalescent.sample();

        JCPhyloCTMC jcPhyloCTMC = new JCPhyloCTMC(g, new DoubleValue("mu", 0.01),new IntegerValue("L", 50), new IntegerValue("dim", 4), random);

        RandomVariable<Alignment> D = jcPhyloCTMC.sample();

        PrintWriter p = new PrintWriter(System.out);
        D.print(p);

        GraphicalModelPanel panel = new GraphicalModelPanel(D);
        panel.setPreferredSize(new Dimension(1200, 800));

        JFrame frame = new JFrame("Graphical Models");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(panel, BorderLayout.CENTER);
        frame.pack();
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setLocation(dim.width/2-frame.getSize().width/2, dim.height/2-frame.getSize().height/2);
        frame.setVisible(true);
    }

}