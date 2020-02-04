package james.swing;

import james.graphicalModel.GraphicalModelParser;
import james.graphicalModel.RandomVariable;
import james.graphicalModel.Value;

import javax.swing.*;
import java.awt.*;

public class StatePanel extends JPanel {
    GraphicalModelParser parser;
    JTextArea area = new JTextArea();
    JScrollPane scrollPane;

    public StatePanel(GraphicalModelParser parser) {
        this.parser = parser;

        area.setFont(new Font("monospaced", Font.PLAIN, 12));
        area.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        area.setEditable(false);

        scrollPane = new JScrollPane(area);

        setText();

        BoxLayout boxLayout = new BoxLayout(this, BoxLayout.PAGE_AXIS);
        setLayout(boxLayout);
        add(scrollPane);

        parser.addGraphicalModelChangeListener(() -> setText());
    }

    void setText() {
        area.setText("");
        for (Value value : parser.getDictionary().values()) {

            if ((value instanceof RandomVariable)) area.append(value.toString()+"\n");
        }
    }
}
