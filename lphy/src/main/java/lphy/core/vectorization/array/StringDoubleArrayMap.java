package lphy.core.vectorization.array;

import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

public class StringDoubleArrayMap extends TreeMap<String, Double[]>  {

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
//
//    @Override
//    public JComponent getComponent(Value<Map<String, Double[]>> value) {
//        String valueString = value.toString();
//
//        JTextArea textArea = new JTextArea(valueString);
//        textArea.setEditable(false);
//
//        return textArea;
//    }
}
