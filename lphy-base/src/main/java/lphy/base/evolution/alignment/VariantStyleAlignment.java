package lphy.base.evolution.alignment;

import jebl.evolution.sequences.SequenceType;
import lphy.base.evolution.CellPosition;
import lphy.base.evolution.datatype.Variant;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static lphy.base.evolution.datatype.Variant.inferGenotype;

public class VariantStyleAlignment extends AbstractAlignment {
    // root sequence (one sequence alignment)
    Alignment alignment;
    // variants
    Map<CellPosition, Integer> variantStore;
    String[] taxaNames;
    List<Variant> variants = new ArrayList<>();

    public VariantStyleAlignment(Map<String, Integer> idMap, Alignment root, Map<CellPosition, Integer> variantStore) {
        super(idMap, root, variantStore);

        if (root.length() != 1) {
            throw new IllegalArgumentException("The root alignment must contain exactly one sequence.");
        }

        if (sequenceType.getName().equals(SequenceType.AMINO_ACID.getName()) || sequenceType.getName().equals(SequenceType.CODON.getName())) {
            throw new IllegalArgumentException("The VariantStyleAlignment does not support AMINO_ACID and CODON variants.");
        }

        taxaNames = idMap.keySet().toArray(new String[idMap.size()]);
        this.alignment = root;
        this.variantStore = variantStore;
    }

    @Override
    public void setState(int taxon, int position, Integer state) {
        if (state < 0 || state > getStateCount())
            throw new IllegalArgumentException("Illegal to set a " + sequenceType.getName() +
                    " state outside of the range [0, " + (sequenceType.getStateCount() - 1) + "] ! state = " + state);

        CellPosition cellPosition = getCellPosition(taxon, position);
        variantStore.put(cellPosition, state);
    }

    public void setState(String taxonName, int position, Integer state) {
        setState(indexOfTaxon(taxonName), position, state);
    }

    private CellPosition getCellPosition(int taxon, int position) {
        if (taxaNames == null || taxon >= taxaNames.length) {
            throw new IllegalArgumentException("Invalid taxon index: " + taxon);
        }
        String taxaName = taxaNames[taxon];
        return new CellPosition(taxaName, position);
    }

    public CellPosition getCellPosition(String taxonName, int position) {
        CellPosition cellPosition = getCellPosition(taxonName, position);
        return cellPosition;
    }

    @Override
    public String toJSON() {
        return "";
    }

    @Override
    public int getState(int taxon, int position) {
        CellPosition cellPosition = getCellPosition(taxon, position);
        // if contains, then it's a variant site
        if (variantStore.containsKey(cellPosition)) {
            return variantStore.get(cellPosition);
        } else {
            // if not, then return the root sequence state
            return alignment.getState(alignment.length()-1, position);
        }
    }

    public Integer getState(String taxonName, int position) {
        return getState(indexOfTaxon(taxonName), position);
    }

    public Map<CellPosition, Integer> getVariantStore() {
        return variantStore;
    }

    public Alignment getRoot(){
        return alignment;
    }

    public Variant[] mapVariants(SequenceType sequenceType){
        for (Map.Entry<CellPosition, Integer> entry : variantStore.entrySet()) {
            String taxonName = entry.getKey().getCellName();
            int state = entry.getValue();
            int pos = entry.getKey().getPosition();
            if (sequenceType.getName().equals(SequenceType.AMINO_ACID.getName())) {
                int ref = alignment.getState(alignment.length()-1 , pos);
                int alt = state;
                String genotype = "1";
                Variant variant = new Variant(taxonName,pos,ref,alt,genotype);
                variants.add(variant);
            } else {
                // only support phasedGenotype
                int ref = state / 4;
                int alt = state % 4;
                String genotype = inferGenotype(ref,alt);
                Variant variant = new Variant(taxonName,pos,ref,alt,genotype);
                variants.add(variant);
            }
        }
        return variants.toArray(new Variant[variants.size()]);
    }
}
