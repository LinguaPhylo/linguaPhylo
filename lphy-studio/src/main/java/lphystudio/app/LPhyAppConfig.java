package lphystudio.app;

import lphy.core.logger.LoggerUtils;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author Walter Xie
 */
public final class LPhyAppConfig {

    public static String LPHY_WEB = "https://linguaphylo.github.io";
    public static String LPHY_SOURCE = "https://github.com/LinguaPhylo/linguaPhylo";

    public static final int MASK = Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();

    public static void setFrameLocation(JFrame frame, int maxWidth, int maxHeight, int offset) {
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        int width = Math.min(maxWidth, dim.width * 9 / 10);
        int height = Math.min(maxHeight, dim.height * 9 / 10);
        frame.setSize(width, height);
        frame.setLocation(dim.width / 2 - frame.getSize().width / 2 + offset,
                dim.height / 2 - frame.getSize().height / 2 + offset);
    }

    public static void setupEcoSys(String appName) {
        System.setProperty("apple.eawt.quitStrategy", "CLOSE_ALL_WINDOWS");
        System.setProperty("com.apple.mrj.application.apple.menu.about.name", appName);
        System.setProperty("apple.laf.useScreenMenuBar", "true");
        System.setProperty("com.apple.macos.useScreenMenuBar", "true");
        System.setProperty("com.apple.mrj.application.growbox.intrudes", "false");
        System.setProperty("apple.awt.fileDialogForDirectories", "true");
        System.setProperty("file.encoding", "UTF-8"); // for windows
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException |
                 IllegalAccessException | UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }
    }

    // icon
    public static BufferedImage getIcon(String iconFile) {
//        URL url = LinguaPhyloStudio.class.getClassLoader().getResource( iconFile );
        InputStream in = LinguaPhyloStudio.class.getClassLoader().getResourceAsStream( iconFile );
        if (in == null) {
            LoggerUtils.log.warning("Cannot find icon ! " + iconFile);
            return null;
        }
        BufferedImage img = null;
        try {
//            File ico = Paths.get(Objects.requireNonNull(url).toURI()).toFile();
            img = ImageIO.read(in);
        } catch (IOException e) {
            LoggerUtils.log.warning("Cannot find icon ! " + iconFile);
        }
        return img;
    }

}
