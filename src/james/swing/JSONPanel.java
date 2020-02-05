package james.swing;

import james.graphicalModel.GraphicalModelParser;
import james.graphicalModel.RandomVariable;
import james.graphicalModel.Value;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class JSONPanel extends JPanel {
    GraphicalModelParser parser;
    JTextArea area = new JTextArea();
    JScrollPane scrollPane;

    public JSONPanel(GraphicalModelParser parser) {
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
        area.setText("{\n");
        List<RandomVariable> variables = new ArrayList<>();
        for (Value value : parser.getDictionary().values()) {
            if ((value instanceof RandomVariable)) {
                variables.add((RandomVariable)value);
            }
        }
        area.append(indent(variables.get(0).toString()));
        for (int i = 1; i < variables.size(); i++) {
            area.append(",\n");
            area.append(indent(variables.get(i).toString()));

        }
        area.append("\n}");
    }

    private String indent(String string) {
        String s = string.replaceAll("\n", "\n  ");
        return "  " + s;
    }
}
