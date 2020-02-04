package james.swing;

import james.graphicalModel.GraphicalModelParser;
import james.graphicalModel.RandomVariable;
import james.graphicalModel.Value;

import javax.swing.*;
import java.awt.*;

public class LiteralPanel extends JPanel {

    GraphicalModelParser parser;
    JTextArea area = new JTextArea();

    public LiteralPanel(GraphicalModelParser parser) {
        this.parser = parser;

        area.setFont(new Font("monospaced", Font.PLAIN, 12));
        area.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        area.setEditable(false);

        setText();

        BoxLayout boxLayout = new BoxLayout(this, BoxLayout.PAGE_AXIS);
        setLayout(boxLayout);
        add(area);

        parser.addGraphicalModelChangeListener(new GraphicalModelChangeListener() {
            @Override
            public void modelChanged() {
                setText();
            }
        });
    }

    void setText() {
        area.setText("");
        for (Value value : parser.getDictionary().values()) {

            if (!(value instanceof RandomVariable)) area.append(value.toString()+"\n");
        }
    }
}
