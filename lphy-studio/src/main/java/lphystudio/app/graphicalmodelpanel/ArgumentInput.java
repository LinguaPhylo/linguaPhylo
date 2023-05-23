package lphystudio.app.graphicalmodelpanel;

import lphy.core.LPhyMetaParser;
import lphy.graphicalModel.Argument;
import lphy.graphicalModel.Value;
import lphystudio.core.swing.BoundsPopupMenuListener;
import lphystudio.core.valueeditors.FieldComboBoxEditor;

import javax.swing.*;
import java.util.List;
import java.util.Vector;
import java.util.stream.Collectors;

public class ArgumentInput extends JPanel {

    Argument argument;

    JComboBox<String> valueComboBox;

    public ArgumentInput(Argument argument, LPhyMetaParser parser) {
        this.argument = argument;

        List<String> names = parser.getNamedValuesByType(argument.type).stream().map(Value::getId).collect(Collectors.toList());
        names.addAll(parser.getNamedValuesByType(argument.type.arrayType()).stream().map(Value::getId).collect(Collectors.toList()));

        Vector<String> eligibleValues = new Vector<>(names);
        // allow arrays as well for vectorization
        eligibleValues.sort(String::compareTo);

        BoundsPopupMenuListener boundsPopupMenuListener = new BoundsPopupMenuListener(true, false);
        valueComboBox = new JComboBox<>(eligibleValues);
        valueComboBox.addPopupMenuListener(boundsPopupMenuListener);
        valueComboBox.setPrototypeDisplayValue((String)valueComboBox.getSelectedItem());
        valueComboBox.setFont(GraphicalModelInterpreter.interpreterFont);

        valueComboBox.addActionListener(e -> valueComboBox.setPrototypeDisplayValue((String)valueComboBox.getSelectedItem()));

        if (argument.type == Double.class || argument.type == Integer.class || argument.type == Number.class) {
            valueComboBox.setEditable(true);
            valueComboBox.setEditor(new FieldComboBoxEditor(parser, argument.type));
        }
        add(valueComboBox);
    }

    public Value getValue() {
        return (Value) valueComboBox.getSelectedItem();
    }

}
