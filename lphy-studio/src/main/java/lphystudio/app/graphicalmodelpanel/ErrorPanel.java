package lphystudio.app.graphicalmodelpanel;

import lphy.core.logger.LoggerUtils;

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

/**
 * Print message logged by {@link LoggerUtils} for various levels.
 */
public class ErrorPanel extends JComponent {
    JTextPane pane = new JTextPane();
    JScrollPane scrollPane;

    Style severeStyle;
    Style warningStyle;
    Style infoStyle;
    Style defaultStyle;

    private boolean noLvlName = false;

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

    public void clear() {
        pane.setText("");
    }

    /**
     * @param noLvlName  set to true, then not print INFO before messages,
     *                   and also easily fix the new line in the beginning of the string.
     *                   Otherwise, it will be added between INFO and messages.
     */
    public void setNoLvlName(boolean noLvlName) {
        this.noLvlName = noLvlName;
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
        String lvlNm = level.getName();
        StyledDocument document = pane.getStyledDocument();
        String message = "";
        // only apply to INFO
        if ( !( noLvlName && "info".equalsIgnoreCase(lvlNm) ) )
            message += lvlNm + ": ";
        message += logRecord.getMessage() + "\n";

        try {
            switch (lvlNm.toLowerCase()) {
                case "severe" -> document.insertString(document.getLength(), message, severeStyle);
                case "warning" -> document.insertString(document.getLength(), message, warningStyle);
                case "info" -> document.insertString(document.getLength(), message, infoStyle);
                default -> document.insertString(document.getLength(), message, defaultStyle);
            }
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }
}
