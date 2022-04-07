package lphystudio.app.graphicalmodelpanel;

import lphy.graphicalModel.MethodInfo;
import lphy.graphicalModel.Value;
import lphy.util.LoggerUtils;
import lphystudio.app.viewer.ViewerRegister;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class MethodInfoPanel extends JPanel {

    Value value;

    List<JLabel> labels = new ArrayList<>();
    List<JComponent> editors = new ArrayList<>();
    GroupLayout layout = new GroupLayout(this);

    public MethodInfoPanel(Value value) {
        this.value = value;

        setLayout(layout);

        generateComponents();

    }

    public static boolean hasZeroParamMethodInfo(Value value) {

        Class c = value.value().getClass();

        for (Method method : c.getMethods()) {
            MethodInfo methodInfo = method.getAnnotation(MethodInfo.class);
            if (methodInfo != null && method.getParameterCount() == 0) return true;
        }
        return false;
    }

    void generateComponents() {

        labels.clear();
        editors.clear();
        removeAll();

        Class c = value.value().getClass();

        Method[] methods = c.getMethods();
        Arrays.sort(methods, Comparator.comparing(Method::getName));

//        JLabel id = new JLabel(value.getUniqueId() + ":");
//        id.setForeground(Color.gray);
//        labels.add(id);
//        editors.add(new JLabel(""));

        for (Method method : methods) {

            MethodInfo methodInfo = method.getAnnotation(MethodInfo.class);
            if (methodInfo != null && method.getParameterCount() == 0) {

                try {
                    Object methodObject = method.invoke(value.value());

                    JLabel label = new JLabel(method.getName()+":");
                    label.setForeground(Color.gray);
                    labels.add(label);

                    JComponent jComponent = null;
                    if (methodObject != null) jComponent = ViewerRegister.getJComponentForValue(methodObject);

                    if (methodObject == null) {
                        JLabel jLabel = new JLabel("null");
                        jLabel.setForeground(Color.gray);
                        editors.add(jLabel);
                    } else if (jComponent == null) {
                        LoggerUtils.log.severe("Found no viewer for " + methodObject);
                        JLabel jLabel = new JLabel(methodObject.toString());
                        jLabel.setForeground(Color.red);
                        editors.add(jLabel);
                    } else {
                        editors.add(jComponent);
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
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
}
