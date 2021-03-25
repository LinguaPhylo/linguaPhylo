package lphy.app;

import lphy.core.functions.VectorizedFunction;
import lphy.graphicalModel.DeterministicFunction;
import lphy.graphicalModel.Generator;
import lphy.graphicalModel.Value;
import lphy.graphicalModel.Vector;
import lphy.graphicalModel.types.CompoundVectorValue;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Method;
import java.util.Iterator;

public class VectorComponent extends JComponent {

    Vector vectorValue;

    public  VectorComponent(Vector vectorValue) {
        this.vectorValue = vectorValue;

        int size = vectorValue.size();

        GridLayout gridLayout = new GridLayout((int)Math.ceil(size/2.0),2,5,5);
        setLayout(gridLayout);

        // such as bSiteModel[3] => SiteModel[3]
//        if (vectorValue instanceof CompoundVectorValue) {
//            Generator generator = ((CompoundVectorValue) vectorValue).getGenerator();
//            if (generator instanceof VectorizedFunction) {
//                Iterator<? extends DeterministicFunction<?>> it = ((VectorizedFunction<?>) generator).getComponentFunctions().iterator();
//                while (it.hasNext()) {
//                    DeterministicFunction fun = it.next();
//                    JComponent jComponent = ViewerRegister.getJComponentForValue(fun.generate());
//                    if (jComponent != null) {
//                        add(jComponent);
//                    } else {
//                        JLabel jLabel = new JLabel(fun.toString());
//                        jLabel.setForeground(Color.red);
//                        add(jLabel);
//                    }
//                }
//            }
//        } else {
            // normal process
            for (int i = 0; i < size; i++) {
                Object component = vectorValue.getComponent(i);
                JComponent jComponent = ViewerRegister.getJComponentForValue(component);
                if (jComponent != null) {
                    add(jComponent);
                } else {
                    JLabel jLabel = new JLabel(component.toString());
                    jLabel.setForeground(Color.red);
                    add(jLabel);
                }
            }
//        }
    }
}
