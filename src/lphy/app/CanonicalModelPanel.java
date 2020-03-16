package lphy.app;

import lphy.core.LPhyParser;
import lphy.parser.CodeColorizer;

import javax.print.attribute.AttributeSetUtilities;
import javax.swing.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import java.awt.*;

public class CanonicalModelPanel extends JComponent {
    GraphicalLPhyParser parser;
    JTextPane pane = new JTextPane();
    JScrollPane scrollPane;

    public CanonicalModelPanel(GraphicalLPhyParser parser) {
        this.parser = parser;

        pane.setFont(new Font("monospaced", Font.PLAIN, 12));
        pane.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        pane.setEditable(false);

        JPanel noWrapPanel = new JPanel(new BorderLayout());
        noWrapPanel.add(pane);
        scrollPane = new JScrollPane(noWrapPanel);

        setText();

        BoxLayout boxLayout = new BoxLayout(this, BoxLayout.PAGE_AXIS);
        setLayout(boxLayout);
        add(new JLabel("Canonical model description."));
        add(scrollPane);

        parser.addGraphicalModelChangeListener(this::setText);
    }

    private void setText() {

        try {
            pane.getDocument().remove(0, pane.getDocument().getLength());
        } catch (BadLocationException e) {
            e.printStackTrace();
        }

        String text = LPhyParser.Utils.getCanonicalScript(parser);

        CodeColorizer codeColorizer = new CodeColorizer(parser.getDictionary(), pane);
        codeColorizer.parse(text);
    }
}
