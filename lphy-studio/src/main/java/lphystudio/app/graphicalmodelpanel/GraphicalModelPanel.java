package lphystudio.app.graphicalmodelpanel;

import jebl.evolution.sequences.SequenceType;
import lphy.core.codebuilder.CanonicalCodeBuilder;
import lphy.core.exception.SimulatorParsingException;
import lphy.core.logger.LoggerUtils;
import lphy.core.model.*;
import lphy.core.parser.LPhyParserDictionary;
import lphy.core.parser.graphicalmodel.GraphicalModel;
import lphy.core.parser.graphicalmodel.GraphicalModelListener;
import lphy.core.simulator.Sampler;
import lphy.core.simulator.SimulatorListener;
import lphy.core.vectorization.VectorizedFunction;
import lphystudio.app.alignmentcomponent.AlignmentComponent;
import lphystudio.app.alignmentcomponent.SequenceTypePanel;
import lphystudio.app.graphicalmodelcomponent.GraphicalModelComponent;
import lphystudio.app.graphicalmodelcomponent.interactive.InteractiveGraphicalModelComponent;
import lphystudio.app.treecomponent.TimeTreeComponent;
import lphystudio.app.treecomponent.TimeTreeExtraPlotComponent;
import lphystudio.core.codecolorizer.LineCodeColorizer;
import lphystudio.core.editor.UndoManagerHelper;
import lphystudio.core.layeredgraph.Layering;
import lphystudio.core.swing.TidyComboBox;
import lphystudio.core.swing.TidyTextField;
import lphystudio.core.valueeditor.Abstract2DEditor;
import lphystudio.spi.ViewerRegister;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

/**
 * The main panel to include prob graphical model,
 * views {@link ViewerPane}, and command line console {@link StudioConsoleInterpreter}.
 */
public class GraphicalModelPanel extends JPanel {

    GraphicalModelComponent component;
    StudioConsoleInterpreter modelInterpreter;
    StudioConsoleInterpreter dataInterpreter;

    // default interpreter for console is model tab
    final String DEFAULT_INTERPRETER = GraphicalModel.Context.model.toString();

    JTabbedPane leftPane;

    ViewerPane rightPane;

    JToolBar toolbar;

    JLabel repsLabel = new JLabel("reps:");
    JTextField repsField = new TidyTextField("1", 4);
    public JButton sampleButton;
    JCheckBox showConstantNodes = new JCheckBox("Show constants");
    JComboBox<Layering> layeringAlgorithm = new TidyComboBox<>(new Layering[]{
            new Layering.LongestPathFromSinks(), new Layering.LongestPathFromSources()
    });

    //TODO https://github.com/LinguaPhylo/linguaPhylo/issues/307
    //    JCheckBox editValues = new JCheckBox("Edit values");

    JSplitPane horizSplitPane;
    JSplitPane verticalSplitPane;

    Object displayedElement;

//    Sampler sampler;
    CanonicalCodeBuilder codeBuilder = new CanonicalCodeBuilder();

    private JProgressBar progressBar;

    public GraphicalModelPanel(GraphicalModelParserDictionary parser, UndoManagerHelper undoManagerHelper) {

        modelInterpreter = new StudioConsoleInterpreter(parser, LPhyParserDictionary.Context.model, null, undoManagerHelper);
        dataInterpreter = new StudioConsoleInterpreter(parser, LPhyParserDictionary.Context.data, modelInterpreter, undoManagerHelper);

        component = new GraphicalModelComponent(parser);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));

        layeringAlgorithm.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                component.setLayering((Layering) layeringAlgorithm.getSelectedItem());
            }
        });
        layeringAlgorithm.setPreferredSize(new Dimension(160, 20));

        buttonPanel.add(repsLabel);
        buttonPanel.add(repsField);
        sampleButton = new JButton("Sample");
        sampleButton.setToolTipText(LPhyParserDictionary.Utils.SAMPLE_FROM_PARSER);
        buttonPanel.add(sampleButton);

        buttonPanel.add(new JLabel(" Layering:"));
        buttonPanel.add(layeringAlgorithm);
        buttonPanel.add(showConstantNodes);
//        buttonPanel.add(editValues);

        sampleButton.addActionListener(e -> {
            if (LPhyParserDictionary.Utils.isSampleValuesUsingParser()) {
                sampleButton.setText("Sample");
                sampleButton.setToolTipText(LPhyParserDictionary.Utils.SAMPLE_FROM_PARSER);
            } else {
                sampleButton.setText("Re-sample");
                sampleButton.setToolTipText("Resample values from the dictionary with variables");
            }
//            sample(getReps());
            startLongRunningTask();
        });

        showConstantNodes.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                component.setShowConstantNodes(showConstantNodes.isSelected());
