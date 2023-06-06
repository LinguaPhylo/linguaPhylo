package lphystudio.app.graphicalmodelpanel;

import lphy.core.exception.LoggerUtils;
import lphy.core.model.RandomVariable;
import lphy.core.model.Value;
import lphy.core.parser.GraphicalLPhyParser;
import lphy.core.parser.graphicalmodel.GraphicalModelUtils;
import lphystudio.core.valueeditor.Abstract2DEditor;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class StatePanel extends JPanel {

    GraphicalLPhyParser parser;

    List<JLabel> labels = new ArrayList<>();
    List<JComponent> editors = new ArrayList<>();
    GroupLayout layout = new GroupLayout(this);

    boolean includeRandomValues;
    boolean includeFixedValues;

    public StatePanel(GraphicalLPhyParser parser, boolean includeFixedValues, boolean includeRandomValues) {
        this.parser = parser;

        this.includeFixedValues = includeFixedValues;
        this.includeRandomValues = includeRandomValues;

        setLayout(layout);

        generateComponents();

        parser.addGraphicalModelChangeListener(this::generateComponents);
    }

    @Deprecated
    public void setTextFieldEditable(boolean editable) {
        for (JComponent jc : editors) {
            if (jc instanceof JTextField textField)
                textField.setEditable(editable);
        }
        repaint();
    }

    void generateComponents() {

        labels.clear();
        editors.clear();
        removeAll();

        for (Value value : GraphicalModelUtils.getAllValuesFromSinks(parser)) {
            if ((value.isRandom() && includeRandomValues) || (!value.isRandom() && includeFixedValues)) {
                JLabel label = new JLabel(value.getLabel()+":");
                label.setForeground(Color.gray);
                labels.add(label);

                JComponent jComponent = ViewerRegister.getJComponentForValue(value);

                if (jComponent == null) {
                    LoggerUtils.log.severe("Found no viewer for " + value);
                    JLabel jLabel = new JLabel(value.value().toString());
                    jLabel.setForeground(Color.red);
                    editors.add(jLabel);
                } else {
                    // use only Current panel to edit
                    if (jComponent instanceof JTextField textField)
                        textField.setEditable(false);
                    if (jComponent instanceof Abstract2DEditor editor)
                        editor.redraw2DArray(false);
                    editors.add(jComponent);
                }
            }
        }
        GroupLayout.ParallelGroup horizParallelGroup = layout.createParallelGroup(GroupLayout.Alignment.TRAILING);
        GroupLayout.ParallelGroup horizParallelGroup2 = layout.createParallelGroup(GroupLayout.Alignment.LEADING);
        GroupLayout.SequentialGroup vertSequentialGroup = layout.createSequentialGroup();
        for (int i = 0; i < labels.size(); i++) {
            horizParallelGroup.addComponent(labels.get(i));
            horizParallelGroup2.addComponent(editors.get(i));
            GroupLayout.ParallelGroup vertParallelGroup = layout.createParallelGroup(GroupLayout.Alignment.BASELINE);
            vertParallelGroup.addComponent(labels.get(i));
            vertParallelGroup.addComponent(editors.get(i));
            vertSequentialGroup.addGroup(vertParallelGroup);
            vertSequentialGroup.addGap(2);
        }

        GroupLayout.SequentialGroup horizSequentialGroup = layout.createSequentialGroup();
        horizSequentialGroup.addGroup(horizParallelGroup).addGap(5).addGroup(horizParallelGroup2);

        layout.setHorizontalGroup(horizSequentialGroup);

        layout.setVerticalGroup(vertSequentialGroup);
    }

    private boolean isFixedValue(Value value) {
        return !(value instanceof RandomVariable) && value.getGenerator() == null;
    }
}
