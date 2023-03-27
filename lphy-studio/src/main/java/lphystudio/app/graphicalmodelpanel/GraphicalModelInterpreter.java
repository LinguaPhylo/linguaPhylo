package lphystudio.app.graphicalmodelpanel;

import lphy.core.LPhyParser;
import lphy.graphicalModel.Generator;
import lphy.parser.ParserUtils;
import lphy.parser.SimulatorParsingException;
import lphy.util.LoggerUtils;
import lphy.util.Symbols;
import lphystudio.core.codecolorizer.LineCodeColorizer;
import lphystudio.core.swing.TextLineNumber;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.*;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

public class GraphicalModelInterpreter extends JPanel {

    boolean includeNewRandomVariablePanel = false;

    LPhyParser parser;
    GraphicalModelTextPane textPane;
    JPanel activeLine = new JPanel();
    JTextField interpreterField;
    NewRandomVariablePanel newRandomVariablePanel;
    JLabel infoLine = new JLabel("  ", SwingConstants.LEFT);
    LPhyParser.Context context;

    private static final String COMMIT_ACTION = "commit";

    static Font interpreterFont = new Font(Font.MONOSPACED, Font.PLAIN, 12);
    static Font smallInterpreterFont = new Font(Font.MONOSPACED, Font.PLAIN, 10);

    int BORDER_SIZE = 10;

    Border textBorder = BorderFactory.createEmptyBorder(BORDER_SIZE, BORDER_SIZE, BORDER_SIZE, BORDER_SIZE);

    Map<String, String> canonicalWords = new TreeMap<>();

