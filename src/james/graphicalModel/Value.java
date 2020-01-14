package james.graphicalModel;

import james.swing.HasComponentView;

import javax.swing.*;
import java.io.PrintStream;
import java.io.PrintWriter;

/**
 * Created by adru001 on 18/12/19.
 */
public class Value<T> {

    T value;
    String id;

    public Value(String id, T value) {
        this.id = id;
        this.value = value;
    }

    public T value() {
        return value;
    }

    void print(PrintWriter p) {
        p.print(toString()+";");
    }

    public String toString() {
        return id + " = " + value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    JLabel label = null;
    static int BORDER_SIZE = 20;
    public JComponent getViewer() {
        if (value instanceof HasComponentView) {
            JComponent component = ((HasComponentView)value).getComponent();
            component.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(0, 0,BORDER_SIZE, BORDER_SIZE ), id));
            return component;
        }

        if (label == null) {
            label = new JLabel(toString());
            label.setBorder(BorderFactory.createEmptyBorder(BORDER_SIZE, BORDER_SIZE, BORDER_SIZE, BORDER_SIZE ));
        }
        return label;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
