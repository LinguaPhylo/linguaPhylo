package lphystudio.app;

import lphystudio.app.manager.ExtManager;
import lphystudio.app.manager.ExtManagerPanel;

import javax.swing.*;

/**
 * The app to manage the LPhy extensions.
 * @author Walter Xie
 */
public class ExtManagerApp extends JFrame {

    public static final String APP_NAME = "LPhyExtension Manager";
    static {
        LPhyAppConfig.setupEcoSys(APP_NAME);
    }

    private final int MASK = LPhyAppConfig.MASK;
    private final String VERSION = "0.1.0";

    private final ExtManager extManager;

    public ExtManagerApp() {

        setTitle(APP_NAME + " version " + VERSION);
        // not close lphy studio frame
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        final int MAX_WIDTH = 1200;
        final int MAX_HEIGHT = 600;
        LPhyAppConfig.setFrameLocation(this, MAX_WIDTH, MAX_HEIGHT, 0);

        extManager = new ExtManager();
        ExtManagerPanel guidePanel = new ExtManagerPanel(extManager);

        getContentPane().add(guidePanel);

        setVisible(true);
    }


    public static void main(String[] args) {
        new ExtManagerApp();

    }

}
