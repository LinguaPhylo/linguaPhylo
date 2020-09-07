package lphy.parser;

import javax.swing.*;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import java.awt.*;

public class ColorizerStyles {

    static Color randomVarColor = new Color(0, 196, 0);
    static Color constantColor = Color.magenta;
    static Color argumentNameColor = Color.gray;
    static Color functionColor = new Color(196,0,196);

    static int argumentNameSize = 10;

    static void addStyles(JTextPane textPane) {
        Style functionStyle = textPane.addStyle("functionStyle", null);
        StyleConstants.setForeground(functionStyle, functionColor);

        Style genDistStyle = textPane.addStyle("genDistStyle", null);
        StyleConstants.setForeground(genDistStyle, Color.blue);
        StyleConstants.setBold(genDistStyle, true);

        Style randomVarStyle = textPane.addStyle("randomVarStyle", null);
        StyleConstants.setForeground(randomVarStyle, randomVarColor);

        Style valueStyle = textPane.addStyle("valueStyle", null);
        StyleConstants.setForeground(valueStyle, Color.black);

        Style argumentNameStyle = textPane.addStyle("argumentNameStyle", null);
        StyleConstants.setForeground(argumentNameStyle, argumentNameColor);
        //StyleConstants.setFontSize(argumentNameStyle, argumentNameSize);

        Style constantStyle = textPane.addStyle("constantStyle", null);
        StyleConstants.setForeground(constantStyle, constantColor);

        Style punctuationStyle = textPane.addStyle("punctuationStyle", null);
        StyleConstants.setForeground(punctuationStyle, Color.black);
    }
}
