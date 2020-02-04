package james.swing;

import james.graphicalModel.GraphicalModelParser;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.List;

public class GraphicalModelInterpreter extends JPanel {

    GraphicalModelParser parser;
    GraphicalModelTextPane textPane;
    JPanel activeLine = new JPanel();
    JTextField interpreterField;

    Font interpreterFont =  new Font("monospaced", Font.PLAIN, 12);

    public GraphicalModelInterpreter(GraphicalModelParser parser) {
        this.parser = parser;

        textPane = new GraphicalModelTextPane(parser);
        textPane.setFont(interpreterFont);
        JScrollPane scrollPane = new JScrollPane(textPane);
        TextLineNumber tln = new TextLineNumber(textPane);
        scrollPane.setRowHeaderView( tln );

        interpreterField = new JTextField(80);
        interpreterField.setFont(interpreterFont);
        interpreterField.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        interpreterField.addActionListener(e -> {
            interpretInput(interpreterField.getText());
            interpreterField.setText("");
        });



        BoxLayout boxLayout = new BoxLayout(this, BoxLayout.PAGE_AXIS);
        setLayout(boxLayout);

        BoxLayout boxLayout2 = new BoxLayout(activeLine, BoxLayout.LINE_AXIS);
        activeLine.setLayout(boxLayout2);


        JLabel label = new JLabel("  >");
        label.setFont(interpreterFont);
        label.setBorder(new CompoundBorder(new MatteBorder(0,0,0,2,Color.gray), new EmptyBorder(10,5,10,7)));

        activeLine.add(label);
        activeLine.add(interpreterField);

        add(scrollPane, BorderLayout.CENTER);
        add(activeLine, BorderLayout.SOUTH);
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
