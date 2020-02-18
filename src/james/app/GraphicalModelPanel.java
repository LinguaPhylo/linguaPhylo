package james.app;

import james.graphicalModel.GraphicalModelParser;
import james.graphicalModel.*;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class GraphicalModelPanel extends JPanel {

    GraphicalModelComponent component;
    JLabel dummyLabel = new JLabel("");
    GraphicalModelInterpreter interpreter;
    JTabbedPane rightPane;

    GraphicalModelParser parser;

    JButton sampleButton = new JButton("Sample");
    JButton shiftLeftButton = new JButton("<");
    JButton shiftRightButton = new JButton(">");


    JSplitPane splitPane;

    Object displayedElement;

    JScrollPane currentSelectionContainer = new JScrollPane();


    GraphicalModelPanel(GraphicalModelParser parser) {

        this.parser = parser;
        interpreter = new GraphicalModelInterpreter(parser);
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS));
        buttonPanel.add(sampleButton);
        buttonPanel.add(shiftLeftButton);
        buttonPanel.add(shiftRightButton);

        sampleButton.addActionListener(e -> {
            parser.sample();
            showValue(parser.getRoots().iterator().next());
        });
        shiftLeftButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                component.shiftLeft();
            }
        });
        shiftRightButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                component.shiftRight();
            }
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

        String name = value.getId();
        if (value.isAnonymous()) {
            name = "[" + ((Parameterized)value.getOutputs().get(0)).getParamName(value) + "]";
        }

        currentSelectionContainer.setViewportView(viewer);
        currentSelectionContainer.setBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createMatteBorder(0,0,0,0, viewer.getBackground()),
                        "<html><font color=\"#808080\" >" + name + "</font></html>"));
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

    public void readScript(File scriptFile) {
        Path path = Paths.get(scriptFile.getAbsolutePath());
        try {
            String mimeType = Files.probeContentType(path);

            if (mimeType.equals("text/plain")) {
                parser.clear();
                interpreter.clear();

                FileReader reader = new FileReader(scriptFile);
                BufferedReader bufferedReader = new BufferedReader(reader);
                String line = bufferedReader.readLine();
                while (line != null) {
                    interpreter.interpretInput(line);
                    line = bufferedReader.readLine();
                }
                bufferedReader.close();
                reader.close();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}