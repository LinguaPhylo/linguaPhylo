package lphystudio.app;

import lphy.graphicalModel.types.MapValue;

import javax.swing.*;
import java.util.Map;

public class MapComponent extends JLabel {

    public MapComponent(MapValue mapValue) {

        StringBuilder builder = new StringBuilder();
        builder.append("<html><table border=\"0\"><tr><th>Key</th>");
        builder.append("<th>Value</th></tr>");

        for (Map.Entry<String, Object> entry : mapValue.value().entrySet()) {
            builder.append("<tr><td>");
            builder.append(entry.getKey());
            builder.append("</td>");
            builder.append("<td>");
            builder.append(entry.getValue().toString());
            builder.append("</td></tr>");
        }

        builder.append("</table></html>");

        setText(builder.toString());
    }
}
