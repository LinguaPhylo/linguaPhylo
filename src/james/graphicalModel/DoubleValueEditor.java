package james.graphicalModel;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class DoubleValueEditor extends JTextField {

    private Value<Double> value;

    public DoubleValueEditor(Value<Double> value)  {

        this.value = value;
        //JLabel label = new JLabel("<html><font color=\"#808080\" >" + value.getId() + ":</font></html>");

        //BoxLayout boxLayout = new BoxLayout(this,BoxLayout.LINE_AXIS);
        //add(label);

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
                    Double d = Double.parseDouble(getText());
                    value.setValue(d);
                    //message.setText("");
                } catch (java.lang.NumberFormatException ne) {
                    //message.setText("'" + textField.getText() + "' is not a double.");
                }

            }
        });

        //add(message);
    }
}
