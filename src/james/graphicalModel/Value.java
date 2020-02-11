package james.graphicalModel;

import james.app.DoubleArrayLabel;
import james.graphicalModel.swing.DoubleValueEditor;
import james.graphicalModel.swing.IntegerValueEditor;
import james.graphicalModel.types.DoubleValue;
import james.graphicalModel.types.IntegerValue;
import james.app.HasComponentView;

import javax.swing.*;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by adru001 on 18/12/19.
 */
public class Value<T> implements Viewable {

    private T value;
    String id;
    List<ValueListener> listeners = new ArrayList<>();

    // the function that produced this value, or null if this value was initialized another way;
    DeterministicFunction<T> function = null;

    public Value(String id, T value) {
        this.id = id;
        this.value = value;
    }

    public Value(String id, T value, DeterministicFunction<T> function) {
        this(id, value);
        this.function = function;
    }

    public T value() {
        return value;
    }

    void print(PrintWriter p) {
        if (function != null) {
            for (Object val : function.getParams().values()) {
                ((Value) val).print(p);
                p.print("\n");
            }

            p.print(id + " = ");
            p.print(function.codeString());
            //p.print(";");
        } else {
            p.print(toString() + ";");
        }
    }

    public String codeString() {
        StringBuilder builder = new StringBuilder();
        if (function != null) {
            builder.append(id + " = ");
            builder.append(function.codeString());
            //p.print(";");
        } else {
            builder.append(toString());
            builder.append(";");
        }
        return builder.toString();
    }

    public String valueDisplayString() {
        return value.toString();
    }

    public String toString() {
        return id + " = " + value;
    }

    public void setValue(T value) {
        this.value = value;
        for (ValueListener listener : listeners) {
            listener.valueSet();
        }
    }

    public JComponent getViewer() {
        if (value instanceof HasComponentView) {
            JComponent component = ((HasComponentView<T>) value).getComponent(this);

            return component;
        }

        if (value() instanceof Double[]) {
            return new DoubleArrayLabel((Value<Double[]>)this);
        }

        if (value.toString().length() < 130) {
            return new JLabel(value.toString());
        } else {
            String valueString = value.toString();
            valueString = valueString.replace(", ", ",\n");

            JTextArea textArea = new JTextArea(valueString);
            textArea.setEditable(false);

            return textArea;
            //JScrollPane scrollPane = new JScrollPane(new JLabel(value.toString()));
            //scrollPane.setPreferredSize(new Dimension(600,50));
            //return scrollPane;
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public DeterministicFunction<T> getFunction() {
        return function;
    }

    public void addValueListener(ValueListener listener) {
        listeners.add(listener);
    }

    public static void traverseGraphicalModel(Value value, GraphicalModelNodeVisitor visitor, boolean post) {
        if (!post) visitor.visitValue(value);

        if (value instanceof RandomVariable) {
            traverseGraphicalModel(((RandomVariable) value).getGenerativeDistribution(), visitor, post);
        } else if (value.getFunction() != null) {
            traverseGraphicalModel(value.getFunction(), visitor, post);
        }
        if (post) visitor.visitValue(value);
    }

    private static void traverseGraphicalModel(DeterministicFunction function, GraphicalModelNodeVisitor visitor, boolean post) {
        if (!post) visitor.visitFunction(function);

        Map<String, Value> map = function.getParams();

        for (Map.Entry<String, Value> e : map.entrySet()) {
            traverseGraphicalModel(e.getValue(), visitor, post);
        }
        if (post) visitor.visitFunction(function);
    }

    public static void traverseGraphicalModel(GenerativeDistribution genDist, GraphicalModelNodeVisitor visitor, boolean post) {
        if (!post) visitor.visitGenDist(genDist);

        Map<String, Value> map = genDist.getParams();

        for (Map.Entry<String, Value> e : map.entrySet()) {
            traverseGraphicalModel(e.getValue(), visitor, post);
        }
        if (post) visitor.visitGenDist(genDist);
    }

}
