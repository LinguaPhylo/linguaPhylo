package lphystudio.app.graphicalmodelpanel.viewer;

import lphy.core.graphicalmodel.types.MapValue;
import lphy.core.util.LoggerUtils;
import lphystudio.app.graphicalmodelpanel.ViewerRegister;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MapComponent extends JPanel {
    List<JLabel> labels = new ArrayList<>();
    List<JComponent> editors = new ArrayList<>();
    GroupLayout layout = new GroupLayout(this);

    public MapComponent(MapValue mapValue) {
        setLayout(layout);

        generateComponents(mapValue);
    }

    void generateComponents(MapValue mapValue) {

        labels.clear();
        editors.clear();
        removeAll();

        for (Map.Entry<String, Object> entry : mapValue.value().entrySet()) {
            JLabel label = new JLabel(entry.getKey() + ":");
            label.setForeground(Color.gray);
            labels.add(label);

            Object value = entry.getValue();
            JComponent jComponent = ViewerRegister.getJComponentForValue(value);

            if (jComponent == null) {
                LoggerUtils.log.severe("Found no viewer for " + value);
                JLabel jLabel = new JLabel(value.toString());
                jLabel.setForeground(Color.red);
                editors.add(jLabel);
            } else {
                // use only Current panel to edit
                if (jComponent instanceof JTextField textField)
                    textField.setEditable(false);
                editors.add(jComponent);
            }
        }
        GroupLayout.ParallelGroup horizParallelGroup = layout.createParallelGroup(GroupLayout.Alignment.TRAILING);
        GroupLayout.ParallelGroup horizParallelGroup2 = layout.createParallelGroup(GroupLayout.Alignment.LEADING);
        GroupLayout.SequentialGroup vertSequentialGroup = layout.createSequentialGroup();
        for (int i = 0; i < labels.size(); i++) {
            horizParallelGroup.addComponent(labels.get(i));
            horizParallelGroup2.addComponent(editors.get(i));
            GroupLayout.ParallelGroup vertParallelGroup = layout.createParallelGroup(GroupLayout.Alignment.BASELINE);
            vertParallelGroup.addComponent(labels.get(i));
            vertParallelGroup.addComponent(editors.get(i));
            vertSequentialGroup.addGroup(vertParallelGroup);
            vertSequentialGroup.addGap(2);
        }

        GroupLayout.SequentialGroup horizSequentialGroup = layout.createSequentialGroup();
        horizSequentialGroup.addGroup(horizParallelGroup).addGap(5).addGroup(horizParallelGroup2);

        layout.setHorizontalGroup(horizSequentialGroup);

        layout.setVerticalGroup(vertSequentialGroup);
    }


//    public MapComponent(MapValue mapValue) {
//
//        StringBuilder builder = new StringBuilder();
//        builder.append("<html><table border=\"0\"><tr><th>Key</th>");
//        builder.append("<th>Value</th></tr>");
//
//        for (Map.Entry<String, Object> entry : mapValue.value().entrySet()) {
//            builder.append("<tr><td>");
//            builder.append(entry.getKey());
//            builder.append("</td>");
//            builder.append("<td>");
//            builder.append(entry.getValue().toString());
//            builder.append("</td></tr>");
//        }
//
//        builder.append("</table></html>");
//
//        setText(builder.toString());
//    }
}
