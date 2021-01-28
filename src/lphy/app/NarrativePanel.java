package lphy.app;

import lphy.core.LPhyParser;
import lphy.parser.codecolorizer.DataModelCodeColorizer;
import lphy.utils.LoggerUtils;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.rtf.RTFEditorKit;
import java.awt.*;

public class NarrativePanel extends JComponent {
    GraphicalLPhyParser parser;
    JTextPane pane = new JTextPane();
    JScrollPane scrollPane;

    public NarrativePanel(GraphicalLPhyParser parser) {
        this.parser = parser;

        pane.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        pane.setEditable(false);

        scrollPane = new JScrollPane(pane);


        setText();

        BoxLayout boxLayout = new BoxLayout(this, BoxLayout.PAGE_AXIS);
        setLayout(boxLayout);
        add(new JLabel("Narrative."));
        add(scrollPane);

        parser.addGraphicalModelChangeListener(this::setText);
    }

    private void setText() {

        try {
            pane.getDocument().remove(0, pane.getDocument().getLength());
        } catch (BadLocationException e) {
            e.printStackTrace();
        }

        String text = LPhyParser.Utils.getNarrative(parser);

        text = text + "\n" + LPhyParser.Utils.getInferenceStatement(parser);

        pane.setText(text);
    }
}
