package lphy.evolution;

import jebl.evolution.sequences.State;
import lphy.evolution.alignment.Alignment;
import lphy.evolution.alignment.SimpleAlignment;
import lphy.evolution.datatype.Standard;

import java.util.*;
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

    /**
     * @param taxa
     * @return      The list of meta data (traits) extracted from {@link Taxa} names.
     */
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

    /**
     * @param taxa
     * @return      An {@link Alignment} of meta data (traits) extracted from {@link Taxa} names.
     *              The taxa of {@link Alignment} will be same as given {@link Taxa}.
     *              The sequences will be 1-site meta data.
     */
    @Override
    public Alignment extractTraitAlignment(Taxa taxa) {
        // list of traits
        List<String> traitList = getData(taxa);
        // no sorting unique traits
        Set<String> uniqTraitVal = new LinkedHashSet<>(traitList);
        List<String> uniqueTraits = new ArrayList<>(uniqTraitVal);

        // create Standard data type, where state code is the index of unique traits
        Standard standard = new Standard(uniqueTraits);

        // 1 site only
        SimpleAlignment traitAl = new SimpleAlignment(taxa, 1, standard);
        // fill in trait alignment, maintaining the same order
        State state;
        int demeIndex;
        for (int t = 0; t < traitList.size(); t++) {
//            int demeIndex = standard.getStateNameIndex(traitList.get(t));
            state = standard.getStateFromName(traitList.get(t));
            demeIndex = Objects.requireNonNull(state).getIndex();
            traitAl.setState(t, 0, demeIndex);
        }
        return traitAl;
    }
}
