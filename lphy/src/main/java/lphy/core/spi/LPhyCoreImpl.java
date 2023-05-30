package lphy.core.spi;

import jebl.evolution.sequences.SequenceType;
import lphy.core.graphicalmodel.components.Func;
import lphy.core.graphicalmodel.components.GenerativeDistribution;
import lphy.core.parser.functions.Range;
import lphy.core.parser.functions.SliceDoubleArray;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The "Container" provider class that implements SPI
 * which include a list of {@link GenerativeDistribution}, {@link Func} required in the core.
 * It requires a public no-args constructor.
 * @author Walter Xie
 */
public class LPhyCoreImpl implements LPhyExtension {

    List<Class<? extends Func>> functions = Arrays.asList(
            Range.class, SliceDoubleArray.class);

    /**
     * Required by ServiceLoader.
     */
    public LPhyCoreImpl() {
        //TODO do something here, e.g. print package or classes info ?
    }

    @Override
    public List<Class<? extends GenerativeDistribution>> getDistributions() {
        return new ArrayList<>();
    }

    @Override
    public List<Class<? extends Func>> getFunctions() {
        return functions;
    }

    @Override
    public Map<String, ? extends SequenceType> getSequenceTypes() {
        Map<String, SequenceType> dataTypeMap = new ConcurrentHashMap<>();
        dataTypeMap.put("rna", SequenceType.NUCLEOTIDE);
        dataTypeMap.put("dna", SequenceType.NUCLEOTIDE);
        dataTypeMap.put(SequenceTypeFactory.sanitise(SequenceType.NUCLEOTIDE.getName()), SequenceType.NUCLEOTIDE); // nucleotide

        dataTypeMap.put(SequenceTypeFactory.sanitise(SequenceType.AMINO_ACID.getName()), SequenceType.AMINO_ACID); // aminoacid
        dataTypeMap.put("protein", SequenceType.AMINO_ACID);

        return dataTypeMap;
    }
}
