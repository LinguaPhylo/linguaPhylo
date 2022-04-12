package lphystudio.core.layeredgraph;

import lphy.core.LPhyParser;
import lphy.graphicalModel.Generator;
import lphy.graphicalModel.RandomVariable;
import lphy.graphicalModel.Value;
import lphy.layeredgraph.LayeredNode;
import lphy.util.LoggerUtils;
import lphystudio.core.swing.*;

import javax.swing.*;
import java.awt.*;
import java.util.prefs.Preferences;

public class LayeredGNode extends LayeredNode.Default {

    static Preferences preferences = Preferences.userNodeForPackage(LayeredGNode.class);

    private JButton button;
    private Object value;
    String name = null;

    public static final double VAR_WIDTH = 90;
    public static final double VAR_HEIGHT = 50;

    public static final double FACTOR_SIZE = 7;
    public static final double FACTOR_LABEL_GAP = 5;

    LPhyParser parser;

    private static boolean showValue = getShowValueInNode();

    public LayeredGNode(Object value, LPhyParser parser) {
        super(0, 0);

        this.value = value;
        this.parser = parser;

        if (value instanceof Value) {
            name = ((Value) value).getId();
            createValueButton((Value) value);

        } else if (value instanceof Generator) {
            createParameterizedButton();
        }
    }

    public String getName() {
        return name;
    }

    public void addOutput(LayeredGNode output) {
        getSuccessors().add(output);

        if (getSuccessors().size() == 1 && (value instanceof Value) && ((Value) value).isAnonymous()) {
            name = "[" + ((Generator) output.value).getParamName(((Value) value)) + "]";
            button.setText(NodePaintUtils.getNodeString(this, (Value) value, showValue));
        }
    }

    public static boolean getShowValueInNode() {
        return preferences.getBoolean("showValueInNode", true);
    }

    public static void setShowValueInNode(boolean showValueInNode) {
        preferences.putBoolean("showValueInNode", showValueInNode);
        showValue = showValueInNode;
    }

    public boolean hasButton() {
        return button != null;
    }


    private void createValueButton(Value value) {
        Color backgroundColor = NodePaintUtils.getFillColor(value, parser);
        Color borderColor = NodePaintUtils.getDrawColor(value, parser);

        if (!value.isAnonymous() && parser.getValue(value.getId(), LPhyParser.Context.model) != value) {
            backgroundColor = backgroundColor.darker();
            borderColor = Color.red;
        }

        String str = NodePaintUtils.getNodeString(this, value, showValue);

        boolean inData = inData(value);

        if (button == null) {
            if (value instanceof RandomVariable) {
                button = new CircleButton(str, backgroundColor, borderColor);
            } else if (value.getGenerator() != null) {
                if (inData) {
                    button = new DataDiamondButton(str);
                } else {
                    button = new DiamondButton(str, backgroundColor, borderColor);
                }
            } else {
                if (inData) {
                    button = new DataButton(str);
                } else {
                    button = new SquareButton(str, backgroundColor, borderColor);
                }
            }
        }
        button.setSize((int) VAR_WIDTH, (int) VAR_HEIGHT);

        Class type = value.getType();
        // keep button string up to date.
        value.addValueListener((oldValue, newValue) -> {
            Value<?> v;
            if (newValue instanceof Value<?>)
                v = (Value<?>) newValue;
            else if ((newValue instanceof Number || newValue instanceof String)
                    // this condition will keep warnings to input string in numbers
                    && type.isAssignableFrom(newValue.getClass()))
                v = new Value<>(name, newValue);
            else {
                String err = "Illegal new value is given ! " + name + " = " + newValue;
                LoggerUtils.log.severe(err);
                throw new IllegalArgumentException(err);
            }
            button.setText(NodePaintUtils.getNodeString(LayeredGNode.this, v, showValue));
        });
    }

    private boolean inData(Value value) {
        if (!value.isAnonymous()) {
            return parser.hasValue(value.getId(), LPhyParser.Context.data);
        }
        return false;
    }

    private void createParameterizedButton() {
        button = new JButton("");
        button.setSize((int) FACTOR_SIZE * 2, (int) FACTOR_SIZE * 2);
    }

    public Object value() {
        return value;
    }

    void setLayer() {
        int maxLayer = -1;
        for (LayeredNode node : getSuccessors()) {
            if (node.getLayer() >= maxLayer) maxLayer = node.getLayer();
        }
        layer = maxLayer + 1;
    }

    public JButton getButton() {
        return button;
    }

    public void setX(double x) {
        if (!Double.isNaN(x)) {
            setLocation(x, getY());
        } else {
            LoggerUtils.log.warning("Tried to set x coordinate of node " + this + " to NaN!");
        }
    }

    public void setY(double y) {
        setLocation(getX(), y);
    }

    public void setLocation(double x, double y) {
        this.x = x;
        this.y = y;
        if (hasButton()) button.setLocation((int) (x - button.getWidth() / 2), (int) (y - button.getHeight() / 2));
    }

    public String toString() {

        return "v(" + getLayer() + ", " + getIndex() + ") = " + value.toString();
    }
}