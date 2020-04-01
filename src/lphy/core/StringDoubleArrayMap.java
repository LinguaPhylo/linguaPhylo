package lphy.core;

import lphy.app.HasComponentView;
import lphy.graphicalModel.Value;

import javax.swing.*;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

public class StringDoubleArrayMap extends TreeMap<String, Double[]> implements HasComponentView<Map<String, Double[]>> {

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{\n");
        for (Map.Entry<String, Double[]> entry : entrySet()) {
            builder.append("  ");
            builder.append(entry.getKey());
            builder.append(": ");
            builder.append(Arrays.toString(entry.getValue()));
            builder.append("\n");
        }
        builder.append("}\n");
        return builder.toString();
    }

    @Override
    public JComponent getComponent(Value<Map<String, Double[]>> value) {
        String valueString = value.toString();

        JTextArea textArea = new JTextArea(valueString);
        textArea.setEditable(false);

        return textArea;
    }
}