//                editValues.setEnabled(showConstantNodes.isSelected());
            }
        });
        showConstantNodes.setSelected(component.getShowConstantNodes());

        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
//        progressBar.setVisible(false); // Initially hidden
        buttonPanel.add(progressBar);

//        editValues.addActionListener(new AbstractAction() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                // avoid GraphicalModelComponent depends on GraphicalModelPanel
//                component.setEditValues(editValues.isSelected());
//                // update currentSelectionContainer
//                // take care of selectedNode here, and the rest will update by showObject(...)
//                LayeredNode selectedNode = component.getSelectedNode();
//                if (selectedNode instanceof LayeredGNode lnode) {
//                    if (lnode.value() instanceof Value value) {
//                        showValue(value, false);
//                    }
//                }
//                component.repaint();
//            }
//        });
//        editValues.setSelected(component.getEditValues());
//        editValues.setToolTipText("Please click Sample button to refresh variables after setting a new value.");

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
        panel.add(component);
        panel.add(buttonPanel);

        setLayout(new BorderLayout());


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

            @Override
            public void layout() {

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

        rightPane = new ViewerPane(parser, component);
        //rightPane.addTab("New Random Variable", new NewRandomVariablePanel(modelInterpreter, ParserUtils.getGenerativeDistributions()));

        leftPane = new JTabbedPane();
        leftPane.addTab("AutoLayout", panel);
        leftPane.addTab("Interactive", new InteractiveGraphicalModelComponent(parser, component));

        horizSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPane, rightPane);
        horizSplitPane.setResizeWeight(0.5);
//        horizSplitPane.setOneTouchExpandable(true);
        horizSplitPane.setContinuousLayout(true);

        JTabbedPane interpreterPane = new JTabbedPane();
        interpreterPane.add("data", dataInterpreter);
        interpreterPane.add("model", modelInterpreter);

        // set default model or model interpreter
        if (DEFAULT_INTERPRETER.equals(GraphicalModel.Context.model.toString())) {
            interpreterPane.setSelectedComponent(modelInterpreter);
        } else {
            interpreterPane.setSelectedComponent(dataInterpreter);
        }

        verticalSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, horizSplitPane, interpreterPane);
        verticalSplitPane.setResizeWeight(0.75);
//        verticalSplitPane.setOneTouchExpandable(true);
        verticalSplitPane.setContinuousLayout(true);
        add(verticalSplitPane, BorderLayout.CENTER);

        if (parser.getDataModelSinks().size() > 0) {
            showValue(parser.getDataModelSinks().iterator().next());
        }
    }

    private void startLongRunningTask() {
        // Create a SwingWorker to perform the calculations
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                // Make the progress bar visible
                SwingUtilities.invokeLater(() -> progressBar.setVisible(true));

                // Simulate long-running task
//                for (int i = 0; i <= 100; i++) {
//                    Thread.sleep(50); // Simulate work
//                    setProgress(i); // Update progress
//                }
                sample(getReps(), progressBar);
                return null;
            }

            @Override
            protected void process(java.util.List<Void> chunks) {
                // Update the progress bar
                progressBar.setValue(getProgress());
            }

            @Override
            protected void done() {
                // Task is done, reset the progress bar
//                progressBar.setVisible(false);
//                progressBar.setValue(0);
//                JOptionPane.showMessageDialog(component, "Task completed!");
            }
        };

        // Execute the worker thread
        worker.execute();
    }

    public JProgressBar getProgressBar() {
        return progressBar;
    }

    //    public Sampler getSampler() {
