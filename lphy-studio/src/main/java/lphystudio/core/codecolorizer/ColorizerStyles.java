package lphystudio.core.codecolorizer;

import lphystudio.core.theme.ThemeColours;

import javax.swing.*;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import java.awt.*;

public class ColorizerStyles {

    static Color constantColor = ThemeColours.getConstantColor();
    static Color randomVarColor = ThemeColours.getRandomVarColor();
    static Color genDistColor = ThemeColours.getGenDistColor();
    static Color functionColor = ThemeColours.getFunctionColor();
    static Color argumentNameColor = ThemeColours.getArgumentNameColor();
    static Color mainColor = ThemeColours.getMainColor();
    static int argumentNameSize = 10;

    public static final String function = "functionStyle";
    public static final String distribution = "distributionStyle";
    public static final String randomVariable = "randomVarStyle";
    public static final String clampedVariable = "clampedVarStyle";
    public static final String value = "valueStyle";
    public static final String argumentName = "argumentNameStyle";
    public static final String constant = "constantStyle";
    public static final String punctuation = "punctuationStyle";
    public static final String keyword = "keywordStyle";

    static void addStyles(JTextPane textPane) {
        Style punctuationStyle = textPane.addStyle(punctuation, null);
        StyleConstants.setForeground(punctuationStyle, mainColor);

        Style functionStyle = textPane.addStyle(function, null);
        StyleConstants.setForeground(functionStyle, functionColor);

        Style genDistStyle = textPane.addStyle(distribution, null);
        StyleConstants.setForeground(genDistStyle, genDistColor);
        StyleConstants.setBold(genDistStyle, true);

        Style randomVarStyle = textPane.addStyle(randomVariable, null);
        StyleConstants.setForeground(randomVarStyle, randomVarColor);

        Style clampedVarStyle = textPane.addStyle(clampedVariable, null);
        StyleConstants.setForeground(clampedVarStyle, ThemeColours.getClampedVarColor());

        Style valueStyle = textPane.addStyle(value, null);
        StyleConstants.setForeground(valueStyle, mainColor);

        Style argumentNameStyle = textPane.addStyle(argumentName, null);
        StyleConstants.setForeground(argumentNameStyle, argumentNameColor);
        //StyleConstants.setFontSize(argumentNameStyle, argumentNameSize);

        Style constantStyle = textPane.addStyle(constant, null);
        StyleConstants.setForeground(constantStyle, constantColor);

        Style keywordStyle = textPane.addStyle(keyword, null);
        StyleConstants.setForeground(keywordStyle, mainColor);
    }
}
