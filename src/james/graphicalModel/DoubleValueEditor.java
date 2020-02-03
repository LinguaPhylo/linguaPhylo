package james.graphicalModel;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class DoubleValueEditor extends JPanel {

    private Value<Double> value;
    JLabel message = new JLabel();

    public DoubleValueEditor(Value<Double> value)  {

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
                    Double d = Double.parseDouble(textField.getText());
                    value.setValue(d);
                    message.setText("");
                } catch (java.lang.NumberFormatException ne) {
                    message.setText("'" + textField.getText() + "' is not a double.");
                }

            }
        });
        add(textField);

        add(message);
    }
}
