package lphy.evolution.alignment;

import lphy.evolution.Taxa;

public class ContinuousCharacterData implements TaxaCharacterMatrix<Double> {

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
}
