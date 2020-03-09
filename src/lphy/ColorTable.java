package lphy;

import java.awt.*;
import java.util.List;

/**
 * @author Alexei Drummond
 */
public class ColorTable  {

    List<Color> colors;

    public ColorTable(List<Color> colors)  {

        this.colors = colors;
    }

    public Color getColor(int index) {
        return colors.get(index);
    }
}
