package lphystudio.app;

import lphy.evolution.Taxa;
import lphy.evolution.Taxon;

import javax.swing.*;

public class TaxaComponent extends JLabel {

    public TaxaComponent(Taxa taxa) {
        Taxon[] taxonArray = taxa.getTaxonArray();

        boolean hasSpecies = taxonArray[0].getSpecies() != null && taxonArray[0].getSpecies() != taxonArray[0].getName();

        StringBuilder builder = new StringBuilder();
        builder.append("<html><table border=\"0\"><tr><th>Taxa</th>");

        if (hasSpecies) {
            builder.append("<th>Species</th>");
        }
        builder.append("<th>Age</th></tr>");

        for (Taxon taxon : taxonArray) {
            builder.append("<tr><td>");
            builder.append(taxon.getName());
            builder.append("</td>");
            if (hasSpecies) {
                builder.append("<td>");
                builder.append(taxon.getSpecies());
                builder.append("</td>");
            }
            builder.append("<td>");
            builder.append(taxon.getAge());
            builder.append("</td></tr>");
        }

        builder.append("</table></html>");

        setText(builder.toString());
    }
}
