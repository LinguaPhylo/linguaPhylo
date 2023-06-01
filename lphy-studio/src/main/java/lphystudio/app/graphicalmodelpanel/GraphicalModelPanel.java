package lphystudio.app.graphicalmodelpanel;

import jebl.evolution.sequences.SequenceType;
import lphy.base.Sampler;
import lphy.base.logger.RandomValueLogger;
import lphy.core.graphicalmodel.GraphicalModel;
import lphy.core.graphicalmodel.GraphicalModelListener;
import lphy.core.graphicalmodel.components.DeterministicFunction;
import lphy.core.graphicalmodel.components.GenerativeDistribution;
import lphy.core.graphicalmodel.components.Generator;
import lphy.core.graphicalmodel.components.Value;
import lphy.core.parser.GraphicalLPhyParser;
import lphy.core.parser.LPhyMetaParser;
import lphy.core.parser.REPL;
import lphy.core.parser.Script;
import lphy.core.util.LoggerUtils;
import lphy.core.vectorization.VectorizedFunction;
import lphystudio.app.alignmentcomponent.AlignmentComponent;
import lphystudio.app.alignmentcomponent.SequenceTypePanel;
import lphystudio.app.graphicalmodelcomponent.GraphicalModelComponent;
import lphystudio.app.graphicalmodelcomponent.interactive.InteractiveGraphicalModelComponent;
import lphystudio.app.treecomponent.TimeTreeComponent;
import lphystudio.app.treecomponent.TimeTreeExtraPlotComponent;
import lphystudio.core.codebuilder.CanonicalCodeBuilder;
import lphystudio.core.codecolorizer.LineCodeColorizer;
import lphystudio.core.editor.UndoManagerHelper;
import lphystudio.core.layeredgraph.Layering;
import lphystudio.core.swing.TidyComboBox;
import lphystudio.core.swing.TidyTextField;
import lphystudio.core.valueeditors.Abstract2DEditor;

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
import java.util.LinkedList;
import java.util.List;

/**
 * The main panel to include prob graphical model,
 * views {@link ViewerPane}, and command line console {@link GraphicalModelInterpreter}.
 */
public class GraphicalModelPanel extends JPanel {

    GraphicalModelComponent component;
    GraphicalModelInterpreter modelInterpreter;
    GraphicalModelInterpreter dataInterpreter;

    // default interpreter for console is model tab
    final String DEFAULT_INTERPRETER = GraphicalModel.Context.model.toString();

    JTabbedPane leftPane;

    ViewerPane rightPane;

    JToolBar toolbar;

    JLabel repsLabel = new JLabel("reps:");
    JTextField repsField = new TidyTextField("1", 4);
    JButton sampleButton = new JButton("Sample");
    JCheckBox showConstantNodes = new JCheckBox("Show constants");
    JComboBox<Layering> layeringAlgorithm = new TidyComboBox<>(new Layering[]{
            new Layering.LongestPathFromSinks(), new Layering.LongestPathFromSources()
    });

    //TODO https://github.com/LinguaPhylo/linguaPhylo/issues/307
    //    JCheckBox editValues = new JCheckBox("Edit values");

    JSplitPane horizSplitPane;
    JSplitPane verticalSplitPane;

    Object displayedElement;

    Sampler sampler;

    CanonicalCodeBuilder codeBuilder = new CanonicalCodeBuilder();


    public GraphicalModelPanel(GraphicalLPhyParser parser, UndoManagerHelper undoManagerHelper) {

        dataInterpreter = new GraphicalModelInterpreter(parser, LPhyMetaParser.Context.data, undoManagerHelper);
        modelInterpreter = new GraphicalModelInterpreter(parser, LPhyMetaParser.Context.model, undoManagerHelper);

        component = new GraphicalModelComponent(parser);

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
//        buttonPanel.add(editValues);

        sampleButton.addActionListener(e -> sample(getReps()));

        showConstantNodes.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                component.setShowConstantNodes(showConstantNodes.isSelected());
//                editValues.setEnabled(showConstantNodes.isSelected());
            }
        });
        showConstantNodes.setSelected(component.getShowConstantNodes());

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

        if (parser.getModelSinks().size() > 0) {
            showValue(parser.getModelSinks().iterator().next());
        }
    }

    public Sampler getSampler() {
        return this.sampler;
    }

    /**
     * This is duplicated to {@link REPL#source(BufferedReader)},
     * but has extra code using {@link LineCodeColorizer}
     * and panel function.
     *
     * @param reader
     * @throws IOException
     */
    public void source(BufferedReader reader) throws IOException {

        Script script = Script.loadLPhyScript(reader);

        dataInterpreter.interpretInput(script.dataLines, LPhyMetaParser.Context.data);
        modelInterpreter.interpretInput(script.modelLines, LPhyMetaParser.Context.model);

        repaint();
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

        // add Loggers here, to trigger after click Sample button
        loggers.addAll(rightPane.getRandomValueLoggers());

        // These sync the consoles with GraphicalModelComponent containing the lphy code
        // the code may be changed by GUI, such as squared rectangles.
        dataInterpreter.clear();
        modelInterpreter.clear();
        // refresh data and model lines
        String text = codeBuilder.getCode(component.getParser());
        dataInterpreter.interpretInput(codeBuilder.getDataLines(), LPhyMetaParser.Context.data);
        modelInterpreter.interpretInput(codeBuilder.getModelLines(), LPhyMetaParser.Context.model);

        // Sample using the lphy code in component.getParser(), and output results to loggers
        Sampler sampler = new Sampler(component.getParser());
        sampler.sample(reps, loggers);
        this.sampler = sampler;

        if (id != null) {
            Value<?> selectedValue = component.getParser().getValue(id, LPhyMetaParser.Context.model);
            if (selectedValue != null) {
                showValue(selectedValue, false);
            }
        } else {
            List<Value<?>> sinks = component.getParser().getModelSinks();
            if (sinks.size() > 0) showValue(sinks.get(0), false);
        }
        long end = System.currentTimeMillis();
        LoggerUtils.log.info("sample(" + reps + ") took " + (end - start) + " ms.");

        rightPane.variableSummary.repaint();
        rightPane.repaint();
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

    public GraphicalLPhyParser getParser() {
        return component.getParser();
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