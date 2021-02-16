package lphy.app;

import lphy.core.LPhyParser;
import lphy.core.narrative.LaTeXNarrative;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.text.BadLocationException;
import javax.swing.text.html.HTMLEditorKit;
import java.awt.*;
import java.io.IOException;
import java.net.URISyntaxException;

public class LaTexPanel extends JComponent {
    GraphicalLPhyParser parser;
    JTextPane pane = new JTextPane();
    JScrollPane scrollPane;

    public LaTexPanel(GraphicalLPhyParser parser) {
        this.parser = parser;

        pane.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        pane.setEditable(false);

        scrollPane = new JScrollPane(pane);

        setText();

        BoxLayout boxLayout = new BoxLayout(this, BoxLayout.PAGE_AXIS);
        setLayout(boxLayout);
        add(scrollPane);

        parser.addGraphicalModelChangeListener(this::setText);
    }

    private void setText() {

        try {
            pane.getDocument().remove(0, pane.getDocument().getLength());
        } catch (BadLocationException e) {
            e.printStackTrace();
        }

        LaTeXNarrative narrative = new LaTeXNarrative();

        String text = "\\begin{document}\n\n";

        text += LPhyParser.Utils.getNarrative(parser, narrative);

        text += "\n" + LPhyParser.Utils.getInferenceStatement(parser, narrative);

        text += "\n" + narrative.referenceSection();

        text += "\\end{document}";

        pane.setText(text);
    }
}
