package lphystudio.core.valueeditors;

import lphy.graphicalModel.Value;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class BooleanValueEditor extends JTextField {

    private Value<Boolean> value;

    public BooleanValueEditor(Value<Boolean> value)  {

        this.value = value;

        setText(value.value().toString());
        setColumns(12);


        getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                setValue(getText());
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                setValue(getText());

            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                setValue(getText());
            }

            void setValue(String text) {
                try {
                    Boolean n = Boolean.parseBoolean(getText());
                    value.setValue(n);
                    //message.setText("");
                } catch (NumberFormatException ne) {
                    //message.setText("'" + textField.getText() + "' is not a double.");
                }

            }
        });

        //add(message);
    }
}
