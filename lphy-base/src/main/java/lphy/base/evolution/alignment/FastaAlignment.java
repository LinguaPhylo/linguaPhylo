package lphy.base.evolution.alignment;

import jebl.evolution.sequences.SequenceType;
import lphy.base.evolution.Taxa;
import lphy.core.logger.LoggerUtils;
import lphy.core.logger.TextFileFormatted;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FastaAlignment extends SimpleAlignment implements TextFileFormatted {

    public FastaAlignment(Map<String, Integer> idMap, int nchar, SequenceType sequenceType) {
        super(idMap, nchar, sequenceType);
    }

    public FastaAlignment(Taxa taxa, int nchar, SequenceType sequenceType) {
        super(taxa, nchar, sequenceType);
    }

    public FastaAlignment(int nchar, Alignment source) {
        super(nchar, source);
    }

    @Override
    public List<String> getTextForFile() {
        List<String> lines = new ArrayList<>();

        for (int i=0; i < ntaxa(); i++) {
            try {
                String taxonName = getTaxaNames()[i];
                lines.add(">" + taxonName);
                String sequence = getSequence(i);
                lines.add(sequence);
            } catch (Exception ex) {
                LoggerUtils.log.severe("Error at " + i + " taxa (" + getTaxaNames()[i] + ") in " +
                        this.getClass().getName());
            }
        }

        return lines;
    }

    @Override
    public String getFileType() {
        return ".fasta";
    }


}
