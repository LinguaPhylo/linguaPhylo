package lphy.app.graphicalmodelcomponent;

import lphy.evolution.alignment.SimpleAlignment;
import lphy.core.LPhyParser;
import lphy.graphicalModel.*;
import lphy.utils.LoggerUtils;

import javax.swing.*;
import java.awt.*;
import java.text.DecimalFormat;
import java.util.Map;
import java.util.prefs.Preferences;

public class LayeredGNode extends LayeredNode.Default {

    static Preferences preferences = Preferences.userNodeForPackage(LayeredGNode.class);

    private JButton button;
    private Object value;
    String name = null;

    static double VAR_WIDTH = 90;
    static double VAR_HEIGHT = 50;

    static double FACTOR_SIZE = 7;
    static double FACTOR_LABEL_GAP = 5;

    static DecimalFormat format = new DecimalFormat();

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
        Color backgroundColor = new Color(0.0f, 1.0f, 0.0f, 0.5f);
        Color borderColor = new Color(0.0f, 0.75f, 0.0f, 1.0f);

        if (ValueUtils.isFixedValue(value)) {
            backgroundColor = Color.white;
            borderColor = Color.black;
        } else if (ValueUtils.isValueOfDeterministicFunction(value)) {
            backgroundColor = new Color(1.0f, 0.0f, 0.0f, 0.5f);
            borderColor = new Color(0.75f, 0.0f, 0.0f, 1.0f);
        } else if (parser.isClampedVariable(value)) {
            backgroundColor = new Color(0.2f, 0.2f, 1.0f, 0.5f);
            borderColor = new Color(0.15f, 0.15f, 0.75f, 1.0f);
        }

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

        // keep button string up to date.
        value.addValueListener(() -> button.setText(NodePaintUtils.getNodeString(this,(Value)this.value, showValue)));
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