package james.app;

import james.graphicalModel.*;
import james.graphicalModel.types.DoubleListValue;
import james.graphicalModel.types.DoubleValue;
import james.graphicalModel.types.IntegerListValue;
import james.graphicalModel.types.MatrixValue;

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

    static double VAR_WIDTH = 100;
    static double VAR_HEIGHT = 60;

    static double FACTOR_SIZE = 7;
    static double FACTOR_LABEL_GAP = 10;


    static DecimalFormat format = new DecimalFormat();

    public RenderNode(T value) {
        this.value = value;

        if (value instanceof Value) {
            createValueButton();
        } else if (value instanceof Parameterized) {
            createParameterizedButton();
        }
    }

    private String getButtonString(Value value) {
        String str = value.getId();

        if (!(value instanceof RandomVariable)) {
            str = displayString(value);
        }
        return str;
    }

    private String displayString(Value v) {

        String valueString;
        if (v instanceof DoubleValue) {
            valueString = format.format(((DoubleValue) v).value());
        } else if (v instanceof MatrixValue || v instanceof IntegerListValue || v instanceof DoubleListValue) {
            return "<html><center><p><font color=\"#808080\" ><b>" + v.getId() + "</b></p></font></center></html>";
        } else {
            valueString = v.value().toString();
        }

        return "<html><center><p><small><font color=\"#808080\" >" + v.getId() + "</p></font></small><p>" + valueString + "</p></center></html>";
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
//        if (!parser.getDictionary().values().contains(value)) {
//            backgroundColor = backgroundColor.darker();
//            borderColor = Color.black;
//        }

        String str = getButtonString((Value) value);

        if (button == null) {
            if (((Value) value).getFunction() != null) {
                button = new DiamondButton(str, backgroundColor, borderColor);
            } else {

                button = new CircleButton(str, backgroundColor, borderColor);
            }
        }
        button.setSize((int) VAR_WIDTH, (int) VAR_HEIGHT);
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
        button.setLocation((int) (point.getX() - button.getWidth()/2), (int) (point.getY() - button.getHeight()/2));
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
