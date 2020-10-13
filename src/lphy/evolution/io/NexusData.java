package lphy.evolution.io;

import lphy.evolution.Taxon;
import lphy.evolution.alignment.Alignment;
import lphy.evolution.alignment.TaxaCharacterMatrix;
import lphy.evolution.traits.CharSetBlock;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Everything from the nexus file, saved as LPhy objects.
 * Not only taxa and sequences, but also ages and other attributes
 * which are inside {@link Taxon}.
 * For example, <code>getData().getTaxa().getAges()</code>
 *
 * @author Walter Xie
 */
public class NexusData<T> {

    // for alignment before partitioning by charsets, and continuous data
    protected TaxaCharacterMatrix<T> data;

    protected Map<String, List<CharSetBlock>> charsetMap;

//    protected ChronoUnit chronoUnit = null;


    public Alignment getOriginalAlignment() {
        if (! (Objects.requireNonNull(data) instanceof Alignment) )
            throw new IllegalArgumentException("Data imported from the nexus file " +
                    "is not an alignment ! " + data.getClass());
        return (Alignment) data;
    }

    public TaxaCharacterMatrix<T> getData() {
        return data;
    }

    public void setData(TaxaCharacterMatrix<T> data) {
        this.data = data;
    }

    public Map<String, List<CharSetBlock>> getCharsetMap() {
        return charsetMap;
    }

    public void setCharsetMap(Map<String, List<CharSetBlock>> charsetMap) {
        this.charsetMap = charsetMap;
    }


}