//        return this.sampler;
//    }

    /**
     * This is duplicated to {@link LPhyParserDictionary#source(BufferedReader, String[])},
     * but has extra code using {@link LineCodeColorizer}
     * and panel function.
     *
     * @param reader
     */
    public void source(BufferedReader reader) {

        LPhyParserDictionary metaData = component.getParserDictionary();

        try {
            metaData.source(reader, null);
        } catch (IOException e) {
            LoggerUtils.logStackTrace(e);
            e.printStackTrace();
        } catch (SimulatorParsingException spe) {
            LoggerUtils.log.severe("Parsing of " + metaData.getName() + " failed: " + spe.getMessage());
        } catch (IllegalArgumentException ex) {
//            LoggerUtils.log.severe(ex.getMessage());
            LoggerUtils.logStackTrace(ex);
        }

        addLinesToConsole(metaData);

        repaint();
    }

    private void addLinesToConsole(LPhyParserDictionary metaData) {
        // fill in data lines and model lines
        String wholeSource = codeBuilder.getCode(metaData);
        String data = codeBuilder.getDataLines();
        dataInterpreter.addInputToPane(data, GraphicalModel.Context.data);
        String model = codeBuilder.getModelLines();
        modelInterpreter.addInputToPane(model, GraphicalModel.Context.model);
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

    // Key is the replicate index, value is the result of each replicate.
    Map<Integer, List<Value>> valuesAllRepsMap;

    public Map<Integer, List<Value>> getValuesAllRepsMap() {
        return valuesAllRepsMap;
    }

    private void sample(int reps, JProgressBar progressBar) {
        sampleButton.setEnabled(false);
        progressBar.setValue(0);

        long start = System.currentTimeMillis();

        // add Loggers here, to trigger after click Sample button
        List<SimulatorListener> loggers = rightPane.getGUISimulatorListener();

        String id = null;
        if (displayedElement instanceof Value && !((Value) displayedElement).isAnonymous()) {
            id = ((Value) displayedElement).getId();
        }

        progressBar.setValue(5);
        // GraphicalModelSampler notifies listeners in ParserDictionary
        Sampler sampler = new GraphicalModelSampler(component.getParserDictionary());
        progressBar.setValue(10);
        // Sampler use the lphy code in component.getParser(), and output results to loggers
        // if null then use a random seed
        valuesAllRepsMap = sampler.sampleAll(reps, loggers, null);
//        this.sampler = sampler;
        progressBar.setValue(90);

        // refresh graphical nodes
        component.modelChanged();

        // show current selected value
        if (id != null) {
            Value<?> selectedValue = component.getParserDictionary().getValue(id, LPhyParserDictionary.Context.model);
            if (selectedValue != null) {
                showValue(selectedValue, false);
            }
        } else {
            List<Value<?>> sinks = component.getParserDictionary().getDataModelSinks();
            // TODO should not move to tab for all sinks?
//            if (sinks.size() > 0) showValue(sinks.get(0), false);
            for (Value value : sinks) {
                if (value instanceof RandomVariable<?>) {
                    showValue(value, false);
                }
            }
        }
        long end = System.currentTimeMillis();
        LoggerUtils.log.info("sample(" + reps + ") took " + (end - start) + " ms.");

        progressBar.setValue(95);

        // refresh all viewerComponent
        rightPane.refresh();

        progressBar.setValue(100);
        sampleButton.setEnabled(true);
    }

    void showValue(Value value) {
        showValue(value, true);
    }

    void showValue(Value value, boolean moveToTab) {
        if (value != null) {
            String type = value.value().getClass().getSimpleName();
            String label = value.getLabel();

            showObject(type + " " + label, value, moveToTab);
        }
    }

    private void showParameterized(Generator g) {
        showObject(g.codeString(), g, true);
    }

    // TODO It looks ViewerRegister not store ValueEditor (JTextField),
    // so everytime viewer.getViewer(object) creates a new ValueEditor.
    private void showObject(String label, Object obj, boolean moveToTab) {
        displayedElement = obj;

        JComponent viewer = null;
        if (obj instanceof Value) {
            viewer = ViewerRegister.getJComponentForValue(obj);

            if (viewer instanceof TimeTreeComponent timeTreeComponent) {
//            if (timeTreeComponent.getTimeTree().isUltrametric()) {
                viewer = createTimeTreeSplitPane(timeTreeComponent);
//            }
            } else if (viewer instanceof AlignmentComponent alignmentComponent) {
                SequenceType sequenceType = alignmentComponent.getAlignment().getSequenceType();
                SequenceTypePanel sequenceTypePanel = new SequenceTypePanel(sequenceType);
                JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, alignmentComponent, sequenceTypePanel);
                splitPane.setResizeWeight(1.0);
                splitPane.setOneTouchExpandable(true);
                splitPane.setContinuousLayout(true);
                splitPane.setBorder(null);
                splitPane.setBackground(Color.white);
                final double ratio = 0.95;
//                if (SequenceTypePanel.isShowLegends())
                    splitPane.setDividerLocation(ratio);
//                else
//                    splitPane.setDividerLocation(1.0);

                splitPane.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        if (e.getButton() == MouseEvent.BUTTON1)
                            AlignmentComponent.setShowTreeInAlignmentViewerIfAvailable(
                                    !AlignmentComponent.getShowTreeInAlignmentViewerIfAvailable());
                        else if (e.getButton() == MouseEvent.BUTTON3) {
                            SequenceTypePanel.setShowLegends(!SequenceTypePanel.isShowLegends());
                            if (SequenceTypePanel.isShowLegends())
                                splitPane.setDividerLocation(ratio);
                            else
                                splitPane.setDividerLocation(1.0);
                        } else {
                            AlignmentComponent.showErrorsIfAvailable = !AlignmentComponent.showErrorsIfAvailable;
                        }
                        splitPane.repaint();
                    }
                });

                viewer = splitPane;
            }

        } else if (obj instanceof VectorizedFunction<?>) {
            viewer = new JLabel(((VectorizedFunction<?>) obj).getComponentFunction(0).getRichDescription(0));
        } else if (obj instanceof Generator) {
            viewer = new JLabel(((Generator) obj).getRichDescription(0));
        } else {
            LoggerUtils.log.severe("Trying to show an object that is neither Value nor Generator.");
        }
