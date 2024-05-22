package lphystudio.core.valueeditor;

import lphy.core.logger.LoggerUtils;
import lphy.core.model.Value;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public abstract class AbstractValueEditor<T> extends JTextField {

    protected Value<T> value;
    //JLabel message = new JLabel();

    public AbstractValueEditor(Value<T> value) {
        this.value = value;
        //JLabel label = new JLabel("<html><font color=\"#808080\" >" + value.getId() + ":</font></html>");

        //BoxLayout boxLayout = new BoxLayout(this,BoxLayout.LINE_AXIS);
        //add(label);

        setText(value.value().toString());
        setColumns(12);

        // press enter, not update while typing
//        addActionListener(e -> {
//            setValue(getText());
//        });

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
        });

        //add(message);
    }

    protected abstract T parseValue(String text);

    protected abstract Class<T> getType();

    protected void setValue(String text) {
        if (!text.trim().isEmpty()) {
            try {
                T v = parseValue(text);
                value.setValue(v);
                //message.setText("");
            } catch (NumberFormatException ne) {
                //message.setText("'" + textField.getText() + "' is not a double.");
                LoggerUtils.log.warning("Couldn't set (" + getType().getSimpleName() + ") value" +
                        (value.isAnonymous() ? "" : (" " + value.getId())) + " to " + text);
            }
        }
    }

}
