package lphystudio.app;

import lphyext.manager.DependencyUtils;
import lphystudio.app.modelguide.ModelGuide;
import lphystudio.app.modelguide.ModelGuidePanel;

import javax.swing.*;
import java.awt.*;

/**
 * @author Walter Xie
 */
public class ModelGuideApp extends JFrame {
    public static final int MAX_WIDTH = 800;
    private static final int MAX_HEIGHT = 800;

    private static final String APP_NAME = "Model Guide";

    // use MANIFEST.MF to store version in jar, or use system property in development,
    // otherwise VERSION = "DEVELOPMENT"
    private final String VERSION;

    private final ModelGuide modelGuide;

    static {
        System.setProperty("apple.eawt.quitStrategy", "CLOSE_ALL_WINDOWS");
        System.setProperty("com.apple.mrj.application.apple.menu.about.name", APP_NAME);
        System.setProperty("apple.laf.useScreenMenuBar", "true");
        System.setProperty("com.apple.macos.useScreenMenuBar", "true");
        System.setProperty("com.apple.mrj.application.growbox.intrudes", "false");
        System.setProperty("apple.awt.fileDialogForDirectories", "true");
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException |
                IllegalAccessException | UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }
    }

    private static final int MASK = Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();

    public ModelGuideApp() {
        VERSION = DependencyUtils.getVersion(ModelGuideApp.class, "model.guide.version");
        // main frame
        setTitle(APP_NAME + " version " + VERSION);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        LPhyAppConfig.setFrameLocation(this, MAX_WIDTH, MAX_HEIGHT);

        modelGuide = new ModelGuide();
        ModelGuidePanel guidePanel = new ModelGuidePanel(modelGuide);
        getContentPane().add(guidePanel, BorderLayout.CENTER);

        if (Desktop.isDesktopSupported()) {
            Desktop desktop = Desktop.getDesktop();
            desktop.setAboutHandler(e ->
                    LPhyAppConfig.buildAboutDialog(this, APP_NAME + " v " + VERSION, getHTMLCredits())
            );
        }

//        JMenuBar menuBar = new JMenuBar();
//        JMenu summMenu = new JMenu("Summary");
//        summMenu.setMnemonic(KeyEvent.VK_S);
//        menuBar.add(summMenu);
//
//        JMenuItem showMenuItem = new JMenuItem("Show Summary...");
//        showMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_H, MASK));
//        showMenuItem.addActionListener(e -> {
//
//        });
//        summMenu.add(showMenuItem);
//
//        setJMenuBar(menuBar);
        setVisible(true);
    }

    private String getHTMLCredits() {
        return "<html><body width='%1s'><h3>Created by Walter Xie</h3>"+
                "<p>The Centre for Computational Evolution<br>"+
                "University of Auckland<br></p>"+
                "<p>Homepage :<br>"+
                "<a href=\""+LPhyAppConfig.LPHY_WEB+"\">"+LPhyAppConfig.LPHY_WEB+"</a></p>"+
                "<p>Source code distributed under the GNU Lesser General Public License Version 3</p>"+
                "<p>Require Java 17, current Java version " + System.getProperty("java.version") + "</p></html>";
    }

    public static void main(String[] args) {

        System.setProperty("model.guide.version", "0.0.1-SNAPSHOT");
        ModelGuideApp frame = new ModelGuideApp();

    }

}
