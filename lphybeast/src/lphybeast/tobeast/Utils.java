package lphybeast.tobeast;

import beast.evolution.datatype.Aminoacid;
import beast.evolution.datatype.Nucleotide;
import beast.evolution.datatype.StandardData;
import jebl.evolution.sequences.*;

/**
 * @author Walter Xie
 */
public class Utils {

    public static beast.evolution.datatype.DataType guessDataType(SequenceType sequenceType) {
        if (DataType.isSame(DataType.NUCLEOTIDE, sequenceType)) {
            return new Nucleotide();
        } else if (DataType.isSame(DataType.AMINO_ACID, sequenceType)) {
            return new Aminoacid();
        } else if (DataType.isSame(DataType.BINARY, sequenceType)) {
            return new beast.evolution.datatype.Binary();
        } else if (DataType.isSame(DataType.STANDARD, sequenceType)) {
            return new StandardData();
        }
        throw new UnsupportedOperationException(sequenceType.getName());
    }
}
