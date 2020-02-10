package james.swing;

import james.graphicalModel.GraphicalModelParser;
import org.antlr.v4.runtime.BufferedTokenStream;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

public class GraphicalModelInterpreter extends JPanel {

    GraphicalModelParser parser;
    GraphicalModelTextPane textPane;
    JPanel activeLine = new JPanel();
    JTextField interpreterField;

    Font interpreterFont =  new Font("monospaced", Font.PLAIN, 12);

    int BORDER_SIZE = 10;

    Border textBorder = BorderFactory.createEmptyBorder(BORDER_SIZE,BORDER_SIZE,BORDER_SIZE,BORDER_SIZE);

    Map<String, String> canonicalWords = new TreeMap<>();

    public GraphicalModelInterpreter(GraphicalModelParser parser) {
        this.parser = parser;

        textPane = new GraphicalModelTextPane(parser);
        textPane.setBorder(textBorder);
        textPane.setFont(interpreterFont);
        JScrollPane scrollPane = new JScrollPane(textPane);
        TextLineNumber tln = new TextLineNumber(textPane);
        scrollPane.setRowHeaderView( tln );

        interpreterField = new JTextField(80);
        interpreterField.setFont(interpreterFont);
        interpreterField.setBorder(textBorder);

        interpreterField.addActionListener(e -> {
            interpretInput(interpreterField.getText());
            interpreterField.setText("");
        });

        canonicalWords.put("\\alpha", "α");
        canonicalWords.put("\\beta", "β");
        canonicalWords.put("\\gamma", "γ");
        canonicalWords.put("\\kappa", "κ");
        canonicalWords.put("\\mu", "μ");
        canonicalWords.put("\\theta", "θ");
        canonicalWords.put("\\Gamma", "Γ");
        canonicalWords.put("\\Theta", "Θ");

        interpreterField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (e.getKeyChar() == ' ' || e.getKeyChar() == '=' || e.getKeyChar() == ',' || e.getKeyChar() == '~') {
                    String lastWord = lastWord(" |\\=|\\,|~");
                    String canonicalWord = getCanonicalWord(lastWord);
                    if (!lastWord.equals(canonicalWord)) {
                        String newText = interpreterField.getText().replace(lastWord, canonicalWord);
                        interpreterField.setText(newText);
                        interpreterField.setCaretPosition(newText.length());
                    }
                }
            }
        });

        BoxLayout boxLayout = new BoxLayout(this, BoxLayout.PAGE_AXIS);
        setLayout(boxLayout);

        BoxLayout boxLayout2 = new BoxLayout(activeLine, BoxLayout.LINE_AXIS);
        activeLine.setLayout(boxLayout2);


        JLabel label = new JLabel("  >");
        label.setFont(interpreterFont);
        label.setBorder(new CompoundBorder(new MatteBorder(0,0,0,2,Color.gray), new EmptyBorder(BORDER_SIZE,tln.getBorderGap(),BORDER_SIZE,tln.getBorderGap()+2)));

        activeLine.add(label);
        activeLine.add(interpreterField);

        add(scrollPane, BorderLayout.CENTER);
        add(activeLine, BorderLayout.SOUTH);
    }

    private String getCanonicalWord(String word) {
        String canonicalWord = canonicalWords.get(word);
        if (canonicalWord != null) return canonicalWord;
        return word;
    }

    private String lastWord(String delimiters) {
        String[] words = interpreterField.getText().split(delimiters);
        return words[words.length-1];
    }

    private void interpretInput(String input) {

        String[] lines = input.split(";");

        for (String line : lines) {
            line = line.trim();
            if (line.charAt(line.length() - 1) != ';') {
                line = line + ";";
            }

            parser.parseLine(line);
            textPane.addLine(line);
        }
    }
}
