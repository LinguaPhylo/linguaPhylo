package james.swing;

import james.graphicalModel.*;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class GraphicalModelTextPane extends JTextPane {

    List<Value> values = new ArrayList<>();
    GraphicalModelPanel panel;
    boolean updatingModelText = false;
    List<GraphicalModelListener> listeners = new ArrayList<>();

    public GraphicalModelTextPane(GraphicalModelPanel panel) {
        this.panel = panel;
        updateModelText();
        setFont(new Font("monospaced", Font.PLAIN, 16));
        setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        setEditable(false);

        addCaretListener(e -> {
            if (!updatingModelText) {
                int dot = e.getDot();
                int line = 0;
                try {
                    line = getLineOfOffset(GraphicalModelTextPane.this, dot);
                    Value value = values.get(line);

                    if (value instanceof  RandomVariable) {
                        int positionInLine = dot - getLineStartOffset(GraphicalModelTextPane.this, line);
                        if (positionInLine < value.codeString().indexOf('~')) {
                            for (GraphicalModelListener listener : listeners) {
                                listener.valueSelected(value);
                            }
                        } else {
                            for (GraphicalModelListener listener : listeners) {
                                listener.generativeDistributionSelected(((RandomVariable)value).getGenerativeDistribution());
                            }
                        }
                    } else {
                        for (GraphicalModelListener listener : listeners) {
                            listener.valueSelected(value);
                        }
                    }
                } catch (BadLocationException e1) {
                    e1.printStackTrace();
                }
            }
        });
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

    public void updateModelText() {

        updatingModelText = true;

        StyledDocument document = getStyledDocument();

        int caretPosition = getCaretPosition();

        try {
            document.remove(0, document.getLength());
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
        values.clear();

        Value.traverseGraphicalModel(panel.variable, new GraphicalModelNodeVisitor() {
            @Override
            public void visitValue(Value value) {
                values.add(value);
                Color color = Color.black;
                if (value instanceof RandomVariable) {
                    String[] parts = value.codeString().split("~");
                    String[] funcParts = parts[1].split("\\(");
                    addColoredTextLine(GraphicalModelTextPane.this,
                            new String[] {parts[0], "~", funcParts[0], "(", funcParts[1]},
                            new Color[] {Color.green, Color.black, Color.blue, Color.black, Color.black});
                } else {
                    addColoredTextLine(GraphicalModelTextPane.this,
                            new String[] {value.codeString()},
                            new Color[] {Color.black});
                }
            }

            @Override
            public void visitGenDist(GenerativeDistribution genDist) {

            }

            @Override
            public void visitFunction(Function f) {

            }
        }, true);

        if (caretPosition < document.getLength()) {
            setCaretPosition(caretPosition);
        } else {
            setCaretPosition(document.getLength());
        }
        updatingModelText = false;
    }

    public void addGraphicalModelListener(GraphicalModelListener graphicalModelListener) {
        listeners.add(graphicalModelListener);
    }
}
