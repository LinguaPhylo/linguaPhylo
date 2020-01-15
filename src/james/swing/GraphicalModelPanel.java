package james.swing;

import james.Coalescent;
import james.TimeTree;
import james.core.LogNormal;
import james.graphicalModel.GenerativeDistribution;
import james.graphicalModel.RandomVariable;
import james.graphicalModel.Value;

import javax.swing.*;
import java.awt.*;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;
import java.util.List;

public class GraphicalModelPanel extends JPanel {

    GraphicalModelComponent component;
    JLabel dummyLabel = new JLabel("");

    JButton sampleButton = new JButton("Sample");

    JSplitPane splitPane;

    RandomVariable variable;

    Value displayedValue;

    GraphicalModelPanel(RandomVariable variable) {

        this.variable = variable;
        component = new GraphicalModelComponent(variable);
        
        setLayout(new BorderLayout());

        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,component,dummyLabel);
        splitPane.setResizeWeight(0.5);
        add(splitPane, BorderLayout.CENTER);

        component.addGraphicalModelListener(this::showValue);

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        variable.print(pw);
        pw.flush();

        JTextArea textArea = new JTextArea(sw.toString());
        textArea.setFont(new Font("monospaced", Font.PLAIN, 16));
        textArea.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel,BoxLayout.LINE_AXIS));
        buttonPanel.add(sampleButton);

        sampleButton.addActionListener(e -> sample());
        
        add(buttonPanel,BorderLayout.NORTH);

        add(textArea, BorderLayout.SOUTH);
    }

    private void sample() {
        variable = sampleAll(variable.getGenerativeDistribution());
        component.setVariable(variable);
        if (displayedValue instanceof RandomVariable) {
            GenerativeDistribution dist = ((RandomVariable)displayedValue).getGenerativeDistribution();
            RandomVariable rv = getVariableByDistribution(variable, dist);
            showValue(rv);
        }
    }

    private RandomVariable sampleAll(GenerativeDistribution generativeDistribution) {
        Map<String, Value> params = generativeDistribution.getParams();

        Map<String, RandomVariable> newlySampledParams = new HashMap<>();
        for (Map.Entry<String, Value> e : params.entrySet()) {
            if (e.getValue() instanceof RandomVariable) {
                RandomVariable v = (RandomVariable) e.getValue();

                RandomVariable nv = sampleAll(v.getGenerativeDistribution());
                nv.setId(v.getId());
                newlySampledParams.put(e.getKey(), nv);
            }
        }
        for (Map.Entry<String, RandomVariable> e : newlySampledParams.entrySet()) {
            generativeDistribution.setParam(e.getKey(), e.getValue());
        }
        
        return generativeDistribution.sample();
    }

    private RandomVariable getVariableByDistribution(RandomVariable variable, GenerativeDistribution dist) {
        GenerativeDistribution candidate = variable.getGenerativeDistribution();

        if (candidate == dist) {
            return variable;
        } else {
            Map<String,Value> params = candidate.getParams();
            for (Value v : params.values()) {
                if (v instanceof RandomVariable) {
                    RandomVariable crv = getVariableByDistribution((RandomVariable)v, dist);
                    if (crv != null) return crv;
                }
            }
            return null;
        }
    }

    private void showValue(Value value) {
        displayedValue = value;
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
