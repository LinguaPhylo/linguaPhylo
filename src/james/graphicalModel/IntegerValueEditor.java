package james.graphicalModel;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class IntegerValueEditor extends JPanel {

    private Value<Integer> value;
    JLabel message = new JLabel();

    public IntegerValueEditor(Value<Integer> value)  {

        this.value = value;
        JLabel label = new JLabel("<html><font color=\"#808080\" >" + value.getId() + ":</font></html>");

        BoxLayout boxLayout = new BoxLayout(this,BoxLayout.LINE_AXIS);
        add(label);

        JTextField textField = new JTextField(value.value().toString(), 12);


        textField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                setValue(textField.getText());
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                setValue(textField.getText());

            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                setValue(textField.getText());
            }

            void setValue(String text) {
                try {
                    Integer n = Integer.parseInt(textField.getText());
                    value.setValue(n);
                    message.setText("");
                } catch (NumberFormatException ne) {
                    message.setText("'" + textField.getText() + "' is not a double.");
                }

            }
        });
        add(textField);

        add(message);
    }
}
