package lphystudio.app.graphicalmodelpanel;

import lphy.core.simulator.SimulatorListener;
import lphystudio.app.graphicalmodelcomponent.GraphicalModelComponent;
import lphystudio.core.logger.AlignmentTextArea;
import lphystudio.core.logger.TreeTextArea;
import lphystudio.core.logger.VariableSummaryTable;
import lphystudio.core.logger.VariableTextArea;
import lphystudio.core.narrative.HTMLNarrative;
import lphystudio.core.narrative.LaTeXNarrative;

import javax.swing.*;
import javax.swing.text.html.HTMLEditorKit;
import java.awt.*;
import java.util.List;
import java.util.prefs.Preferences;

public class ViewerPane extends JTabbedPane {

    static Preferences preferences = Preferences.userNodeForPackage(ViewerPane.class);

    protected JScrollPane currentSelectionContainer = new JScrollPane();

    JScrollPane valueScrollPane;
    JScrollPane variablesScrollPane;

    CanonicalModelPanel canonicalModelPanel;
    NarrativePanel narrativePanel;
    NarrativePanel latexPanel;

    JScrollPane variableSummaryScrollPane;
    JScrollPane variableLogScrollPane;
    JScrollPane treeLogScrollPane;
    AlignmentLogPanel alignmentLogPanel;

    ErrorPanel errorPanel;

    VariableSummaryTable variableSummary = new VariableSummaryTable();//true, true
    VariableTextArea variableTextArea = new VariableTextArea();//true, true
    TreeTextArea treeTextArea = new TreeTextArea();
    AlignmentTextArea alignmentTextArea;

    JComponent[] viewerComponent = new JComponent[Viewer.values().length];

    JMenu viewerMenu = new JMenu("View");

    public void clear() {
        currentSelectionContainer.setViewport(null);
        currentSelectionContainer.setBorder(null);
        treeTextArea.clear();
        variableTextArea.clear();
        alignmentTextArea.clear();
    }

    enum Viewer {
        Current("Current"),
        Constants("Constants"),
        Variables("Variables"),
        Model("Model"),
        Narrative("Narrative"),
        Latex("Latex"),
        Variable_Summary("Variable Summary"),
        Variable_Log("Variable Log"),
        Tree_Log ("Tree Log"),
        Alignment_Log ("Alignment"),
        Errors ("Errors");

        public String name;

        Viewer(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }


    public ViewerPane(GraphicalModelParserDictionary parserDictionary, GraphicalModelComponent component) {

        currentSelectionContainer.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        currentSelectionContainer.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        valueScrollPane = new JScrollPane(new StatePanel(parserDictionary, true, false));
        valueScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        variablesScrollPane = new JScrollPane(new StatePanel(parserDictionary, false, true));
        variablesScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        canonicalModelPanel = new CanonicalModelPanel(parserDictionary);
        narrativePanel = new NarrativePanel(parserDictionary, new HTMLNarrative(), component, new HTMLEditorKit());
        latexPanel = new NarrativePanel(parserDictionary, new LaTeXNarrative(), component);

        variableSummaryScrollPane = new JScrollPane(variableSummary);
        variableLogScrollPane = new JScrollPane(variableTextArea);
        treeLogScrollPane = new JScrollPane(treeTextArea);

        alignmentTextArea = new AlignmentTextArea(parserDictionary);
        alignmentLogPanel = new AlignmentLogPanel(alignmentTextArea);

        errorPanel = new ErrorPanel();

        viewerComponent[Viewer.Current.ordinal()] = currentSelectionContainer;
        viewerComponent[Viewer.Constants.ordinal()] = valueScrollPane;
        viewerComponent[Viewer.Variables.ordinal()] = variablesScrollPane;
        viewerComponent[Viewer.Model.ordinal()] = canonicalModelPanel;
        viewerComponent[Viewer.Narrative.ordinal()] = narrativePanel;
        viewerComponent[Viewer.Latex.ordinal()] = latexPanel;
        viewerComponent[Viewer.Variable_Summary.ordinal()] = variableSummaryScrollPane;
        viewerComponent[Viewer.Variable_Log.ordinal()] = variableLogScrollPane;
        viewerComponent[Viewer.Tree_Log.ordinal()] = treeLogScrollPane;
        viewerComponent[Viewer.Alignment_Log.ordinal()] = alignmentLogPanel;
        viewerComponent[Viewer.Errors.ordinal()] = errorPanel;

        for (Viewer viewer : Viewer.values()) {
            if (getShowViewer(viewer)) {
                addTab(viewer.name, viewerComponent[viewer.ordinal()]);
            }
        }

        buildViewersMenu();
    }

    private void buildViewersMenu() {
        //Build the third menu.

        for (int i = 0; i < Viewer.values().length; i++) {

            Viewer viewer = Viewer.values()[i];

            JCheckBoxMenuItem showViewer = new JCheckBoxMenuItem("Show " + viewer.name);
            showViewer.setState(getShowViewer(viewer));
            viewerMenu.add(showViewer);

            showViewer.addActionListener(e -> setShowViewer(showViewer.getState(), viewer));
        }
    }

    public void refresh() {
        for (JComponent vC: viewerComponent)
            vC.repaint();
        repaint();
    }

    public List<SimulatorListener> getGUISimulatorListener() {
        return List.of(variableTextArea, treeTextArea, variableSummary, alignmentTextArea);
    }

    @Deprecated
    public StatePanel getConstantsPanel() {
        return (StatePanel) valueScrollPane.getViewport().getView();
    }

    public VariableTextArea getVariableLog() {
        return variableTextArea;
    }

    public TreeTextArea getTreeLog() {
        return treeTextArea;
    }

    public AlignmentTextArea getAlignmentLog() {
        return alignmentTextArea;
    }

    public JMenu getMenu() {
        return viewerMenu;
    }

    static boolean getShowViewer(Viewer viewer) {
        return preferences.getBoolean("show" + viewer.name, true);
    }

    void setShowViewer(boolean show, Viewer viewer) {
        preferences.putBoolean("show" + viewer.name, show);

        JComponent component = viewerComponent[viewer.ordinal()];

        if (show) {
            if (indexOfTabComponent(component) < 0) {
                insertTab(viewer.name, null, component, viewer.name, getIndex(viewer));
            }
            setSelectedComponent(component);
        } else {
            remove(component);
        }
    }

    private int getOrdinal(Component component) {
        for (int i = 0; i <  viewerComponent.length; i++) {
            if (component == viewerComponent[i]) return i;
        }
        return -1;
    }

    // find the correct index to insert this
    private int getIndex(Viewer viewer) {

        for (int i = 0; i < getTabCount(); i++) {
            if (getOrdinal(getComponent(i)) >= viewer.ordinal()) return i;
        }
        return getTabCount();
    }
}
