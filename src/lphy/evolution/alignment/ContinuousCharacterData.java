package lphy.evolution.alignment;

import lphy.evolution.Taxa;

import java.util.Arrays;
import java.util.Objects;

public class ContinuousCharacterData implements TaxaCharacterMatrix<Double> {

    // 1st[] is taxa, index is same order as Taxa
    // 2nd[] is traits
    Double[][] continuousCharacterData;
    Taxa taxa;

    public ContinuousCharacterData(Taxa taxa, Double[][] continuousCharacterData) {
        this.taxa = taxa;
        this.continuousCharacterData = continuousCharacterData;
    }

    public Double getState(String taxonName, int column) {
        return continuousCharacterData[taxa.indexOfTaxon(taxonName)][column];
    }

    @Override
    public Class getComponentType() {
        return Double.class;
    }

    @Override
    public Taxa getTaxa() {
        return taxa;
    }

    @Override
    public int nchar() {
        return continuousCharacterData[0].length;
    }

    @Override
    public String toJSON() {
        StringBuilder builder = new StringBuilder();
        builder.append("{\n");
        for (int i = 0; i < Objects.requireNonNull(taxa).ntaxa(); i++) {
            builder.append("  ").append(taxa.getTaxon(i));
            builder.append(" = ").append(Arrays.toString(continuousCharacterData[i]));
//            if (i < n()-1)
            builder.append(",");
            builder.append("\n");
        }
        builder.append("  ntax = ").append(taxa.ntaxa());
        builder.append("\n").append("}");
        return builder.toString();
    }
}
