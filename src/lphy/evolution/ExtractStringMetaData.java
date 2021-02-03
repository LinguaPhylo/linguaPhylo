package lphy.evolution;

import lphy.evolution.alignment.Alignment;
import lphy.evolution.alignment.SimpleAlignment;
import lphy.evolution.sequences.Standard;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public class ExtractStringMetaData implements TaxaData<String> {

    String label;
    String separator;
    int fieldIndex;


    /**
     *
     * @param label           meta data name, such as location.
     * @param fieldSeparator  Java regular expression escape characters will be given no special meaning.
     * @param fieldIndex      index starts from 0.
     */
    public ExtractStringMetaData(String label, String fieldSeparator, int fieldIndex) {
            this.label = label;
            this.separator = fieldSeparator;
            this.fieldIndex = fieldIndex;
    }

    @Override
    public String getName() {
        return label;
    }

    @Override
    public List<String> getData(Taxa taxa) {
        List<String> list = new ArrayList<>();

        for (Taxon t : taxa.getTaxonArray()) {

            // exact match, no regex escape characters
            String[] fields = t.getName().split(Pattern.quote(separator));
            if (fields.length <= fieldIndex)
                throw new IllegalArgumentException("Cannot find " + fieldIndex +
                        "th element after splitting name " + t + " by substring " + separator + " !");

            list.add(fields[fieldIndex]);
        }

        return list;
    }

    @Override
    public Alignment extractTraitAlignment(Taxa taxa) {
        List<String> traitList = getData(taxa);
        // no sorting demes
        Set<String> uniqTraitVal = new LinkedHashSet<>(traitList);
        List<String> uniqueDemes = new ArrayList<>(uniqTraitVal);
        // state names are sorted unique demes
        Standard standard = new Standard(uniqueDemes);
        SimpleAlignment traitAl = new SimpleAlignment(taxa, 1, standard);
        // fill in trait values, traitVal and taxaNames have to maintain the same order
        for (int t = 0; t < traitList.size(); t++) {
            int demeIndex = standard.getStateNameIndex(traitList.get(t));
            traitAl.setState(t, 0, demeIndex);
        }
        return traitAl;
    }
}
