package james.app;

import james.graphicalModel.*;

import javax.swing.*;
import java.awt.*;

public class CanonicalModelPanel extends JComponent {
    GraphicalModelParser parser;
    JTextArea area = new JTextArea();
    JScrollPane scrollPane;

    public CanonicalModelPanel(GraphicalModelParser parser) {
        this.parser = parser;

        area.setFont(new Font("monospaced", Font.PLAIN, 12));
        area.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        area.setEditable(false);

        scrollPane = new JScrollPane(area);

        setText();

        BoxLayout boxLayout = new BoxLayout(this, BoxLayout.PAGE_AXIS);
        setLayout(boxLayout);
        add(new JLabel("Canonical model description."));
        add(scrollPane);

        parser.addGraphicalModelChangeListener(() -> setText());
    }

    void setText() {
        area.setText("");
        for (Value value : parser.getRoots()) {

            Value.traverseGraphicalModel(value, new GraphicalModelNodeVisitor() {
                @Override
                public void visitValue(Value value) {
                    area.append(value.codeString()+"\n");
                }

                @Override
                public void visitGenDist(GenerativeDistribution genDist) {

                }

                public void visitFunction(DeterministicFunction f) {

                }
            }, true);
        }
    }
}
