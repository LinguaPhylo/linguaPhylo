package james.app.graphicalmodelcomponent;

import james.TimeTree;
import james.graphicalModel.Parameterized;
import james.graphicalModel.RandomVariable;
import james.graphicalModel.Value;
import james.graphicalModel.ValueUtils;
import james.graphicalModel.types.DoubleValue;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Point2D;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class LayeredGNode implements LayeredNode {

    private JButton button;
    Point2D point = new Point2D.Double();
    private List<LayeredNode> inputs = new ArrayList<>();
    private List<LayeredNode> outputs = new ArrayList<>();
    private Object value;
    int layer;
    int index;

    String name = null;

    static double VAR_WIDTH = 90;
    static double VAR_HEIGHT = 50;

    static double FACTOR_SIZE = 7;
    static double FACTOR_LABEL_GAP = 10;

    static DecimalFormat format = new DecimalFormat();

    //GraphicalModelParser parser;

    public LayeredGNode(Object value) {//, GraphicalModelParser parser) {
        this.value = value;
        //this.parser = parser;

        if (value instanceof Value) {
            name = ((Value)value).getId();
            createValueButton();

        } else if (value instanceof Parameterized) {
            createParameterizedButton();
        }
    }

    public void addOutput(LayeredGNode output) {
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

            if (obj instanceof TimeTree || obj instanceof Double[] || obj instanceof Double[][] || obj instanceof Integer[] || obj instanceof Integer[][]) {
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
        } else if (tooLarge(value)) {
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

    private boolean tooLarge(Object v) {
        return (v instanceof TimeTree || v instanceof Integer[] || v instanceof Double[] || v instanceof Double[][] || v instanceof Integer[][] || v.toString().length()>10);
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


//        if (!((Value)value).isAnonymous() && parser.getDictionary().get(((Value)value).getId()) != value) {
//            backgroundColor = backgroundColor.darker();
//            borderColor = borderColor.darker();
//        }

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

    public Object value() {
        return value;
    }

    public boolean isSource() {
        return inputs.size() == 0;
    }

    public void setY(double y) {
        setLocation(point.getX(), y);
    }

    public void setX(double x) {
        setLocation(x, point.getY());
    }

    public double getX() {
        return point.getX();
    }

    public double getY() {
        return point.getY();
    }

    @Override
    public int getLayer() {
        return layer;
    }

    @Override
    public void setLayer(int layer) {
        this.layer = layer;
    }

    @Override
    public Point2D getPosition() {
        return point;
    }

    @Override
    public int getIndex() {
        return index;
    }

    @Override
    public void setIndex(int index) {
        this.index = index;
    }

    @Override
    public List<LayeredNode> getSuccessors() {
        return outputs;
    }

    @Override
    public List<LayeredNode> getPredecessors() {
        return inputs;
    }

    void setLayer() {
        int maxLayer = -1;
        for (LayeredNode node : outputs) {
            if (node.getLayer() >= maxLayer) maxLayer = node.getLayer();
        }
        layer = maxLayer + 1;
    }

    public int siblingCount(LayeredGNode parent) {
        if (parent.inputs.contains(this)) {
            return parent.inputs.size() - 1;
        } else {
            return -1;
        }
    }

    public double getRelativeIndex(LayeredGNode parent) {
        double index = parent.inputs.indexOf(this);

        return index - ((parent.inputs.size()-1.0) / 2.0);
    }

    public double getPreferredX(double preferredSpacing) {
        double x = 0;

        for (LayeredNode parent : outputs) {
            x += getPreferredX((LayeredGNode)parent, preferredSpacing);
        }
        return x / outputs.size();
    }

    public double getPreferredX(LayeredGNode parent, double preferredSpacing) {
        return getRelativeIndex(parent) * preferredSpacing + parent.point.getX();
    }

    public JButton getButton() {
        return button;
    }

    public void setLocation(double x, double y) {
        point = new Point2D.Double(x,y);
        if (hasButton()) button.setLocation((int) (point.getX() - button.getWidth()/2), (int) (point.getY() - button.getHeight()/2));
    }

    public String toString() {
        return value.toString();
    }
}
