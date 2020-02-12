package james.app;

import james.graphicalModel.GraphicalModelParser;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class GraphicalModelInterpreter extends JPanel {

    GraphicalModelParser parser;
    GraphicalModelTextPane textPane;
    JPanel activeLine = new JPanel();
    JTextField interpreterField;

    static String[] greekLetterCodes = {
            "\\alpha", "\\beta", "\\gamma", "\\delta", "\\epsilon", "\\zeta", "\\eta", "\\theta", "\\iota", "\\kappa",
            "\\lambda", "\\mu", "\\nu", "\\xi", "\\omicron", "\\pi", "\\rho", "\\sigma", "\\tau", "\\upsilon",
            "\\phi", "\\chi", "\\psi", "\\omega", "\\Gamma", "\\Delta", "\\Theta", "\\Lambda", "\\Xi", "\\Pi",
            "\\Sigma", "\\Omega"};

    String[] greekLetters = {
            "α", "β", "γ", "δ", "ε", "ζ", "η", "θ", "ι", "κ", "λ", "μ", "ν", "ξ", "ο", "π", "ρ", "σ", "τ", "υ", "φ",
            "χ", "ψ", "ω", "Γ", "Δ", "Θ", "Λ", "Ξ", "Π", "Σ", "Ω"};

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

        if (greekLetterCodes.length != greekLetters.length) {
            throw new RuntimeException("Error! Mismatched array lengths for greek letter codes and symbols");
        }

        for (int i = 0; i < greekLetterCodes.length; i++) {
            canonicalWords.put(greekLetterCodes[i], greekLetters[i]);
        }

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

    public void interpretInput(String input) {

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

    public void clear() {
        textPane.setText("");
    }
}
