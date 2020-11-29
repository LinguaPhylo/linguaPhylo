package lphy.app;

import lphy.app.graphicalmodelcomponent.GraphicalModelComponent;
import lphy.app.graphicalmodelcomponent.Layering;
import lphy.core.LPhyParser;
import lphy.core.Sampler;
import lphy.graphicalModel.*;
import lphy.parser.ParserUtils;
import lphy.utils.LoggerUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class GraphicalModelPanel extends JPanel {

    GraphicalModelComponent component;
    JLabel dummyLabel = new JLabel("");
    GraphicalModelInterpreter modelInterpreter;
    GraphicalModelInterpreter dataInterpreter;
    JTabbedPane rightPane;
    VariableLog variableLog = new VariableLog(true, true);
    VariableSummary variableSummary = new VariableSummary(true, true);
    TreeLog treeLog = new TreeLog();

    GraphicalLPhyParser parser;

    JLabel repsLabel = new JLabel("reps:");
    JTextField repsField = new TidyTextField("1", 4);
    JButton sampleButton = new JButton("Sample");
    JCheckBox showConstantNodes = new JCheckBox("Show constants");
    JComboBox<Layering> layeringAlgorithm = new TidyComboBox<>(new Layering[]{new Layering.LongestPathFromSinks(), new Layering.LongestPathFromSources()});

    JSplitPane horizSplitPane;
    JSplitPane verticalSplitPane;

    Object displayedElement;

    JScrollPane currentSelectionContainer = new JScrollPane();

    GraphicalModelPanel(GraphicalLPhyParser parser) {

        this.parser = parser;

        dataInterpreter = new GraphicalModelInterpreter(parser, LPhyParser.Context.data);
        modelInterpreter = new GraphicalModelInterpreter(parser, LPhyParser.Context.model);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS));

        layeringAlgorithm.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                component.setLayering((Layering) layeringAlgorithm.getSelectedItem());
            }
        });
        layeringAlgorithm.setPreferredSize(new Dimension(200, 20));

        buttonPanel.add(repsLabel);
        buttonPanel.add(repsField);
        buttonPanel.add(sampleButton);
        buttonPanel.add(new JLabel(" Layering:"));
        buttonPanel.add(layeringAlgorithm);
        buttonPanel.add(showConstantNodes);

        sampleButton.addActionListener(e -> sample(getReps()));

        showConstantNodes.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                component.setShowConstantNodes(showConstantNodes.isSelected());
            }
        });

        component = new GraphicalModelComponent(parser);

        showConstantNodes.setSelected(component.getShowConstantNodes());

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
        panel.add(component);
        panel.add(buttonPanel);

        setLayout(new BorderLayout());

        horizSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, panel, dummyLabel);
        horizSplitPane.setResizeWeight(0.5);

        JTabbedPane interpreterPane = new JTabbedPane();
        interpreterPane.add("data", dataInterpreter);
        interpreterPane.add("model", modelInterpreter);

        verticalSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, horizSplitPane, interpreterPane);
        verticalSplitPane.setResizeWeight(0.75);
        add(verticalSplitPane, BorderLayout.CENTER);

        GraphicalModelListener listener = new GraphicalModelListener() {
            @Override
            public void valueSelected(Value value) {

                showValue(value);
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

        //TODO need a new way to deal with this model listener interaction

        //        parser.addGraphicalModelListener(new GraphicalModelListener() {
//            @Override
//            public void valueSelected(Value value) {
//
//                showValue(value);
//            }
//
//            @Override
//            public void generativeDistributionSelected(GenerativeDistribution g) {
//
//            }
//
//            @Override
//            public void functionSelected(DeterministicFunction f) {
//
//            }
//        });

        currentSelectionContainer.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        currentSelectionContainer.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        JScrollPane valueScrollPane = new JScrollPane(new StatePanel(parser, true, false));
        valueScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        JScrollPane variablesScrollPane = new JScrollPane(new StatePanel(parser, false, true));
        variablesScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        rightPane = new JTabbedPane();
        rightPane.addTab("Current", currentSelectionContainer);
        rightPane.addTab("Constants", valueScrollPane);
        rightPane.addTab("Variables", variablesScrollPane);
        rightPane.addTab("Model", new CanonicalModelPanel(parser));
        rightPane.addTab("Variable Summary", new JScrollPane(variableSummary));
        rightPane.addTab("Variable Log", new JScrollPane(variableLog));
        rightPane.addTab("Tree Log", new JScrollPane(treeLog));
        rightPane.addTab("New Random Variable", new NewRandomVariablePanel(parser, ParserUtils.getGenerativeDistributions()));
        horizSplitPane.setRightComponent(rightPane);

        if (parser.getModelSinks().size() > 0) {
            showValue(parser.getModelSinks().iterator().next());
        }
    }

    /**
     * This is duplicated to {@link lphy.parser.REPL#source(BufferedReader)},
     * but has extra code using {@link lphy.parser.codecolorizer.CodeColorizer}
     * and panel function.
     * @param reader
     * @throws IOException
     */
    public void source(BufferedReader reader) throws IOException {
        String line = reader.readLine();
        LPhyParser.Context context = LPhyParser.Context.model;
        boolean skip;
        while (line != null) {
            skip = false;
            if (line.trim().startsWith("data")) {
                context = LPhyParser.Context.data;
                skip = true;
            } else if (line.trim().startsWith("model")) {
                context = LPhyParser.Context.model;
                skip = true;
            } else if (line.trim().startsWith("for")) {
                line = consumeForLoop(line, reader);
            } else if (line.trim().startsWith("}")) {
                // this line is just closing a data or model block.
                skip = true;
            }

            if (!skip) {
                switch (context) {
                    case data:
                        dataInterpreter.interpretInput(line, LPhyParser.Context.data);
                        break;
                    case model:
                        modelInterpreter.interpretInput(line, LPhyParser.Context.model);
                        break;
                }
            }
            line = reader.readLine();
        }
        repaint();
        reader.close();
    }

    private String consumeForLoop(String firstLine, BufferedReader reader) throws IOException {
        StringBuilder builder = new StringBuilder(firstLine);
        String line = reader.readLine();
        while (!line.trim().startsWith("}")) {
            builder.append(line);
            line = reader.readLine();
        }
        builder.append(line);
        return builder.toString();
    }

    private int getReps() {
        int reps = 1;
        try {
            reps = Integer.parseInt(repsField.getText());
        } catch (NumberFormatException nfe) {
            repsField.setText("1");
            reps = 1;
        }
        return reps;
    }

    void sample(int reps) {
        sample(reps, new LinkedList<>());
    }

    void sample(int reps, List<RandomValueLogger> loggers) {

        long start = System.currentTimeMillis();

        String id = null;
        if (displayedElement instanceof Value && !((Value) displayedElement).isAnonymous()) {
            id = ((Value) displayedElement).getId();
        }

        loggers.add(variableLog);
        loggers.add(treeLog);
        loggers.add(variableSummary);

        Sampler sampler = new Sampler(parser);
        sampler.sample(reps, loggers);

        if (id != null) {
            Value<?> selectedValue = parser.getValue(id, LPhyParser.Context.model);
            if (selectedValue != null) {
                showValue(selectedValue);
            }
        } else {
            Set<Value<?>> sinks = parser.getModelSinks();
            if (sinks.size() > 0) showValue(sinks.iterator().next());
        }
        long end = System.currentTimeMillis();
        LoggerUtils.log.info("sample(" + reps + ") took " + (end - start) + " ms.");

        variableSummary.repaint();
        rightPane.repaint();
    }

    void showValue(Value value) {
        if (value != null) {
            String type = value.value().getClass().getSimpleName();
            String label = value.getLabel();

            showObject(type + " " + label, value);
        }
    }

    private void showParameterized(Generator g) {
        showObject(g.codeString(), g);
    }

    private void showObject(String label, Object obj) {
        displayedElement = obj;

        JComponent viewer = null;
        if (obj instanceof Value) {
            viewer = ViewerRegister.getJComponentForValue(obj);
        } else if (obj instanceof Generator) {
            viewer = new JLabel(((Generator)obj).getRichDescription(0));
        } else {
            LoggerUtils.log.severe("Trying to show an object that is neither Value nor Generator.");
        }

        if (viewer instanceof JTextField || viewer instanceof JLabel || viewer instanceof DoubleArray2DEditor) {
            JPanel viewerPanel = new JPanel();
            viewerPanel.setOpaque(false);
            viewerPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
            viewerPanel.add(viewer);
            viewer = viewerPanel;
        }
        currentSelectionContainer.setViewportView(viewer);
        currentSelectionContainer.setBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createMatteBorder(0, 0, 0, 0, viewer.getBackground()),
                        "<html><font color=\"#808080\" >" + label + "</font></html>"));

        //rightPane.setSelectedComponent(currentSelectionContainer);

        repaint();
    }

    public void readScript(File scriptFile) {
        Path path = Paths.get(scriptFile.getAbsolutePath());
        try {
            String mimeType = Files.probeContentType(path);

            if (mimeType.equals("text/plain")) {
                // TODO need to find another way to do this
                //parser.clear();
                dataInterpreter.clear();
                modelInterpreter.clear();

                FileReader reader = new FileReader(scriptFile);
                BufferedReader bufferedReader = new BufferedReader(reader);
                String line = bufferedReader.readLine();
                while (line != null) {
                    dataInterpreter.clear();
                    modelInterpreter.clear();
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