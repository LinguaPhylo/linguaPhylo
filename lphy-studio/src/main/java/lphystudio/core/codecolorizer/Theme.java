package lphystudio.core.codecolorizer;

import java.awt.*;

/**
 * Colours for the code
 */
public class Theme {
    // black        orange      skyblue      bluishgreen   yellow
    // "#000000"    "#E69F00"   "#56B4E9"    "#009E73"     "#F0E442"
    // blue       vermillion    reddishpurple  gray
    // "#0072B2"  "#D55E00"     "#CC79A7"      "#999999"
    static final Color[] theme1 = new Color[]{Color.decode("#D55E00"), Color.decode("#009E73"),
            Color.decode("#0072B2"), Color.decode("#CC79A7"), Color.gray, Color.black};
    static final Color[] theme2 = new Color[]{Color.magenta,
            new Color(0, 196, 0), Color.blue,
            new Color(196, 0, 196), Color.gray, Color.black};

    // the order is constantColor, randomVarColor, genDistColor,
    // argumentNameColor, functionColor, mainColor
    public static Color[] getTheme() {
       return theme1;
    }
}
