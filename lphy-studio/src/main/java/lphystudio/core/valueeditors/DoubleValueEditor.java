package lphystudio.core.valueeditors;

import lphy.core.model.components.Value;
import lphy.core.util.LoggerUtils;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class DoubleValueEditor extends JTextField {


    public DoubleValueEditor(Value<Double> value)  {

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
                    Double d = Double.parseDouble(text);
                    value.setValue(d);
                } catch (java.lang.NumberFormatException ne) {
                    LoggerUtils.log.warning("Couldn't set value" + (value.isAnonymous() ? "" : (" " + value.getId())) + " to " + text);
                }
            }
        });

        //add(message);
    }
}
