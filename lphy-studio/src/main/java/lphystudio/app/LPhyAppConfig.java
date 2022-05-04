package lphystudio.app;

import lphy.util.LoggerUtils;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import java.awt.*;
import java.io.IOException;
import java.net.URISyntaxException;

/**
 * @author Walter Xie
 */
public final class LPhyAppConfig {

    public static String LPHY_WEB = "https://linguaphylo.github.io";
    public static String LPHY_SOURCE = "https://github.com/LinguaPhylo/linguaPhylo";

    public static final int MASK = Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();

    public static void setFrameLocation(JFrame frame, int maxWidth, int maxHeight) {
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        int width = Math.min(maxWidth, dim.width * 9 / 10);
        int height = Math.min(maxHeight, dim.height * 9 / 10);
        frame.setSize(width, height);
        frame.setLocation(dim.width / 2 - frame.getSize().width / 2,
                dim.height / 2 - frame.getSize().height / 2);
    }

    public static void buildAboutDialog(Component parentComponent, String title, String credits) {
        final JTextPane textPane = new JTextPane();
        textPane.setEditorKit(JTextPane.createEditorKitForContentType("text/html"));
        textPane.setText(credits);
        textPane.setEditable(false);
        textPane.setAutoscrolls(true);
        textPane.addHyperlinkListener(e -> {
            if(e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                if(Desktop.isDesktopSupported()) {
                    try {
                        Desktop.getDesktop().browse(e.getURL().toURI());
                    } catch (IOException | URISyntaxException ex) {
                        LoggerUtils.log.severe(ex.toString());
                        ex.printStackTrace();
                    }
                }
            }
        });
        JOptionPane.showMessageDialog(parentComponent, textPane, title, JOptionPane.PLAIN_MESSAGE, null);
    }

}
