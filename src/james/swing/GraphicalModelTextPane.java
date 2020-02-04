package james.swing;

import james.core.GraphicalModelParser;
import james.graphicalModel.*;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class GraphicalModelTextPane extends JTextPane {

    GraphicalModelParser parser;
    GraphicalModelPanel panel;
    boolean updatingModelText = false;
    List<GraphicalModelListener> listeners = new ArrayList<>();

    public GraphicalModelTextPane(GraphicalModelPanel panel) {
        this.panel = panel;
        this.parser = panel.parser;
        setFont(new Font("monospaced", Font.PLAIN, 16));
        setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        setEditable(false);

        //for (String line : );
    }

    static int getLineOfOffset(JTextComponent comp, int offset) throws BadLocationException {
        Document doc = comp.getDocument();
        if (offset < 0) {
            throw new BadLocationException("Can't translate offset to line", -1);
        } else if (offset > doc.getLength()) {
            throw new BadLocationException("Can't translate offset to line", doc.getLength() + 1);
        } else {
            Element map = doc.getDefaultRootElement();
            return map.getElementIndex(offset);
        }
    }

    static int getLineStartOffset(JTextComponent comp, int line) throws BadLocationException {
        Element map = comp.getDocument().getDefaultRootElement();
        if (line < 0) {
            throw new BadLocationException("Negative line", -1);
        } else if (line >= map.getElementCount()) {
            throw new BadLocationException("No such line", comp.getDocument().getLength() + 1);
        } else {
            Element lineElem = map.getElement(line);
            return lineElem.getStartOffset();
        }
    }

    private void addColoredTextLine(JTextPane pane, String[] text, Color[] color) {
        StyledDocument doc = pane.getStyledDocument();

        if (doc.getLength() > 0) text[0] = "\n" + text[0];
        for (int i = 0; i < text.length; i++) {
            Style style = pane.addStyle("Color Style", null);
            StyleConstants.setForeground(style, color[i]);
            try {
                doc.insertString(doc.getLength(), text[i], style);
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
        }
    }

    public void addGraphicalModelListener(GraphicalModelListener graphicalModelListener) {
        listeners.add(graphicalModelListener);
    }
}
