package lphy.app;

import lphy.app.components.DoubleComboBoxEditor;
import lphy.core.LPhyParser;
import lphy.graphicalModel.Argument;
import lphy.graphicalModel.Value;

import javax.swing.*;
import java.util.Vector;

public class ArgumentInput extends JPanel {

    Argument argument;

    JComboBox<Value<?>> valueComboBox;

    public ArgumentInput(Argument argument, LPhyParser parser) {
        this.argument = argument;

        Vector<Value<?>> eligibleValues = new Vector(parser.getNamedValuesByType(argument.type));
        eligibleValues.sort((o1, o2) -> {
            if (o1.getId() == null) return 1;
            return o1.getId().compareTo(o2.getId());
        });

        valueComboBox = new JComboBox<>(eligibleValues);

        if (argument.type == Double.class) {
            valueComboBox.setEditable(true);
            valueComboBox.setEditor(new DoubleComboBoxEditor());
        }
        add(valueComboBox);

    }

    public Value getValue() {
        return (Value) valueComboBox.getSelectedItem();
    }

}
