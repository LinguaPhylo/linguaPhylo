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

    /**TODO protected ?
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
     * @return a list viewers to register
     */
    @Override
    public List<Viewer> getViewers() {
        return Arrays.stream(ViewerRegister.viewers).toList();
    }

    @Override
    public void register() {

//        Map<String, ? extends Viewer> newTypes = declareViewers();

        addViewers(getViewers(), viewerList, "LPhy studio viewers : ");
    }

    public static JComponent getJComponentForValue(Object object) {
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
