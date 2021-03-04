package lphy.app;

import lphy.utils.LoggerUtils;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.text.html.HTMLEditorKit;
import java.awt.*;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

public class ErrorPanel extends JComponent {
    JTextPane pane = new JTextPane();
    JScrollPane scrollPane;

    Style severeStyle;
    Style warningStyle;
    Style infoStyle;
    Style defaultStyle;

    public ErrorPanel() {

        pane.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 10));
        pane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        pane.setEditable(false);
        pane.setEditorKit(new HTMLEditorKit());

        severeStyle = pane.addStyle("severe", null);
        StyleConstants.setForeground(severeStyle, Color.red);

        warningStyle = pane.addStyle("warning", null);
        StyleConstants.setForeground(warningStyle, Color.orange);

        infoStyle = pane.addStyle("info", null);
        StyleConstants.setForeground(infoStyle, Color.green.darker());

        defaultStyle = pane.addStyle("default", null);

        //JPanel noWrapPanel = new JPanel(new BorderLayout());
        //noWrapPanel.add(pane);
        scrollPane = new JScrollPane(pane);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        BoxLayout boxLayout = new BoxLayout(this, BoxLayout.PAGE_AXIS);
        setLayout(boxLayout);
        add(scrollPane);

        LoggerUtils.log.addHandler(new Handler() {
            @Override
            public void publish(LogRecord record) {
                addText(record);
            }

            @Override
            public void flush() {
                ErrorPanel.this.flush();
            }

            @Override
            public void close() throws SecurityException {

            }
        });
    }

    private void flush() {
        try {
            pane.getDocument().remove(0, pane.getDocument().getLength());
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    private void addText(LogRecord logRecord) {

        Level level = logRecord.getLevel();
        StyledDocument document = pane.getStyledDocument();
        String message = logRecord.getLevel().getName() + ": " + logRecord.getMessage() + "\n";

        try {
            switch (level.getName().toLowerCase()) {
                case "severe":
                    document.insertString(document.getLength(), message, severeStyle);
                    break;
                case "warning":
                    document.insertString(document.getLength(), message, warningStyle);
                    break;
                case "info":
                    document.insertString(document.getLength(), message, infoStyle);
                    break;
                default:
                    document.insertString(document.getLength(), message, defaultStyle);
            }
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }
}
