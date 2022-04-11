package lphystudio.app.graphicalmodelpanel;

import lphy.graphicalModel.Value;
import lphy.graphicalModel.Vector;
import lphy.graphicalModel.types.CompoundVectorValue;

import javax.swing.*;
import java.awt.*;

public class VectorComponent extends JComponent {

    Vector vectorValue;

    public VectorComponent(Vector vectorValue) {
        this.vectorValue = vectorValue;

        int size = vectorValue.size();

        GridLayout gridLayout = new GridLayout((int) Math.ceil(size / 2.0), 2, 5, 5);
        setLayout(gridLayout);

        for (int i = 0; i < size; i++) {
            JComponent jComponent;
            String label = "";
            if (vectorValue instanceof CompoundVectorValue) {
                // need to get the Value not component inside Value
                Value compValue = ((CompoundVectorValue) vectorValue).getComponentValue(i);
                label = compValue.getLabel();
                // to get methodInfoViewer
                // if (MethodInfoPanel.hasZeroParamMethodInfo(compValue)) {
                jComponent = ViewerRegister.getJComponentForValue(compValue);
                // }
            } else {
                // for not CompoundVectorValue
                Object component = vectorValue.getComponent(i);
                label = component.toString();
                jComponent = ViewerRegister.getJComponentForValue(component);
            }

            if (jComponent != null) {
                add(jComponent);
            } else {
                JLabel jLabel = new JLabel(label);
                jLabel.setForeground(Color.red);
                add(jLabel);
            }
        }
    }
}
