package lphy.app;

import lphy.core.LPhyParser;

import javax.swing.*;
import java.awt.*;

public class CanonicalModelPanel extends JComponent {
    GraphicalLPhyParser parser;
    JTextArea area = new JTextArea();
    JScrollPane scrollPane;

    public CanonicalModelPanel(GraphicalLPhyParser parser) {
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

        parser.addGraphicalModelChangeListener(this::setText);
    }

    private void setText() {
        area.setText(LPhyParser.Utils.getCanonicalScript(parser));
    }
}
