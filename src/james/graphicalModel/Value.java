package james.graphicalModel;

import javax.swing.*;
import java.io.PrintStream;
import java.io.PrintWriter;

/**
 * Created by adru001 on 18/12/19.
 */
public class Value<T> {

    T value;
    String name;

    public Value(String name, T value) {
        this.name = name;
        this.value = value;
    }

    public T value() {
        return value;
    }

    void print(PrintWriter p) {
        p.print(toString()+";");
    }

    public String toString() {
        return name + " = " + value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    public String getName() {
        return name;
    }

    JLabel label = null;
    public JComponent getViewer() {
        if (label == null) {
            label = new JLabel(toString());
            label.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        }
        return label;
    }
}
