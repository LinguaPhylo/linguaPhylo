package lphystudio.core.awt;

import lphy.core.util.LoggerUtils;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.URISyntaxException;

/**
 * The helper class to handle About menu.
 * If Desktop is not supported, then use the ActionAbout.
 * @author Walter Xie
 */
public class AboutMenuHelper {

    final Component parentComponent;
    final String title, credits;

    public AboutMenuHelper(Component parentComponent, String title, String credits, JMenuBar menuBar) {
        this.parentComponent = parentComponent;
        this.title = title;
        this.credits = credits;

        // deal with About menu
        if (Desktop.isDesktopSupported()) {
            Desktop desktop = Desktop.getDesktop();

            // avoid UnsupportedOperationException of APP_ABOUT
            if (desktop.isSupported(Desktop.Action.APP_ABOUT)) {
                desktop.setAboutHandler(e ->
                        buildAboutDialog(parentComponent, title, credits)
                );
            } else addAboutNotMac(menuBar);
        } else addAboutNotMac(menuBar);

    }

    private void addAboutNotMac(JMenuBar menuBar) {
        JMenu helpMenu = new JMenu("Help");
        helpMenu.setMnemonic('H');
        helpMenu.add(new ActionAbout());
        menuBar.add(helpMenu);
    }

    class ActionAbout extends AbstractAction {
        public ActionAbout() {
            super("About", null);
        }
        @Override
        public void actionPerformed(ActionEvent ae) {
            buildAboutDialog(parentComponent, title, credits);
        }
    } // non Mac About


    private void buildAboutDialog(Component parentComponent, String title, String credits) {
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
