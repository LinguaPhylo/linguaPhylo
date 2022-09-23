package lphystudio.app;

import java.awt.*;

/**
 * @author Alexei Drummond
 * @author Walter Xie
 */
public class FontUtils {

    public static int MIN_FONT_SIZE = 8;
    public static int MAX_FONT_SIZE = 16;

    public static Font MIN_FONT = new Font(Font.MONOSPACED, Font.PLAIN, FontUtils.MIN_FONT_SIZE);
    public static Font MID_FONT = new Font(Font.MONOSPACED, Font.PLAIN, (MIN_FONT_SIZE+MAX_FONT_SIZE)/2);
    public static Font MAX_FONT = new Font(Font.MONOSPACED, Font.PLAIN, FontUtils.MAX_FONT_SIZE);

    static int screenWidth = Toolkit.getDefaultToolkit().getScreenSize().width;
    static int screenHeight = Toolkit.getDefaultToolkit().getScreenSize().height;

    public static int getMaxWidthWithinScreen(int ncol) {
        if (ncol < 0) return screenWidth;
        return Math.min(screenWidth, FontUtils.MAX_FONT_SIZE * ncol);
    }

    public static int getMaxHeightWithinScreen(int nrow) {
        if (nrow < 0) return screenHeight;
        return Math.min(screenHeight, FontUtils.MAX_FONT_SIZE * nrow);
    }

    public static int getMinWidth(int ncol) {
        return FontUtils.MIN_FONT_SIZE * ncol;
    }

    public static int getMinHeight(int nrow) {
        return FontUtils.MIN_FONT_SIZE * nrow;
    }

    /**
     * @param dim  height or width
     * @return  {@link Font#deriveFont(float)} according to given dim
     */
    public static Font deriveFont(double dim) {
        if (dim < 9)
            return MIN_FONT.deriveFont(8.0f);
        else if (dim < 10)
            return MIN_FONT.deriveFont(9.0f);
        else if (dim < 11)
            return MIN_FONT.deriveFont(10.0f);
        else if (dim < 12)
            return MIN_FONT.deriveFont(11.0f);
        else
            return MIN_FONT.deriveFont(12.0f);
    }

}
