package lphystudio.core.codecolorizer;

import javax.swing.*;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import java.awt.*;

public class ColorizerStyles {

    static Color randomVarColor = new Color(0, 196, 0);
    static Color constantColor = Color.magenta;
    static Color argumentNameColor = Color.gray;
    static Color functionColor = new Color(196, 0, 196);

    static int argumentNameSize = 10;

    public static final String function = "functionStyle";
    public static final String distribution = "distributionStyle";
    public static final String randomVariable = "randomVarStyle";
    public static final String value = "valueStyle";
    public static final String argumentName = "argumentNameStyle";
    public static final String constant = "constantStyle";
    public static final String punctuation = "punctuationStyle";
    public static final String keyword = "keywordStyle";

    static void addStyles(JTextPane textPane) {
        Style punctuationStyle = textPane.addStyle(punctuation, null);
        StyleConstants.setForeground(punctuationStyle, Color.black);

        Style functionStyle = textPane.addStyle(function, null);
        StyleConstants.setForeground(functionStyle, functionColor);

        Style genDistStyle = textPane.addStyle(distribution, null);
        StyleConstants.setForeground(genDistStyle, Color.blue);
        StyleConstants.setBold(genDistStyle, true);

        Style randomVarStyle = textPane.addStyle(randomVariable, null);
        StyleConstants.setForeground(randomVarStyle, randomVarColor);

        Style valueStyle = textPane.addStyle(value, null);
        StyleConstants.setForeground(valueStyle, Color.black);

        Style argumentNameStyle = textPane.addStyle(argumentName, null);
        StyleConstants.setForeground(argumentNameStyle, argumentNameColor);
        //StyleConstants.setFontSize(argumentNameStyle, argumentNameSize);

        Style constantStyle = textPane.addStyle(constant, null);
        StyleConstants.setForeground(constantStyle, constantColor);

        Style keywordStyle = textPane.addStyle(keyword, null);
        StyleConstants.setForeground(keywordStyle, Color.black);
    }
}