    public GraphicalModelInterpreter(LPhyParser parser, LPhyParser.Context context) {
        this.parser = parser;
        this.context = context;

        includeNewRandomVariablePanel = (context != LPhyParser.Context.data);

        textPane = new GraphicalModelTextPane(parser);
        textPane.setBorder(textBorder);
        textPane.setFont(interpreterFont);
        JScrollPane scrollPane = new JScrollPane(textPane);
        TextLineNumber tln = new TextLineNumber(textPane);
        scrollPane.setRowHeaderView(tln);

        interpreterField = new JTextField(80);
        interpreterField.setFont(interpreterFont);
        interpreterField.setBorder(textBorder);
        interpreterField.setFocusTraversalKeysEnabled(false);

        if (includeNewRandomVariablePanel) newRandomVariablePanel = new NewRandomVariablePanel(this, ParserUtils.getGenerativeDistributions());

        List<String> keywords = parser.getKeywords();
        keywords.addAll(Arrays.asList(Symbols.symbolCodes));

        Autocomplete autoComplete = new Autocomplete(interpreterField, keywords);

        for (Map.Entry<String, Set<Class<?>>> entry : parser.getGeneratorClasses().entrySet()) {

            Set<Class<?>> classes = entry.getValue();
            Iterator iterator = classes.iterator();

            StringBuilder builder = new StringBuilder();
            for (Class c : classes) {
                builder.append(Generator.getSignature((Class) iterator.next()));
                builder.append("; ");
            }
            final String message = builder.toString();

            autoComplete.getActionMap().put(entry.getKey(), new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    setMessage(message);
                }
            });
        }

        interpreterField.getDocument().addDocumentListener(autoComplete);
        interpreterField.getInputMap().put(KeyStroke.getKeyStroke('\t'), COMMIT_ACTION);
        interpreterField.getActionMap().put(COMMIT_ACTION, autoComplete.new CommitAction());

        interpreterField.addActionListener(e -> {
            interpretInput(interpreterField.getText(), context);
            interpreterField.setText("");
        });


        for (int i = 0; i < Symbols.symbolCodes.length; i++) {
            canonicalWords.put(Symbols.symbolCodes[i], Symbols.unicodeSymbols[i]);
        }

        interpreterField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (e.getKeyChar() == ' ' || e.getKeyChar() == '=' || e.getKeyChar() == ',' || e.getKeyChar() == '~') {
                    String lastWord = lastWord(" |\\=|\\,|~|\\(");
                    String canonicalWord = getCanonicalWord(lastWord);
                    if (!lastWord.equals(canonicalWord)) {
                        String newText = interpreterField.getText().replace(lastWord, canonicalWord);
                        interpreterField.setText(newText);
                        interpreterField.setCaretPosition(newText.length());
                    }
                }
            }
        });

        //BoxLayout boxLayout = new BoxLayout(this, BoxLayout.PAGE_AXIS);
        //setLayout(boxLayout);

        BoxLayout boxLayout2 = new BoxLayout(activeLine, BoxLayout.LINE_AXIS);
        activeLine.setLayout(boxLayout2);

        JToggleButton button = new JToggleButton("  >");
        button.setFont(interpreterFont);
        button.setBorder(new CompoundBorder(new MatteBorder(0, 0, 0, 2, Color.gray), new EmptyBorder(BORDER_SIZE, tln.getBorderGap(), BORDER_SIZE, tln.getBorderGap() + 2)));

        activeLine.add(button);

        if (includeNewRandomVariablePanel) {
            button.addActionListener(e -> {
                if (button.isSelected()) {
                    activeLine.remove(interpreterField);
                    activeLine.add(newRandomVariablePanel);
                } else {
                    activeLine.remove(newRandomVariablePanel);
                    activeLine.add(interpreterField);
                }
                revalidate();
            });
        }

        activeLine.add(interpreterField);
        //activeLine.add(newRandomVariablePanel);
        
        activeLine.setPreferredSize(new Dimension(2000,interpreterField.getPreferredSize().height));
        activeLine.setMaximumSize(new Dimension(2000,interpreterField.getPreferredSize().height));

        //add(scrollPane);
        //add(activeLine);

        infoLine.setBorder(new EmptyBorder(2,43,2,2));
        infoLine.setHorizontalTextPosition(SwingConstants.LEFT);
        infoLine.setFont(infoLine.getFont().deriveFont(10.0f));
        infoLine.setForeground(Color.gray);
        //add(infoLine);

        GroupLayout layout = new GroupLayout(this);
        setLayout(layout);

        GroupLayout.ParallelGroup horizParallelGroup = layout.createParallelGroup(GroupLayout.Alignment.LEADING);
        horizParallelGroup.addComponent(scrollPane);
        horizParallelGroup.addComponent(activeLine);
        horizParallelGroup.addComponent(infoLine);

        GroupLayout.SequentialGroup vertSequentialGroup = layout.createSequentialGroup();
        vertSequentialGroup.addComponent(scrollPane);
        vertSequentialGroup.addComponent(activeLine);
        vertSequentialGroup.addComponent(infoLine);

        layout.setHorizontalGroup(horizParallelGroup);

        layout.setVerticalGroup(vertSequentialGroup);

        LoggerUtils.log.addHandler(new Handler() {
            @Override
            public void publish(LogRecord record) {

                String message = record.getMessage();

                if (record.getLevel() == Level.SEVERE) {
                    message = "<html><font color=\"red\">SEVERE: " + message + "</font></html>";
                } else if (record.getLevel() == Level.WARNING) {
                    message = "<html><font color=\"#FFA500\">WARNING: " + message + "</font></html>";
                } else if (record.getLevel() != Level.INFO) {
                    return;
                }
                infoLine.setText(message);
            }

            @Override
            public void flush() {
                infoLine.setText("");
            }

            @Override
            public void close() throws SecurityException {
                infoLine.setText("");

            }
        });
    }

    private void setMessage(String message) {
        infoLine.setText(message);
        repaint();
    }

    private String getCanonicalWord(String word) {
        String canonicalWord = canonicalWords.get(word);
        if (canonicalWord != null) return canonicalWord;
        return word;
    }

    private String lastWord(String delimiters) {
        String[] words = interpreterField.getText().split(delimiters);
        return words[words.length - 1];
    }

    public void interpretInput(String input, LPhyParser.Context context) {

        try {
            parser.parse(input, context);

            try {
                LineCodeColorizer codeColorizer = new LineCodeColorizer(parser, context, textPane);
                // if no data{}, input is empty
                codeColorizer.parse(input);
            } catch (Exception e) {
                LoggerUtils.log.severe("CodeColorizer failed with exception: " + e.getMessage());
                e.printStackTrace(System.err);
            }
        } catch (SimulatorParsingException spe) {
            LoggerUtils.log.severe("Parsing of " + context + " block failed: " + spe.getMessage());
        }
    }

    public void clear() {
        textPane.setText("");
    }
}
