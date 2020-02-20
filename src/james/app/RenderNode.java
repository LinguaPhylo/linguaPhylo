package james.app;

import james.graphicalModel.*;
import james.graphicalModel.types.*;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Point2D;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class RenderNode<T> {

    private JButton button;
    Point2D point;
    List<RenderNode> inputs = new ArrayList<>();
    List<RenderNode> outputs = new ArrayList<>();
    private T value;
    int level;

    String name = null;

    static double VAR_WIDTH = 90;
    static double VAR_HEIGHT = 50;

    static double FACTOR_SIZE = 7;
    static double FACTOR_LABEL_GAP = 10;

    static DecimalFormat format = new DecimalFormat();

    GraphicalModelParser parser;

    public RenderNode(T value, GraphicalModelParser parser) {
        this.value = value;
        this.parser = parser;

        if (value instanceof Value) {
            name = ((Value)value).getId();
            createValueButton();

        } else if (value instanceof Parameterized) {
            createParameterizedButton();
        }
    }

    public void addOutput(RenderNode output) {
        outputs.add(output);

        if (outputs.size() == 1 && (value instanceof Value) && ((Value) value).isAnonymous()) {
            name = "[" + ((Parameterized)output.value).getParamName(((Value) value)) + "]";
            button.setText(getButtonString((Value)value));
        }
    }
    
    public boolean hasButton() {
        return button != null;
    }

    private String getButtonString(Value value) {
        String str = value.getId();

        if (!(value instanceof RandomVariable)) {
            str = displayString(name, value);
        } else {
            if (str.length() > 5) {
                str = "<small>" + str + "</small>";
            }

            Object obj = value.value();

            if (obj instanceof Double[] || obj instanceof Double[][] || obj instanceof Integer[] || obj instanceof Integer[][]) {
                str = "<b>" + str + "</b>";
            }
            return "<html><center><p>" + str + "</p></center></html>";

        }
        return str;
    }

    private String displayString(String name, Value v) {

        Object value = v.value();

        if (name == null) name = "null";

        String valueString;
        if (v instanceof DoubleValue) {
            valueString = format.format(((DoubleValue) v).value());
        } else if (value instanceof Integer[] || value instanceof Double[] || value instanceof Double[][] || value instanceof Integer[][]) {
            if (name.length() < 5) {
                return "<html><center><p><font color=\"#808080\" ><b>" + name + "</b></p></font></center></html>";
            } else {
                return "<html><center><p><font color=\"#808080\" ><b><small>" + name + "</small></b></p></font></center></html>";
            }
        } else {
            valueString = v.value().toString();
        }
        
        return "<html><center><p><small><font color=\"#808080\" >" + name + "</p></font></small><p>" + valueString + "</p></center></html>";
    }


    private void createValueButton() {
        Color backgroundColor = new Color(0.0f, 1.0f, 0.0f, 0.5f);
        Color borderColor = new Color(0.0f, 0.75f, 0.0f, 1.0f);

        if (ValueUtils.isFixedValue((Value) value)) {
            backgroundColor = Color.white;
            borderColor = Color.black;
        } else if (ValueUtils.isValueOfFunction((Value) value)) {
            backgroundColor = new Color(1.0f, 0.0f, 0.0f, 0.5f);
            borderColor = new Color(0.75f, 0.0f, 0.0f, 1.0f);
        }


        if (!((Value)value).isAnonymous() && parser.getDictionary().get(((Value)value).getId()) != value) {
            backgroundColor = backgroundColor.darker();
            borderColor = borderColor.darker();
        }

        String str = getButtonString((Value)value);

        if (button == null) {
            if (((Value) value).getFunction() != null) {
                button = new DiamondButton(str, backgroundColor, borderColor);
            } else if (!(value instanceof RandomVariable)) {
                button = new SquareButton(str, backgroundColor, borderColor);
            } else {
                button = new CircleButton(str, backgroundColor, borderColor);
            }
        }
        button.setSize((int) VAR_WIDTH, (int) VAR_HEIGHT);

        // keep button string up to date.
        ((Value)value).addValueListener(() -> button.setText(getButtonString((Value)value)));
    }

    private void createParameterizedButton() {
        String dtr = ((Parameterized) value).getName();

        button = new JButton("");
        button.setSize((int) FACTOR_SIZE * 2, (int) FACTOR_SIZE * 2);
    }

    public T value() {
        return value;
    }

    public boolean isLeaf() {
        return inputs.size() == 0;
    }

    public void locate(Point2D point) {
        this.point = point;
        if (hasButton()) button.setLocation((int) (point.getX() - button.getWidth()/2), (int) (point.getY() - button.getHeight()/2));
    }

    void setLevel() {
        int maxLevel = -1;
        for (RenderNode node : outputs) {
            if (node.level >= maxLevel) maxLevel = node.level;
        }
        level = maxLevel + 1;
    }

    public int siblingCount(RenderNode parent) {
        if (parent.inputs.contains(this)) {
            return parent.inputs.size() - 1;
        } else {
            return -1;
        }
    }

    public double getRelativeIndex(RenderNode parent) {
        double index = parent.inputs.indexOf(this);

        return index - ((parent.inputs.size()-1.0) / 2.0);
    }

    public double getPreferredX(double preferredSpacing) {
        double x = 0;

        for (RenderNode parent : outputs) {
            x += getPreferredX(parent, preferredSpacing);
        }
        return x / outputs.size();
    }

    public double getPreferredX(RenderNode parent, double preferredSpacing) {
        return getRelativeIndex(parent) * preferredSpacing + parent.point.getX();
    }

    public JButton getButton() {
        return button;
    }
}
