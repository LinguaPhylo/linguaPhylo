package lphystudio.app.graphicalmodelpanel;

import lphy.core.model.DeterministicFunction;
import lphy.core.model.GenerativeDistribution;
import lphy.core.model.Value;
import lphy.core.narrative.Narrative;
import lphy.core.narrative.NarrativeUtils;
import lphy.core.parser.GraphicalLPhyParser;
import lphy.core.parser.graphicalmodel.GraphicalModelListener;
import lphystudio.app.graphicalmodelcomponent.GraphicalModelComponent;
import lphystudio.core.narrative.NarrativeLayeredGraph;
import lphystudio.core.narrative.Section;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.EditorKit;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Objects;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;


public class NarrativePanel extends JComponent {
    GraphicalLPhyParser parser;
    JTextPane pane = new JTextPane();
    JScrollPane scrollPane;
    NarrativeLayeredGraph narrative;
    JPanel narrativeInnerPanel;
    GraphicalModelComponent graphicalModelComponent;

    JPopupMenu popupMenu = new JPopupMenu("Preferences");

    JList<String> include = createJListWithDragAndDrop();

    static Preferences preferences = Preferences.userNodeForPackage(NarrativePanel.class);

    public NarrativePanel(GraphicalLPhyParser parser, NarrativeLayeredGraph narrative, GraphicalModelComponent component) {
        this(parser, narrative,  component,null);
    }


    public NarrativePanel(GraphicalLPhyParser parser, NarrativeLayeredGraph narrative, GraphicalModelComponent component, EditorKit editorKit) {
        this.parser = parser;
        this.narrative = narrative;
        this.graphicalModelComponent = component;

        setLayout(new BorderLayout());

        pane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        pane.setEditable(false);

        if (editorKit != null) pane.setEditorKit(editorKit);

        pane.addHyperlinkListener(e -> {
            if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                if (Desktop.isDesktopSupported()) {
                    try {
                        Desktop.getDesktop().browse(e.getURL().toURI());
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    } catch (URISyntaxException uriSyntaxException) {
                        uriSyntaxException.printStackTrace();
                    }
                }
            }
        });


        scrollPane = new JScrollPane(pane);

        setText();

        narrativeInnerPanel = new JPanel();

        BoxLayout boxLayout = new BoxLayout(narrativeInnerPanel, BoxLayout.PAGE_AXIS);
        narrativeInnerPanel.setLayout(boxLayout);

        include.setModel(createIncludeListModel());

        include.getModel().addListDataListener(new ListDataListener() {
            @Override
            public void intervalAdded(ListDataEvent e) { setText(); }

            @Override
            public void intervalRemoved(ListDataEvent e) { setText(); }

            @Override
            public void contentsChanged(ListDataEvent e) { setText(); }
        });

        JList<String> exclude = createJListWithDragAndDrop();
        exclude.setModel(createExcludeListModel());
        exclude.setBorder(BorderFactory.createTitledBorder(exclude.getBorder(), "Exclude"));

        include.setBorder(BorderFactory.createTitledBorder(include.getBorder(), "Include"));

