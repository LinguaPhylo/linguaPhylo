package lphystudio.core.theme;

import lphy.core.model.components.Value;
import lphy.core.model.components.ValueUtils;
import lphy.core.parser.LPhyMetaParser;

import java.awt.*;

/**
 * Colours for the code
 */
public class ThemeColours {
    // black        orange      skyblue      bluishgreen   yellow
    // "#000000"    "#E69F00"   "#56B4E9"    "#009E73"     "#F0E442"
    // blue       vermillion    reddishpurple  gray
    // "#0072B2"  "#D55E00"     "#CC79A7"      "#999999"
    public enum THEME1 {
        Constant     ("Constant", Color.decode("#D55E00")), // vermillion
        RandomVar    ("RandomVar", Color.decode("#009E73")), // bluishgreen
        GenDist      ("GenDist", Color.decode("#0072B2")), // blue
        Function     ("Function", Color.decode("#CC79A7")), // reddishpurple
        ArgumentName ("ArgumentName", Color.gray),
        DataButton   ("DataButton", getTransparentColor(Color.decode("#E69F00"))), // orange
        DataButtonBorder ("DataButtonBorder", Color.decode("#E69F00")),
        ClampedVar   ("ClampedVar", Color.decode("#56B4E9")), // skyblue
        MousePress   ("MousePress", Color.lightGray),
        Default      ("Default", Color.black),
        Background   ("Background", Color.white);

        private final String id;
        private final Color color;
        THEME1(String id, Color color) {
            this.id = id;
            this.color = color;
        }

        public String getIdLowerCase() {
            return id.toLowerCase(); // for latex
        }

        public Color getColor() {
            return color;
        }
    }

    static final Color[] theme2 = new Color[]{Color.magenta,
            new Color(0, 196, 0), Color.blue,
            new Color(196, 0, 196), Color.gray, Color.black, Color.orange,
            new Color(0.2f, 0.2f, 1.0f, 0.5f), // semi-transparent blue
            Color.lightGray, // mouse press
            Color.darkGray // border
    };

    public static Color getConstantColor() {
        return THEME1.Constant.getColor();
    }
    public static Color getRandomVarColor() {
        return THEME1.RandomVar.getColor();
    }
    public static Color getGenDistColor() {
        return THEME1.GenDist.getColor();
    }
    public static Color getFunctionColor() {
        return THEME1.Function.getColor();
    }
    public static Color getArgumentNameColor() {
        return THEME1.ArgumentName.getColor();
    }
    public static Color getDefaultColor() {
        return THEME1.Default.getColor();
    }
    public static Color getBackgroundColor() {
        return THEME1.Background.getColor();
    }
    // +++ graphical nodes +++ //
    public static Color getDataButtonColor() {
        return THEME1.DataButton.getColor();
    }
    public static Color getClampedVarColor() {
        return THEME1.ClampedVar.getColor();
    }
    public static Color getMousePressColor() {
        return THEME1.MousePress.getColor();
    }
    public static Color getDataButtonBorderColor() {
        return THEME1.DataButtonBorder.getColor();
    }
    public static String getConstantIdLowerCase() {
        return THEME1.Constant.getIdLowerCase();
    }
    public static String getRandomVarIdLowerCase() {
        return THEME1.RandomVar.getIdLowerCase();
    }
    public static String getGenDistIdLowerCase() {
        return THEME1.GenDist.getIdLowerCase();
    }
    public static String getFunctionIdLowerCase() {
        return THEME1.Function.getIdLowerCase();
    }
    public static String getArgumentNameIdLowerCase() {
        return THEME1.ArgumentName.getIdLowerCase();
    }
    public static String getDefaultIdLowerCase() {
        return THEME1.Default.getIdLowerCase();
    }
    public static String getBackgroundIdLowerCase() {
        return THEME1.Background.getIdLowerCase();
    }
    // +++ graphical nodes +++ //
    public static String getDataButtonIdLowerCase() {
        return THEME1.DataButton.getIdLowerCase();
    }
    public static String getClampedVarIdLowerCase() {
        return THEME1.ClampedVar.getIdLowerCase();
    }
    public static String getMousePressIdLowerCase() {
        return THEME1.MousePress.getIdLowerCase();
    }
    public static String getDataButtonBorderIdLowerCase() {
        return THEME1.DataButtonBorder.getIdLowerCase();
    }

    // create a transparent version with alpha = 128 (50% transparency)
    public static Color getTransparentColor(Color origColor) {
        return new Color(origColor.getRed(), origColor.getGreen(), origColor.getBlue(), 128);
    }

    /**
     * @param color Color
     * @return the hex string without #.
     */
    public static String getHexString(Color color) {
        return "#" + Integer.toHexString(color.getRGB()).substring(2);
    }

    public static Color getFillColor(Value value, LPhyMetaParser parser) {
//        Color fillColor = new Color(0.0f, 1.0f, 0.0f, 0.5f);
        Color fillColor = getTransparentColor(getRandomVarColor());

        if (ValueUtils.isFixedValue(value)) {
            fillColor = ThemeColours.getBackgroundColor();
        } else if (ValueUtils.isValueOfDeterministicFunction(value)) {
//            fillColor = new Color(1.0f, 0.0f, 0.0f, 0.5f);
            fillColor = getTransparentColor(getFunctionColor());
        } else if (parser.isClampedVariable(value)) {
            // fillColor = new Color(0.2f, 0.2f, 1.0f, 0.5f);
            fillColor = getTransparentColor(getClampedVarColor());
        }

        return fillColor;
    }

    public static Color getBorderColor(Value value, LPhyMetaParser parser) {
//        Color drawColor = new Color(0.0f, 0.75f, 0.0f, 1.0f);
        Color drawColor = getRandomVarColor();
        if (ValueUtils.isFixedValue(value)) {
            drawColor = ThemeColours.getDefaultColor();
        } else if (ValueUtils.isValueOfDeterministicFunction(value)) {
//            drawColor = new Color(0.75f, 0.0f, 0.0f, 1.0f);
            drawColor = getFunctionColor();
        } else if (parser.isClampedVariable(value)) {
//            drawColor = new Color(0.15f, 0.15f, 0.75f, 1.0f);
            drawColor = getClampedVarColor();
        }

        return drawColor;
    }

    public static String defineLatexColours() {
        StringBuilder builder = new StringBuilder();
        for (THEME1 theme : THEME1.values()) {
            builder.append("\\definecolor{").append(theme.getIdLowerCase()).
                    append("}{RGB}{").append(theme.getColor().getRed()).append(", ").
                    append(theme.getColor().getGreen()).append(", ").
                    append(theme.getColor().getBlue()).append("}\n");
        }
        builder.append("\n");
        return builder.toString();
    }

}
