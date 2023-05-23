package lphystudio.app.graphicalmodelpanel;

import lphy.core.GraphicalModelListener;
import lphy.core.LPhyMetaParser;
import lphy.graphicalModel.GraphicalModel;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * To display the history of LPhy scripts inputted into either the data or model console.
 */
public class GraphicalModelTextPane extends JTextPane {

    LPhyMetaParser parser;
    List<GraphicalModelListener> listeners = new ArrayList<>();

    public GraphicalModelTextPane(LPhyMetaParser parser) {
        this.parser = parser;
        setEditable(false);

        for (String line : parser.getLines() ) {
            addLine(line);
        }
//        if (parser.getLines().size() == 0) {
//            setPreferredSize(new Dimension(1,200));
//        }
    }

    void addLine(String line) {

        String[] commentParts = line.split("//(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
        String comment = null;

        if (commentParts.length == 2) {
            line = commentParts[0];
            comment = commentParts[1];
        }

        if (!line.equals("")) {
            if (GraphicalModel.Utils.isRandomVariableLine(line)) {
                String[] parts = line.split("~");
                String genDist = parts[1].substring(0, parts[1].indexOf('('));
                String rest = parts[1].substring(parts[1].indexOf('('));

                addColoredTextLine(GraphicalModelTextPane.this,
                        new String[]{parts[0], "~", genDist, rest},
                        new Color[]{Color.green, Color.black, Color.blue, Color.black}, true);
            } else {
                addColoredTextLine(GraphicalModelTextPane.this,
                        new String[]{line},
                        new Color[]{Color.black}, true);
            }
        }
        setCaretPosition(getDocument().getLength());
        if (comment != null) {
            System.out.println("Found comment after " + line);
            addColoredTextLine(GraphicalModelTextPane.this,
                    new String[]{"//"+comment},
                    new Color[]{Color.gray},line.equals(""));
        }
        setCaretPosition(getDocument().getLength());
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

    private void addColoredTextLine(JTextPane pane, String[] text, Color[] color, boolean newlineFirst) {
        StyledDocument doc = pane.getStyledDocument();

        if (doc.getLength() > 0 && newlineFirst) text[0] = "\n" + text[0];
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
