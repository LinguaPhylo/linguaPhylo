package lphystudio.core.awt;

import lphystudio.app.LinguaPhyloStudio;
import lphystudio.app.graphicalmodelcomponent.GraphicalModelComponent;
import lphystudio.app.graphicalmodelpanel.GraphicalModelPanel;
import lphystudio.core.layeredgraph.LayeredGNode;
import lphystudio.core.swing.SpringUtilities;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import static lphystudio.app.LPhyAppConfig.MASK;

/**
 * The helper class to handle Preference in menu.
 * If Desktop is not supported, then PreferencesAction.
 * @author Walter Xie
 */
public class PreferencesHelper {

    final Component parentComponent;
    final GraphicalModelPanel panel;

    public PreferencesHelper(Component parentComponent, GraphicalModelPanel panel, JMenu editMenu) {
        this.parentComponent = parentComponent;
        this.panel = panel;

        // deal with About menu
        if (Desktop.isDesktopSupported()) {
            Desktop desktop = Desktop.getDesktop();

            // avoid UnsupportedOperationException of APP_PREFERENCES
            if (desktop.isSupported(Desktop.Action.APP_PREFERENCES)) {
                desktop.setPreferencesHandler(e -> showPrefDialog(parentComponent, panel));
            } else add2MenuNotMac(editMenu);
        } else add2MenuNotMac(editMenu);

    }

    // fix to add to Edit menu
    private void add2MenuNotMac(JMenu editMenu) {
        editMenu.addSeparator();
        editMenu.setMnemonic(KeyEvent.VK_P);
        editMenu.add(new PreferencesAction());
    }

    class PreferencesAction extends AbstractAction {
        public PreferencesAction() {
            super("Preferences", null);
        }
        @Override
        public void actionPerformed(ActionEvent ae) {
            showPrefDialog(parentComponent, panel);
        }
    } // non Mac About


    public void showPrefDialog(Component parentComponent, GraphicalModelPanel panel) {
        List<JCheckBox> checkBoxList = getPreferencesList(panel);

        JPanel p = new JPanel(new SpringLayout());
        for (int i = 0; i < checkBoxList.size(); i++) {
            p.add(checkBoxList.get(i));
        }

        //Lay out the panel.
        SpringUtilities.makeCompactGrid(p,
                checkBoxList.size(), 1, //rows, cols
                6, 6,        //initX, initY
                6, 6);       //xPad, yPad

//        JScrollPane scrollPane = new JScrollPane();
//        scrollPane.add(p);
        JOptionPane.showMessageDialog(parentComponent, p, "Preferences", JOptionPane.PLAIN_MESSAGE, null);
    }

    private List<JCheckBox> getPreferencesList(GraphicalModelPanel panel) {
        List<JCheckBox> checkBoxList = new ArrayList<>();
        GraphicalModelComponent component = panel.getComponent();
        //CTRL/COMMAND + SHIFT
        int modifiers = MASK + KeyEvent.SHIFT_DOWN_MASK;

        JCheckBox showToolbar = new JCheckBox("Show Toolbar", GraphicalModelComponent.getShowToolbar());
        showToolbar.addActionListener(
                e -> component.setShowToolbar(showToolbar.isSelected(), panel.getToolbar()));
        checkBoxList.add(showToolbar);


        JCheckBox showArgumentLabels = new JCheckBox("Show Argument Names", GraphicalModelComponent.getShowArgumentLabels());
        showArgumentLabels.addActionListener(
                e -> component.setShowArgumentLabels(showArgumentLabels.isSelected()));
        checkBoxList.add(showArgumentLabels);

        JCheckBox showSampledValues = new JCheckBox("Show Sampled Values", LayeredGNode.getShowValueInNode());
         showSampledValues.addActionListener(
                e -> component.setShowValueInNode(showSampledValues.isSelected()));
        checkBoxList.add(showSampledValues);

        JCheckBox useStraightEdges = new JCheckBox("Use Straight Edges", GraphicalModelComponent.getUseStraightEdges());
        useStraightEdges.addActionListener(
                e -> component.setUseStraightEdges(useStraightEdges.isSelected()));
        checkBoxList.add(useStraightEdges);

        //TODO
//        JCheckBox showTreeInAlignmentView = new JCheckBox("Show tree with alignment if available", true);
//        showTreeInAlignmentView.addActionListener(e -> {
//            AlignmentComponent.setShowTreeInAlignmentViewerIfAvailable(showTreeInAlignmentView.isSelected());
//            panel.repaint();
//        });
//        checkBoxList.add(showTreeInAlignmentView);
//
//        JCheckBox showErrorsInErrorAlignmentView = new JCheckBox("Show errors in alignment if available", true);
//        showErrorsInErrorAlignmentView.addActionListener(e -> {
//            AlignmentComponent.showErrorsIfAvailable = showErrorsInErrorAlignmentView.isSelected();
//            panel.repaint();
//        });
//        checkBoxList.add(showErrorsInErrorAlignmentView);
        return checkBoxList;
    }

}
