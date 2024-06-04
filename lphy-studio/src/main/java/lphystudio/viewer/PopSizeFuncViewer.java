package lphystudio.viewer;

import lphy.base.evolution.coalescent.PopulationFunction;
import lphy.core.model.Value;
import lphystudio.app.graphicalmodelpanel.viewer.Viewer;

import javax.swing.*;

public class PopSizeFuncViewer implements Viewer {

    /**
     * Required by ServiceLoader.
     */
    public PopSizeFuncViewer() {
    }

    @Override
    public boolean match(Object value) {
        return value instanceof PopulationFunction ||
                (value instanceof Value && ((Value) value).value() instanceof PopulationFunction);
    }

    @Override
    public JComponent getViewer(Object value) {
        //TODO create PopulationFunctionComponent((Value<PopulationFunction>) value)
        String valStr;
        if (value instanceof Value<?> vV && vV.value() instanceof PopulationFunction) {
            valStr = vV.value().toString();
        } else
            valStr = value.toString();
        // change to html, which looks better
        if (!valStr.contains("<html>")) {
            valStr = valStr.replaceAll("\n", "<br>");
            valStr = "<html>" + valStr + "</html>";
        }
        return new JLabel(valStr);
    }
    @Override
    public String toString() {
        return "Pop-size function Viewer";
    }

}