//        if (viewer instanceof JTextField textField)
//            textField.setEditable(editValues.isSelected());

        if (viewer instanceof JTextField || viewer instanceof JLabel || viewer instanceof Abstract2DEditor) {
            JPanel viewerPanel = new JPanel();
            viewerPanel.setOpaque(false);
            viewerPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
            viewerPanel.add(viewer);
            viewer = viewerPanel;
        }

        rightPane.currentSelectionContainer.setViewportView(viewer);
        rightPane.currentSelectionContainer.setBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createMatteBorder(0, 0, 0, 0, viewer.getBackground()),
                        "<html><font color=\"#808080\" >" + label + "</font></html>"));

        if (moveToTab) rightPane.setSelectedComponent(rightPane.currentSelectionContainer);
        rightPane.repaint();
        repaint();
    }

    private JSplitPane createTimeTreeSplitPane(TimeTreeComponent timeTreeComponent) {
        TimeTreeExtraPlotComponent plotComponent = new TimeTreeExtraPlotComponent(timeTreeComponent);
        TimeTreeExtraPlotPanel timeTreePlotPanel = new TimeTreeExtraPlotPanel(plotComponent);

        JSplitPane treeSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, timeTreeComponent, timeTreePlotPanel);
        treeSplitPane.setResizeWeight(0.5);
        treeSplitPane.setOneTouchExpandable(true);
        treeSplitPane.setContinuousLayout(true);
        treeSplitPane.setBorder(null);
        treeSplitPane.setBackground(Color.white);
//        if (TimeTreeExtraPlotComponent.isShowExtraPlot())
//            treeSplitPane.setDividerLocation(0.5);
//        else
//            treeSplitPane.setDividerLocation(1.0);

        //TODO not working
//        if (TimeTreeExtraPlotComponent.isShowExtraPlot())
//            treeSplitPane.setDividerLocation(0.5);
//        else
//            treeSplitPane.setDividerLocation(1.0);
        //TODO not working
//        treeSplitPane.addPropertyChangeListener("dividerLocation", evt -> {
//            //  Get the new divider location of the split pane
//            int location = (Integer) evt.getNewValue();
//            // getMaximumDividerLocation() NOT return the maximum position of the divider bar when maximized using the arrow
//            TimeTreeExtraPlotComponent.setShowExtraPlot(location < treeSplitPane.getMaximumDividerLocation());
//        });

        treeSplitPane.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON3) {
                    TimeTreeExtraPlotComponent.setShowExtraPlot(!TimeTreeExtraPlotComponent.isShowExtraPlot());
                    if (TimeTreeExtraPlotComponent.isShowExtraPlot())
                        treeSplitPane.setDividerLocation(0.5);
                    else
                        treeSplitPane.setDividerLocation(1.0);
                }
                treeSplitPane.repaint();
            }
        });
        return treeSplitPane;
    }

    // IO should be in one place
    // use LinguaPhyloStudio readFile(File exampleFile)
    @Deprecated
    public void readScript(File scriptFile) {
        Path path = Paths.get(scriptFile.getAbsolutePath());
        try {
            String mimeType = Files.probeContentType(path);
            // bug: NullPointerException: Cannot invoke "String.equals(Object)"
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

    public GraphicalModelParserDictionary getParserDictionary() {
        return component.getParserDictionary();
    }

    public ViewerPane getRightPane() {
        return rightPane;
    }

    public JTextPane getCanonicalModelPane() {
        return rightPane.canonicalModelPanel.pane;
    }

    public GraphicalModelComponent getComponent() {
        return component;
    }

    /**
     * clear panel, parser, and interpreters
     */
    public void clear() {
        dataInterpreter.clear();
        modelInterpreter.clear();
        component.clear();
        rightPane.clear();
    }

    public JToolBar getToolbar() {
        return toolbar;
    }

    public void setToolbar(JToolBar toolbar) {
        this.toolbar = toolbar;
    }
}