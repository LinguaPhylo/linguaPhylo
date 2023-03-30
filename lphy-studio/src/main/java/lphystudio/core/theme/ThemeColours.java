package lphystudio.core.theme;

import lphy.core.LPhyParser;
import lphy.graphicalModel.Value;
import lphy.graphicalModel.ValueUtils;

import java.awt.*;

/**
 * Colours for the code
 */
public class ThemeColours {

    public enum THEME1 {
        Constant     ("Constant", Color.decode("#D55E00")),
        RandomVar    ("RandomVar", Color.decode("#009E73")), // bluishgreen
        GenDist      ("GenDist", Color.decode("#0072B2")), // blue
        Function     ("Function", Color.decode("#CC79A7")), // reddishpurple
        ArgumentName ("ArgumentName",   Color.gray),
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

    // black        orange      skyblue      bluishgreen   yellow
    // "#000000"    "#E69F00"   "#56B4E9"    "#009E73"     "#F0E442"
    // blue       vermillion    reddishpurple  gray
    // "#0072B2"  "#D55E00"     "#CC79A7"      "#999999"
    static final Color[] theme1 = new Color[]{
            Color.decode("#D55E00"), // vermillion
            Color.decode("#009E73"), // bluishgreen
            Color.decode("#0072B2"), // blue
            Color.decode("#CC79A7"), // reddishpurple
            Color.gray,
            Color.black,
            Color.decode("#E69F00"), // orange
            Color.decode("#56B4E9"), // skyblue
            Color.lightGray, // mouse press
            Color.darkGray // DataButton border
    };
    static final Color[] theme2 = new Color[]{Color.magenta,
            new Color(0, 196, 0), Color.blue,
            new Color(196, 0, 196), Color.gray, Color.black, Color.orange,
            new Color(0.2f, 0.2f, 1.0f, 0.5f), // semi-transparent blue
            Color.lightGray, // mouse press
            Color.darkGray // border
    };

    // the order is constantColor, randomVarColor, genDistColor,
    // argumentNameColor, functionColor, mainColor
    public static Color[] getTheme() {
       return theme1;
    }

    public static Color getConstantColor() {
        return getTheme()[0];
    }
    public static Color getRandomVarColor() {
        return getTheme()[1];
    }
    public static Color getGenDistColor() {
        return getTheme()[2];
    }
    public static Color getFunctionColor() {
        return getTheme()[3];
    }
    public static Color getArgumentNameColor() {
        return getTheme()[4];
    }
    public static Color getMainColor() {
        return getTheme()[5];
    }
    public static Color getBackgroundColor() {
        return Color.white;
    }
    // +++ graphical nodes +++ //
    public static Color getDataColor() {
        return getTheme()[6];
    }
    public static Color getClampedVarColor() {
        return getTheme()[7];
    }
    public static Color getMousePressColor() {
        return getTheme()[8];
    }
    public static Color getDataButtonColor() {
        return getTheme()[9];
    }

    // create a transparent version with alpha = 128 (50% transparency)
    public static Color getTransparentColor(Color origColor) {
        return new Color(origColor.getRed(), origColor.getGreen(), origColor.getBlue(), 128);
    }

    public static Color getFillColor(Value value, LPhyParser parser) {
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

    public static Color getBorderColor(Value value, LPhyParser parser) {
//        Color drawColor = new Color(0.0f, 0.75f, 0.0f, 1.0f);
        Color drawColor = getRandomVarColor();
        if (ValueUtils.isFixedValue(value)) {
            drawColor = ThemeColours.getMainColor();
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
