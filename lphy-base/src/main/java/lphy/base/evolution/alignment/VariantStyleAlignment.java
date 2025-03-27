package lphy.base.evolution.alignment;

import jebl.evolution.sequences.SequenceType;
import lphy.base.evolution.CellPosition;

import java.util.Map;

public class VariantStyleAlignment extends AbstractAlignment {
    // root sequence (one sequence alignment)
    Alignment alignment;
    // variants
    Map<CellPosition, Integer> variantStore;
    String[] taxaNames;

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

    private CellPosition getCellPosition(int taxon, int position) {
        if (taxaNames == null || taxon >= taxaNames.length) {
            throw new IllegalArgumentException("Invalid taxon index: " + taxon);
        }
        String taxaName = taxaNames[taxon];
        return new CellPosition(taxaName, position);
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

}
