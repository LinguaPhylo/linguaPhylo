package lphystudio.app.graphicalmodelpanel;

import javax.swing.*;
import java.awt.*;
import java.util.prefs.Preferences;

/**
 * @author Walter Xie
 */
public class AlignmentLogPanel extends JPanel {

    static Preferences preferences = Preferences.userNodeForPackage(AlignmentLogPanel.class);

    final JScrollPane jScrollPane;

    public AlignmentLogPanel(Component view) {
        this.jScrollPane = new JScrollPane(view);

        setLayout(new BorderLayout());
        add(jScrollPane, BorderLayout.CENTER);

    }
}
