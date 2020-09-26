package lphy.evolution.alignment;

import lphy.evolution.Taxa;

public class ContinuousCharacterData implements TaxaCharacterMatrix<Double> {

    Double[][] continousCharacterData;
    Taxa taxa;

    public ContinuousCharacterData(Taxa taxa, Double[][] continousCharacterData) {
        this.taxa = taxa;
        this.continousCharacterData = continousCharacterData;
    }

    public Double getState(String taxon, int column) {
        return continousCharacterData[taxa.indexOfTaxon(taxon)][column];
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
        return continousCharacterData[0].length;
    }
}
