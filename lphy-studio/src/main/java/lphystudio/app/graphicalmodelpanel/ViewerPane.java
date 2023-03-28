package lphystudio.app.graphicalmodelpanel;

import lphy.core.GraphicalLPhyParser;
import lphy.graphicalModel.RandomValueLogger;
import lphystudio.app.graphicalmodelcomponent.GraphicalModelComponent;
import lphystudio.app.narrative.HTMLNarrative;
import lphystudio.app.narrative.LaTeXNarrative;
import lphystudio.core.log.AlignmentLog;
import lphystudio.core.log.TreeLog;
import lphystudio.core.log.VariableLog;
import lphystudio.core.log.VariableSummary;

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

    VariableSummary variableSummary = new VariableSummary(true, true);
    VariableLog variableLog = new VariableLog(true, true);
    TreeLog treeLog = new TreeLog();
    AlignmentLog alignmentLog;

    JComponent[] viewerComponent = new JComponent[Viewer.values().length];

    JMenu viewerMenu = new JMenu("View");

    public void clear() {
        currentSelectionContainer.setViewport(null);
        currentSelectionContainer.setBorder(null);
        treeLog.clear();
        variableLog.clear();
        alignmentLog.clear();
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


    public ViewerPane(GraphicalLPhyParser parser, GraphicalModelComponent component) {

        currentSelectionContainer.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        currentSelectionContainer.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        valueScrollPane = new JScrollPane(new StatePanel(parser, true, false));
        valueScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        variablesScrollPane = new JScrollPane(new StatePanel(parser, false, true));
        variablesScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        canonicalModelPanel = new CanonicalModelPanel(parser);
        narrativePanel = new NarrativePanel(parser, new HTMLNarrative(), component, new HTMLEditorKit());
        latexPanel = new NarrativePanel(parser, new LaTeXNarrative(), component);

        variableSummaryScrollPane = new JScrollPane(variableSummary);
        variableLogScrollPane = new JScrollPane(variableLog);
        treeLogScrollPane = new JScrollPane(treeLog);

        alignmentLog = new AlignmentLog(parser);
        alignmentLogPanel = new AlignmentLogPanel(alignmentLog);

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

    public List<RandomValueLogger> getRandomValueLoggers() {
        return List.of(variableLog, treeLog, variableSummary, alignmentLog);
    }

    @Deprecated
    public StatePanel getConstantsPanel() {
        return (StatePanel) valueScrollPane.getViewport().getView();
    }

    public VariableLog getVariableLog() {
        return variableLog;
    }

    public TreeLog getTreeLog() {
        return treeLog;
    }

    public AlignmentLog getAlignmentLog() {
        return alignmentLog;
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
