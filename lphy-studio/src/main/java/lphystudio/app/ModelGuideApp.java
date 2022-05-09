package lphystudio.app;

import lphystudio.app.modelguide.LatexPane;
import lphystudio.app.modelguide.ModelGuide;
import lphystudio.app.modelguide.ModelGuidePanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;

/**
 * @author Walter Xie
 */
public class ModelGuideApp extends JFrame {

    public static final String APP_NAME = "Model Guide";
    static {
        LPhyAppConfig.setupEcoSys(APP_NAME);
    }

    private final int MASK = LPhyAppConfig.MASK;
    private final String VERSION = "0.1.0";

    private final ModelGuide modelGuide;

    public ModelGuideApp() {
//        VERSION = DependencyUtils.getVersion(ModelGuideApp.class, "model.guide.version");
        setTitle(APP_NAME + " version " + VERSION);
        // not close lphy studio frame
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        final int MAX_WIDTH = 800;
        final int MAX_HEIGHT = 800;
        LPhyAppConfig.setFrameLocation(this, MAX_WIDTH, MAX_HEIGHT);

        modelGuide = new ModelGuide();
        ModelGuidePanel guidePanel = new ModelGuidePanel(modelGuide);

        if (Desktop.isDesktopSupported()) {
            Desktop desktop = Desktop.getDesktop();
            desktop.setAboutHandler(e ->
                    LPhyAppConfig.buildAboutDialog(this, APP_NAME + " v " + VERSION, getHTMLCredits())
            );
        }

        JTabbedPane tabbedPane = new JTabbedPane();
        //The following line enables to use scrolling tabs.
        tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);

        tabbedPane.addTab("Models", guidePanel);
        tabbedPane.setMnemonicAt(0, KeyEvent.VK_1);

        LatexPane latexPane = new LatexPane(modelGuide);
        JScrollPane scrollPane = new JScrollPane(latexPane);
        tabbedPane.addTab("Latex", scrollPane);
        tabbedPane.setMnemonicAt(1, KeyEvent.VK_2);

        tabbedPane.addChangeListener(e -> {
            if (tabbedPane.getSelectedIndex() == 1)
                SwingUtilities.invokeLater(latexPane::setLatexTable);
        });

        getContentPane().add(tabbedPane, BorderLayout.CENTER);

        setVisible(true);
    }

    private String getHTMLCredits() {
        return "<html><body width='%1s'><h3>Created by Walter Xie and Alexei Drummond</h3>"+
                "<p>The Centre for Computational Evolution<br>"+
                "University of Auckland<br></p>"+
                "<p>Homepage :<br>"+
                "<a href=\""+LPhyAppConfig.LPHY_WEB+"\">"+LPhyAppConfig.LPHY_WEB+"</a></p>"+
                "<p>Source code distributed under the GNU Lesser General Public License Version 3</p>"+
                "<p>Require Java 17, current Java version " + System.getProperty("java.version") + "</p></html>";
    }

    public static void main(String[] args) {
        ModelGuideApp frame = new ModelGuideApp();
    }

}
