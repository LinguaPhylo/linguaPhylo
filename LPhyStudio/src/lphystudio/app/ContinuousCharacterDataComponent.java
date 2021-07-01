package lphystudio.app;

import lphy.evolution.Taxon;
import lphy.evolution.alignment.ContinuousCharacterData;

import javax.swing.*;

import static lphy.graphicalModel.VectorUtils.INDEX_SEPARATOR;

public class ContinuousCharacterDataComponent extends JLabel {

    public ContinuousCharacterDataComponent(ContinuousCharacterData data) {

        StringBuilder builder = new StringBuilder();
        builder.append("<html><table border=\"0\"><tr><th>Taxa</th>");

        for (int j =0; j < data.nchar(); j++) {
            builder.append("<th>trait" + INDEX_SEPARATOR + j + "</th>");
        }

        Taxon[] taxonArray = data.getTaxa().getTaxonArray();
        for (int i = 0; i < taxonArray.length; i++) {

            builder.append("<tr><td>");
            builder.append(taxonArray[i].getName());
            builder.append("</td>");

            for (int j =0; j < data.nchar(); j++) {
                builder.append("<td>" + data.getState(i, j) + "</td>");
            }
        }

        builder.append("</table></html>");

        setText(builder.toString());
    }
}
