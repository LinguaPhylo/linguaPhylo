package lphy.app.components;

import lphy.graphicalModel.Value;
import lphy.graphicalModel.types.DoubleValue;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionListener;

public class DoubleComboBoxEditor implements ComboBoxEditor {

    JTextField editor = new JTextField(12);

    boolean isEdited = false;
    Value currentValue;

    @Override
    public Component getEditorComponent() {
        return editor;
    }

    @Override
    public void setItem(Object anObject) {
        if (anObject instanceof Value) {
            Value value = (Value)anObject;
            if (value.value() instanceof Double) {
                currentValue = value;
                if (value.isAnonymous()) {
                    editor.setText(value.toString());
                } else {
                    editor.setText(value.getId());
                }
                isEdited = false;
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
                throw new RuntimeException("Should be Double Value!");
            }
        } else if (anObject == null) {
            isEdited = true;
            editor.setText("0.0");
            
        } else throw new RuntimeException("Should be a Value, but is a " + anObject.getClass());
    }

    @Override
    public Object getItem() {
        if (isEdited) {
            Double d = Double.parseDouble(editor.getText());
            return new DoubleValue(null, d);
        } else {
            return currentValue;
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
