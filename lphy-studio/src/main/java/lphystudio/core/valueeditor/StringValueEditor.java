package lphystudio.core.valueeditor;

import lphy.core.model.component.Value;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class StringValueEditor extends JTextField {

    private Value<String> value;

    public StringValueEditor(Value<String> value)  {

        this.value = value;

        setText(value.value());
        setColumns(20);

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
                value.setValue(text);
            }
        });
    }
}
