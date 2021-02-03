package lphy.evolution;

import java.util.List;

public interface TaxaData<T> {

    String getName();

    List<T> getData(Taxa taxa);
}