        JSplitPane includeExcludePane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, include, exclude);

        add(includeExcludePane, BorderLayout.SOUTH);

        add(scrollPane, BorderLayout.CENTER);

        parser.addGraphicalModelChangeListener(this::setText);

        component.addGraphicalModelListener(new GraphicalModelListener() {
            @Override
            public void valueSelected(Value value) {

            }

            @Override
            public void generativeDistributionSelected(GenerativeDistribution g) {

            }

            @Override
            public void functionSelected(DeterministicFunction f) {

            }

            @Override
            public void layout() {
                setText();
            }
        });

        parser.addGraphicalModelChangeListener(() -> {
            popupMenu = new JPopupMenu();
            setupPreferencesMenu(narrative);
            pane.setComponentPopupMenu(popupMenu);
        });

    }

    private void setupPreferencesMenu(Narrative narrative) {
        Preferences preferences = narrative.getPreferences();

        try {
            for (String key : preferences.keys()) {
                String val = preferences.get(key, "");
                if (val.equals("true") || val.equals("false")) {
                    Boolean bool = Boolean.parseBoolean(val);
                    JCheckBoxMenuItem menuItem = new JCheckBoxMenuItem(key, bool);
                    popupMenu.add(menuItem);
                    menuItem.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            preferences.putBoolean(key, menuItem.getState());
                            setText();
                        }
                    });
                }

            }
        } catch (BackingStoreException e) {
            e.printStackTrace();
        }

    }


    private JList<String> createJListWithDragAndDrop() {
        JList<String> list = new JList<>();
        list.setDragEnabled(true);
        list.setDropMode(DropMode.INSERT);
        StringMoveHandler.createFor(list);

        return list;
    }

    /**
     * Handles the moving of one or multiple strings between {@link JList}s.
     *
     * @author Matthias Braun
     */
    private static class StringMoveHandler extends TransferHandler {
        private static final long serialVersionUID = 1L;
        private DataFlavor objectArrayFlavor = new DataFlavor(Object[].class, "Array of items");
        // We'll be moving the strings of this list
        private JList<String> list;

        // Clients should use a static factory method to instantiate the handler
        private StringMoveHandler() {
        }

        ;

        public static StringMoveHandler createFor(JList<String> list) {
            StringMoveHandler handler = new StringMoveHandler();
            list.setTransferHandler(handler);
            handler.list = list;
            return handler;
        }

        @Override
        public boolean canImport(TransferSupport info) {
            return info.isDataFlavorSupported(objectArrayFlavor);
        }

        @Override
        public boolean importData(TransferSupport transferSupport) {
            Transferable t = transferSupport.getTransferable();

            boolean success = false;
            try {
                Object[] importedData = (Object[]) t.getTransferData(objectArrayFlavor);
                addToListModel(importedData);
                success = true;
            } catch (UnsupportedFlavorException | IOException e) {
                e.printStackTrace();
            }
            return success;
        }

        private void addToListModel(Object[] importedData) {
            JList.DropLocation loc = list.getDropLocation();
            int dropIndex = loc.getIndex();

            DefaultListModel<String> listModel = (DefaultListModel<String>) list.getModel();
            for (int i = 0; i < importedData.length; i++) {
                Object elem = importedData[i];
                if (elem instanceof String) {
                    listModel.add(dropIndex + i, (String) elem);
                } else {
                    System.err.println("Imported data contained something else than strings: " + elem);
                }
            }
        }

        @Override
        public int getSourceActions(JComponent c) {
            return TransferHandler.COPY_OR_MOVE;
        }

        @Override
        public Transferable createTransferable(JComponent source) {
            // We need the values from the list as an object array, otherwise the data flavor won't match in importData
            @SuppressWarnings("deprecation")
            Object[] valuesToTransfer = list.getSelectedValues();
            return new Transferable() {
                @Override
                public DataFlavor[] getTransferDataFlavors() {
                    return new DataFlavor[]{objectArrayFlavor};
                }

                @Override
                public boolean isDataFlavorSupported(DataFlavor flavor) {
                    return Objects.equals(objectArrayFlavor, flavor);
                }

                @Override
                public Object getTransferData(DataFlavor flavor)
                        throws UnsupportedFlavorException, IOException {
                    if (isDataFlavorSupported(flavor)) {
                        return valuesToTransfer;
                    } else {
                        throw new UnsupportedFlavorException(flavor);
                    }
                }
            };
        }

        @Override
        protected void exportDone(JComponent source, Transferable data, int action) {
            if (action == TransferHandler.MOVE) {
                try {
                    Object[] exportedData = (Object[]) data.getTransferData(objectArrayFlavor);
                    removeFromListModel(exportedData);
                } catch (UnsupportedFlavorException | IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private void removeFromListModel(Object[] dataToRemove) {
            DefaultListModel<String> listModel = (DefaultListModel<String>) list.getModel();
            for (Object elemToRemove : dataToRemove) {
                boolean removedSuccessfully = listModel.removeElement(elemToRemove);
                if (!removedSuccessfully) {
                    System.err.println("Source model did not contain exported data");
                }
            }
        }
    }

    private static ListModel<String> createExcludeListModel() {
        DefaultListModel<String> listModel = new DefaultListModel<>();
        return listModel;
    }

    private static DefaultListModel<String> createIncludeListModel() {
        DefaultListModel<String> listModel = new DefaultListModel<>();
        for (Section section :Section.values()) {
            listModel.addElement(section.name);
        }
        return listModel;
    }

//    private void buildNarrativeMenu() {
//
//        narrativeMenu = new JMenu("Narrative");
//
//        JCheckBoxMenuItem showCodeMenuItem = new JCheckBoxMenuItem("Show Code");
//        showCodeMenuItem.setState(getShowCode());
//        narrativeMenu.add(showCodeMenuItem);
//
//        showCodeMenuItem.addActionListener(e -> setShowCode(showCodeMenuItem.getState()));
//
//        JCheckBoxMenuItem showPosteriorMenuItem = new JCheckBoxMenuItem("Show Posterior");
//        showPosteriorMenuItem.setState(getShowPosterior());
//        narrativeMenu.add(showPosteriorMenuItem);
//
//        showPosteriorMenuItem.addActionListener(e -> setShowPosterior(showPosteriorMenuItem.getState()));
//
//        JCheckBoxMenuItem showGraphicalModel = new JCheckBoxMenuItem("Show Graphical Model");
//        showGraphicalModel.setState(getShowGraphicalModel());
//        narrativeMenu.add(showGraphicalModel);
//
//        showGraphicalModel.addActionListener(e -> setShowGraphicalodel(showGraphicalModel.getState()));
//
//        JCheckBoxMenuItem showReferences = new JCheckBoxMenuItem("Show Graphical Model");
//        showGraphicalModel.setState(getShowGraphicalModel());
//        narrativeMenu.add(showGraphicalModel);
//
//        showGraphicalModel.addActionListener(e -> setShowGraphicalodel(showGraphicalModel.getState()));
//
//    }


    private void setText() {

        try {
            pane.getDocument().remove(0, pane.getDocument().getLength());
        } catch (BadLocationException e) {
            e.printStackTrace();
        }

        String text = narrative.beginDocument(parser.getName());

        for (int i = 0; i < include.getModel().getSize(); i++) {
            String item = include.getModel().getElementAt(i);

            Section section = Section.Model;
            try {
                section  = Section.valueOf(item);
            } catch (IllegalArgumentException e) {
                for (Section s : Section.values()) {
                    if (s.name.equals(item)) {
                        section = s;
                        break;
                    }
                }
            }

            switch (section) {
                case Data:
                    text += NarrativeUtils.getNarrative(parser, narrative, true, false);
                    break;
                case Model:
                    text += NarrativeUtils.getNarrative(parser, narrative, false, true);
                    break;
                case Code:
                    text += narrative.section("Code");
                    text += narrative.codeBlock(parser, 11);
                    break;
                case Posterior:
                    text += narrative.section("Posterior");
                    text += narrative.posterior(parser);
                    break;
                case References:
                    text += narrative.referenceSection();
                    break;
                case GraphicalModel:
                    text += narrative.section("Graphical Model");
                    if (graphicalModelComponent.properLayeredGraph != null)
                        text += narrative.graphicalModelBlock(parser, graphicalModelComponent.properLayeredGraph);
                    break;
            }
        }
        text += narrative.endDocument();

        pane.setText(text);

        pane.setCaretPosition(0);
    }
}
