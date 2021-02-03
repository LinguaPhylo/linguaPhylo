package lphy.evolution;

import lphy.evolution.alignment.Alignment;

import java.util.List;

public interface TaxaData<T> {

    /**
     * @return meta data name, such as location.
     */
    String getName();

    List<T> getData(Taxa taxa);

    Alignment extractTraitAlignment(Taxa taxa);
}
