package lphystudio.app.alignmentcomponent;

import jebl.evolution.sequences.SequenceType;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Walter Xie
 */
public class ColourPalette {

    public final static Color UNKNOWN = Color.gray;
    public final static List<Color> Four = List.of(Color.red, Color.blue, Color.yellow, Color.green);

    public static Color[] getTwoPlusOne(){
        return new Color[]{Four.get(0), Four.get(1), UNKNOWN};
    }

    public static Color[] getFourPlusOne(){
        List<Color> colorList = new ArrayList<>(Four);
        colorList.add(UNKNOWN);
        return colorList.toArray(Color[]::new);
    }

    // https://stackoverflow.com/questions/470690/how-to-automatically-generate-n-distinct-colors
    public static String[] kelly_colors_hex = {
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

    // 20 colours + 1 UNKNOWN
    public static Color[] getTwentyPlusOne() {
        Color[] kelly_colors = new Color[kelly_colors_hex.length + 1];
        for (int i = 0; i < kelly_colors_hex.length; i++)
            kelly_colors[i] = HexToColor(kelly_colors_hex[i]);

        kelly_colors[kelly_colors.length-1] = UNKNOWN;
        return kelly_colors;
    }

    /**
     * @return 22 + 1 colours, because jebl.evolution.sequences.AminoAcids creates 22 amino acids.
     *         But we only use 20 for genetic coding.
     */
    public static Color[] getTwentyTwoPlusOne() {
        Color[] more_colors = new Color[kelly_colors_hex.length + 1 + Four.size()];
        for (int i = 0; i < kelly_colors_hex.length; i++)
            more_colors[i] = HexToColor(kelly_colors_hex[i]);
        int i = kelly_colors_hex.length;
        more_colors[i++] = UNKNOWN; // for 20 colours + 1 UNKNOWN
        more_colors[i++] = Four.get(1);
        more_colors[i] = Four.get(2);
        return more_colors;
    }


    // "FFB300"
    protected static Color HexToColor(String hex) {
        if (hex.length() != 6) return null;
        return new Color(
                Integer.valueOf(hex.substring(0, 2), 16),
                Integer.valueOf(hex.substring(2, 4), 16),
                Integer.valueOf(hex.substring(4, 6), 16));
    }

    /**
     * @param col
     * @param palette
     * @return   state >= palette.length, then return Color.gray
     */
    public static Color getColour(int col, Color[] palette) {
        if (col < palette.length)
            return palette[col];
        //TODO need a smart method to colour uncertain states
        return UNKNOWN;
    }


    //*** Colours ***//

    /**
     * @param sequenceType
     * @return  a {@link Color} array to visualise sequences, including uncertain states
     */
    public static Color[] getCanonicalStateColours(SequenceType sequenceType) {
        // extra 2 colours for UNKNOWN_STATE, GAP_STATE
        if ( sequenceType.getCanonicalStateCount() <=  2 && sequenceType.getStateCount() <= 4 )
            return ColourPalette.getTwoPlusOne();
        else if ( sequenceType.getCanonicalStateCount() <=  4 ) // DNA or traits
            return ColourPalette.getFourPlusOne();
        else if ( sequenceType.getCanonicalStateCount() <=  22 ) // AMINO_ACID
            return ColourPalette.getTwentyTwoPlusOne();
        else throw new IllegalArgumentException("Cannot choose colours given data type " +
                    sequenceType + " and numStates " + sequenceType.getCanonicalStateCount() + " !");
    }
    // TODO how to colour uncertain states ?
    /**
     * @return  state, if 0 <= state < numStates (no ambiguous),
     *          otherwise return numStates which is the last index
     *          in colours always for ambiguous state.
     */
//    public static int getColourIndex(int state, SequenceType sequenceType) {
//        if (sequenceType == null)
//            throw new IllegalArgumentException("SequenceType is required !");
//
//        if (sequenceType.getName().equals(Binary.NAME) && state > 1 )
//            return 2;
//        if (sequenceType.getName().equals(SequenceType.NUCLEOTIDE.getName()) && state > 3)
//            return 4;
//        else if (sequenceType.getName().equals(SequenceType.AMINO_ACID.getName()) && state > 19) // no ambiguous
//            return 20; // the last extra is always for ambiguous
//        return state;
//    }

//    public static Color getColourByDataType(int state, SequenceType sequenceType) {
//    }

}
