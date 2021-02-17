package lphy.app;

import lphy.core.LPhyParser;
import lphy.core.narrative.HTMLNarrative;
import lphy.core.narrative.Narrative;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.EditorKit;
import javax.swing.text.html.HTMLEditorKit;
import java.awt.*;
import java.io.IOException;
import java.net.URISyntaxException;

public class NarrativePanel extends JComponent {
    GraphicalLPhyParser parser;
    JTextPane pane = new JTextPane();
    JScrollPane scrollPane;
    Narrative narrative;

    public NarrativePanel(GraphicalLPhyParser parser, Narrative narrative) {
     this(parser, narrative, null);
    }


    public NarrativePanel(GraphicalLPhyParser parser, Narrative narrative, EditorKit editorKit) {
        this.parser = parser;
        this.narrative = narrative;

        pane.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        pane.setEditable(false);

        if (editorKit != null) pane.setEditorKit(editorKit);

        pane.addHyperlinkListener(e -> {
            if(e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                if(Desktop.isDesktopSupported()) {
                    try {
                        Desktop.getDesktop().browse(e.getURL().toURI());
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    } catch (URISyntaxException uriSyntaxException) {
                        uriSyntaxException.printStackTrace();
                    }
                }
            }
        });


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

        String text = narrative.beginDocument();

        text += LPhyParser.Utils.getNarrative(parser, narrative);

        text += LPhyParser.Utils.getInferenceStatement(parser, narrative);

        text += narrative.referenceSection();

        text += narrative.endDocument();

        pane.setText(text);
    }
}
