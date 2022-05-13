package lphystudio.core.swing.io;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.io.OutputStream;

/**
 * System out or err to JTextPane.
 * <code>
 *     final JTextPane soutPane = new JTextPane();
 *     TextPaneOutputStream out = new TextPaneOutputStream(soutPane, false);
 *     PrintStream printStream = new PrintStream(out);
 *     System.setOut(printStream);
 * </code>
 * @author Walter Xie
 */
public class TextPaneOutputStream extends OutputStream {

    final JTextPane pane;
    private final Style style;

    public TextPaneOutputStream(JTextPane pane, boolean isErr) {
        this.pane = pane;

        pane.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 10));
        pane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        pane.setEditable(false);
        pane.setEditorKit(JTextPane.createEditorKitForContentType("text/html"));

        if (isErr) {
            style = pane.addStyle("err", null);
            StyleConstants.setForeground(style, Color.red);
        } else
            style = pane.addStyle("out", null);

//        textPane.setAutoscrolls(true);
//        textPane.addHyperlinkListener(e -> {
//            if(e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
//                if(Desktop.isDesktopSupported()) {
//                    try {
//                        Desktop.getDesktop().browse(e.getURL().toURI());
//                    } catch (IOException | URISyntaxException ex) {
//                        LoggerUtils.log.severe(ex.toString());
//                        ex.printStackTrace();
//                    }
//                }
//            }
//        });
    }


    @Override
    public void write(byte[] buffer, int offset, int length) {
        final String text = new String (buffer, offset, length);
        SwingUtilities.invokeLater(() -> {
            StyledDocument document = pane.getStyledDocument();
            try {
//                if (this. == System.err)
//                    document.insertString(document.getLength(), text, errStyle);
//                else
                    document.insertString(document.getLength(), text, style);
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void write(int b) {
        write (new byte [] {(byte)b}, 0, 1);
    }

    public void flush() {
        try {
            pane.getDocument().remove(0, pane.getDocument().getLength());
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

}
