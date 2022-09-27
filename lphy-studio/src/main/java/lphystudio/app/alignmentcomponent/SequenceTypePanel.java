package lphystudio.app.alignmentcomponent;

import jebl.evolution.sequences.SequenceType;
import jebl.evolution.sequences.State;
import lphystudio.app.FontUtils;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.prefs.Preferences;

/**
 * Easy to draw buttons.
 * @author Walter Xie
 */
public class SequenceTypePanel extends JPanel {

    static Preferences preferences = Preferences.userNodeForPackage(SequenceTypePanel.class);

    public SequenceTypePanel(SequenceType sequenceType) {
        createLegends(sequenceType);

        setOpaque(false);
        setLayout(new FlowLayout());
    }

    private void createLegends(final SequenceType sequenceType) {
        List<? extends State> states = sequenceType.getCanonicalStates();
        final Color[] colors = ColourPalette.getCanonicalStateColours(sequenceType);

        int ncol = getStatesTotalChars(states);
        int nrow = 1; //TODO more rows?

        int maximumWidth = FontUtils.getMaxWidthWithinScreen(ncol);
        int maximumHeight = FontUtils.getMaxHeightWithinScreen(nrow);

        int minimumWidth = FontUtils.MIN_FONT_SIZE * ncol;
        int minimumHeight = FontUtils.MIN_FONT_SIZE * nrow;
        // size 12
        setFont(FontUtils.MID_FONT);

        JButton button;
        Color color;
        String label;
        for (int i = 0; i < states.size(); i++) {
            label = states.get(i).getCode();
            color = colors[i];

            // adjust text colour by background
            button = new LegendButton(label, color);
            add(button);
        }

        setMaximumSize(new Dimension(maximumWidth, maximumHeight));
        setMinimumSize(new Dimension(minimumWidth, minimumHeight));
    }

    private int getStatesTotalChars(List<? extends State> states) {
        int tot = 0;
        for (State state : states)
            tot += state.getCode().length();
        return tot;
    }

//    private int getCol() {
//
//    }
//
//    private int getRow() {
//
//    }

    public static boolean isShowLegends() {
        return preferences.getBoolean("showLegends", false);
    }
    public static void setShowLegends(boolean show) {
        preferences.putBoolean("showLegends", show);
    }
}
