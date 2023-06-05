package lphystudio.core.valueeditor;

import lphy.core.model.component.Value;
import lphy.core.parser.LPhyMetaParser;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionListener;

public class FieldComboBoxEditor implements ComboBoxEditor {

    LPhyMetaParser parser;
    Class type;

    JTextField editor = new JTextField() {

        @Override
        public boolean isValidateRoot() {
            return false;
        }
    };
    boolean isEdited = false;
    Value currentValue;


    public FieldComboBoxEditor(LPhyMetaParser parser, Class type) {
        this.parser = parser;
        this.type = type;
        editor.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                handleEdit();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                handleEdit();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                handleEdit();
            }
        });
    }

    @Override
    public Component getEditorComponent() {
        return editor;
    }

    private void handleEdit() {
        Value value = parser.getValue(editor.getText(), LPhyMetaParser.Context.model);
        isEdited = value == null;
        if (!isEdited) {
            currentValue = value;
        }
    }

    @Override
    public void setItem(Object anObject) {

        if (anObject instanceof String) {

            String str = (String) anObject;
            if (parser.hasValue(str, LPhyMetaParser.Context.model)) {
                Value value = parser.getValue(str, LPhyMetaParser.Context.model);

                Class c = value.value().getClass();

                if (type.isAssignableFrom(c) || type.arrayType().isAssignableFrom(c)) {
                    currentValue = value;
                    editor.setText(value.getId());
                } else {
                    throw new RuntimeException("Should be value of type " + type + " but found type " + value.value().getClass());
                }
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
