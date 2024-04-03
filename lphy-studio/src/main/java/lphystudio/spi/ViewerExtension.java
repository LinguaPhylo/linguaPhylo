package lphystudio.spi;

import lphy.core.spi.Extension;
import lphystudio.app.graphicalmodelpanel.viewer.Viewer;

import java.util.Arrays;
import java.util.List;

/**
 * The service interface defined for SPI.
 * Implement this interface to create one "Container" provider class
 * for each module of LPhy or its extensions,
 * which should include {@link Viewer}.
 *
 * @author Walter Xie
 */
public interface ViewerExtension extends Extension {

    /**
     * @return the map of new {@link Viewer} implemented in the LPhy extension.
     *         The string key is a keyword to represent this Viewer.
     *         The keyword can be used to identify and initialise the corresponding Viewer.
     */
    List<Viewer> getViewers();

    /**
     * Add new viewers
     * @param newViewers         new viewers defined by {@link #getViewers()}.
     * @param viewers  the map to store all Viewer for this extension.
     * @param message          information message.
     */
    default void addViewers(List<Viewer> newViewers,
                            List<Viewer> viewers,
                                  String message) {
        if (newViewers != null && !newViewers.isEmpty())
            // TODO validate same viewer ?
            viewers.addAll(newViewers);

        System.out.println(message + Arrays.toString(getViewers().toArray(new Viewer[0])));
    }

}
