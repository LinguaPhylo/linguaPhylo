package james.graphicalModel.swing;

import james.graphicalModel.Value;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class IntegerValueEditor extends JTextField {

    private Value<Integer> value;
    //JLabel message = new JLabel();

    public IntegerValueEditor(Value<Integer> value)  {

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
                    Integer n = Integer.parseInt(getText());
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
