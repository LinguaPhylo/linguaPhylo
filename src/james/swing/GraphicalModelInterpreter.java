package james.swing;

import james.graphicalModel.GraphicalModelParser;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

public class GraphicalModelInterpreter extends JPanel {

    GraphicalModelParser parser;
    GraphicalModelTextPane textPane;
    JTextField interpreterField;

    public GraphicalModelInterpreter(GraphicalModelParser parser) {
        this.parser = parser;

        textPane = new GraphicalModelTextPane(parser);
        JScrollPane scrollPane = new JScrollPane(textPane);
        TextLineNumber tln = new TextLineNumber(textPane);
        scrollPane.setRowHeaderView( tln );

        interpreterField = new JTextField(80);
        interpreterField.setFont(new Font("monospaced", Font.PLAIN, 12));
        interpreterField.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        interpreterField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                interpretLine(interpreterField.getText());
                interpreterField.setText("");
            }
        });

        BoxLayout boxLayout = new BoxLayout(this, BoxLayout.PAGE_AXIS);
        setLayout(boxLayout);

        add(scrollPane, BorderLayout.CENTER);
        add(interpreterField, BorderLayout.SOUTH);
    }

    private void interpretLine(String line) {

        line = line.trim();
        if (line.charAt(line.length()-1) != ';') {
            line = line + ";";
        }

        parser.parseLine(line);
        textPane.addLine(line);

    }
}
