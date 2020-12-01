package lphy.app.components;

import lphy.core.LPhyParser;
import lphy.graphicalModel.Value;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionListener;

public class FieldComboBoxEditor implements ComboBoxEditor {

    LPhyParser parser;
    Class type;

    JTextField editor = new JTextField() {

        @Override
        public boolean isValidateRoot() {
            return false;
        }
    };
    boolean isEdited = false;
    Value currentValue;


    public FieldComboBoxEditor(LPhyParser parser, Class type) {
        this.parser = parser;
        this.type = type;
    }

    @Override
    public Component getEditorComponent() {
        return editor;
    }

    @Override
    public void setItem(Object anObject) {

        if (anObject instanceof String) {

            String str = (String) anObject;
            if (parser.hasValue(str, LPhyParser.Context.model)) {
                Value value = parser.getValue(str, LPhyParser.Context.model);
                if (type.isAssignableFrom(value.value().getClass())) {
                    currentValue = value;
                    isEdited = false;
                    editor.setText(value.getId());
                    editor.getDocument().addDocumentListener(new DocumentListener() {
                        @Override
                        public void insertUpdate(DocumentEvent e) {
                            isEdited = true;
                        }

                        @Override
                        public void removeUpdate(DocumentEvent e) {
                            isEdited = true;

                        }

                        @Override
                        public void changedUpdate(DocumentEvent e) {
                            isEdited = true;
                        }
                    });
                } else {
                    throw new RuntimeException("Should be value of type " + type);
                }
            } else {
                isEdited = true;
            }
        } else if (anObject != null) throw new RuntimeException("Should be a string, but is a " + anObject.getClass());
    }

    @Override
    public Object getItem() {
        if (isEdited) {
            return editor.getText();
        } else {
            return currentValue.getId();
        }
    }

    @Override
    public void selectAll() {
        editor.selectAll();
    }

    @Override
    public void addActionListener(ActionListener l) {
        editor.addActionListener(l);
    }

    @Override
    public void removeActionListener(ActionListener l) {
        editor.removeActionListener(l);
    }
}
