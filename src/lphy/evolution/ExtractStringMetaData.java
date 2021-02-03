package lphy.evolution;

import java.util.ArrayList;
import java.util.List;

public class ExtractStringMetaData implements TaxaData<String> {

    String label;
    String separator;
    int fieldIndex;

    public ExtractStringMetaData(String label, String fieldSeparator, int fieldIndex) {
            this.label = label;
            this.separator = fieldSeparator;
            this.fieldIndex = fieldIndex;
    }

    public String getName() {
        return label;
    }
    
    public List<String> getData(Taxa taxa) {
        List<String> list = new ArrayList<>();

        for (Taxon t : taxa.getTaxonArray()) {

            String[] fields = t.getName().split(separator);

            list.add(fields[fieldIndex]);
        }

        return list;
    }
}
