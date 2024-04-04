package lphystudio.spi;

import lphy.core.model.Value;
import lphystudio.app.graphicalmodelpanel.ViewerRegister;
import lphystudio.app.graphicalmodelpanel.viewer.Viewer;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The "Container" provider class that implements SPI
 * which include a list of {@link Viewer} to extend.
 * It requires a public no-args constructor.
 * @author Walter Xie
 */
public class StudioViewerImpl implements ViewerExtension {

    /**TODO this should be Map<T, Viewer> where T is the class, e.g. Alignment, Double, ...
     * LPhy studio {@link Viewer}
     */
    protected static List<Viewer> viewerList;

    /**
     * Required by ServiceLoader.
     */
    public StudioViewerImpl() {
        if (viewerList == null) viewerList = new ArrayList<>();
    }


    /**
     * Check {@link ViewerRegister} how to create the viewers.
     * @return a list viewers to register in this extension
     */
    @Override
    public List<Viewer> getViewers() {
        return Arrays.stream(ViewerRegister.viewers).toList();
    }

    /**
     * this must be overwritten in each lphy extension.
     */
    @Override
    public void register() {
        // TODO add should check if viewer for a type already exists, after getViewers() changes to Map<T, Viewer>.
        addViewers(getViewers(), viewerList, "LPhy studio viewers : ");
    }

    /**
     * Call this in the panel to show the corresponding viewer given a value.
     * @param object  a value
     * @return        the corresponding viewer
     */
    public static JComponent getJComponentForValue(Object object) {
        // loop through all viewers.
        for (Viewer viewer : viewerList) {
            if (viewer.match(object)) return viewer.getViewer(object);
        }
//            LoggerUtils.log.severe("Found no viewer for " + object);
        String label;
        if (object instanceof Value) {
            label = ((Value) object).getLabel();
        } else {
            label = object.toString();
        }
        JLabel jLabel = new JLabel(label);
        jLabel.setForeground(Color.red);
        return jLabel;
    }

    public String getExtensionName() {
        return "LPhy studio viewers";
    }

}
