package lphy.base.spi;

import jebl.evolution.sequences.SequenceType;
import lphy.base.evolution.datatype.Binary;
import lphy.base.evolution.datatype.Continuous;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The "Container" provider class that implements SPI
 * which include a list of {@link SequenceType} to extend.
 * It requires a public no-args constructor.
 * @author Walter Xie
 */
public class SequenceTypeBaseImpl implements SequenceTypeExtension {

    /**
     * Required by ServiceLoader.
     */
    public SequenceTypeBaseImpl() {
        //TODO do something here, e.g. print package or classes info ?
    }

    @Override
    public Map<String, ? extends SequenceType> declareSequenceTypes() {
        Map<String, SequenceType> dataTypeMap = new ConcurrentHashMap<>();
        dataTypeMap.put("rna", SequenceType.NUCLEOTIDE);
        dataTypeMap.put("dna", SequenceType.NUCLEOTIDE);
        dataTypeMap.put(SequenceTypeLoader.sanitise(SequenceType.NUCLEOTIDE.getName()), SequenceType.NUCLEOTIDE); // nucleotide

        dataTypeMap.put(SequenceTypeLoader.sanitise(SequenceType.AMINO_ACID.getName()), SequenceType.AMINO_ACID); // aminoacid
        dataTypeMap.put("protein", SequenceType.AMINO_ACID);

        dataTypeMap.put(SequenceTypeLoader.sanitise(Binary.NAME), Binary.getInstance());
        dataTypeMap.put(SequenceTypeLoader.sanitise(Continuous.NAME), Continuous.getInstance());
        return dataTypeMap;
    }

    @Override
    public void register() {

    }

    public String getExtensionName() {
        return "LPhy base";
    }
}
