package james.app;

import james.core.LPhyParser;
import james.graphicalModel.*;

import javax.swing.*;
import java.awt.*;
import java.util.HashSet;
import java.util.Set;

public class CanonicalModelPanel extends JComponent {
    LPhyParser parser;
    JTextArea area = new JTextArea();
    JScrollPane scrollPane;

    public CanonicalModelPanel(LPhyParser parser) {
        this.parser = parser;

        area.setFont(new Font("monospaced", Font.PLAIN, 12));
        area.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        area.setEditable(false);

        scrollPane = new JScrollPane(area);

        setText();

        BoxLayout boxLayout = new BoxLayout(this, BoxLayout.PAGE_AXIS);
        setLayout(boxLayout);
        add(new JLabel("Canonical model description."));
        add(scrollPane);

        // TODO find another way to communicate changes to model
        //parser.addGraphicalModelChangeListener(this::setText);
    }

    private void setText() {
        area.setText(LPhyParser.Utils.getCanonicalScript(parser));
    }
}
