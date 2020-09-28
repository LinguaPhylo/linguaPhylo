package lphy.app;

import java.awt.*;

/**
 * @author Walter Xie
 */
public class AlignmentColour {

    public static Color UNKNOWN = Color.gray;

    public static Color[] DNA_COLORS = {Color.red, Color.blue, Color.yellow, Color.green, UNKNOWN};

    public static Color[] BINARY_COLORS = {Color.red, Color.blue, UNKNOWN};

    public static Color[] PROTEIN_COLORS = getProteinColors();

    // 20 colours
    protected static Color[] getProteinColors() {
        // https://stackoverflow.com/questions/470690/how-to-automatically-generate-n-distinct-colors
        String[] kelly_colors_hex = {
                "FFB300", // Vivid Yellow
                "803E75", // Strong Purple
                "FF6800", // Vivid Orange
                "A6BDD7", // Very Light Blue
                "C10020", // Vivid Red
                "CEA262", // Grayish Yellow
                "817066", // Medium Gray
                // The following don't work well for people with defective color vision
                "007D34", // Vivid Green
                "F6768E", // Strong Purplish Pink
                "00538A", // Strong Blue
                "FF7A5C", // Strong Yellowish Pink
                "53377A", // Strong Violet
                "FF8E00", // Vivid Orange Yellow
                "B32851", // Strong Purplish Red
                "F4C800", // Vivid Greenish Yellow
                "7F180D", // Strong Reddish Brown
                "93AA00", // Vivid Yellowish Green
                "593315", // Deep Yellowish Brown
                "F13A13", // Vivid Reddish Orange
                "232C16" // Dark Olive Green
        };

        Color[] kelly_colors = new Color[kelly_colors_hex.length + 1];
        for (int i = 0; i < kelly_colors_hex.length; i++) {
            kelly_colors[i] = HexToColor(kelly_colors_hex[i]);
        }
        kelly_colors[kelly_colors_hex.length] = UNKNOWN;
        return kelly_colors;
    }

    // "FFB300"
    protected static Color HexToColor(String hex) {
        if (hex.length() != 6) return null;
        return new Color(
                Integer.valueOf(hex.substring(0, 2), 16),
                Integer.valueOf(hex.substring(2, 4), 16),
                Integer.valueOf(hex.substring(4, 6), 16));
    }

}
