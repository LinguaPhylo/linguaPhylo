package lphystudio.app.graphicalmodelpanel;

import lphy.core.exception.SimulatorParsingException;
import lphy.core.parser.LPhyMetaData;
import lphystudio.core.codecolorizer.LineCodeColorizer;

import javax.swing.*;
import java.awt.*;

public class LPhyCodeLabel extends JTextPane {

    LineCodeColorizer codeColorizer;

    int preferredHeight = 20;
    static int preferredWidth = 300;

    public LPhyCodeLabel(LPhyMetaData parser, String code) {

        codeColorizer = new LineCodeColorizer(parser, LPhyMetaData.Context.model,this);

        setEditable(false);
        setOpaque(false);

        codeColorizer.parse(code);
    }

    public void setCodeColorizedText(String text) {
        super.setText("");
        try {
            codeColorizer.parse(text);
        } catch (org.antlr.v4.runtime.NoViableAltException e) {
            super.setText(text);
        } catch (SimulatorParsingException spe) {
            super.setText(text);
        }
        preferredHeight = getContentHeight(text);
        revalidate();
    }

    @Override
    public Dimension getMaximumSize() {
        return new Dimension(preferredWidth, preferredHeight);
    }

    public static int getContentHeight(String content) {
        JEditorPane dummyEditorPane=new JEditorPane();
        dummyEditorPane.setSize(preferredWidth,Short.MAX_VALUE);
        dummyEditorPane.setText(content);

        return dummyEditorPane.getPreferredSize().height;
    }
}
