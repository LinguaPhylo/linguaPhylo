package lphy.base.evolution.alignment;

import jebl.evolution.sequences.SequenceType;
import lphy.core.logger.LoggerUtils;
import lphy.core.logger.TextFileFormatted;

import java.util.ArrayList;
import java.util.List;

/**
 * The lphy data type implements {@link TextFileFormatted},
 * which will trigger logging as a fasta format to file.
 */
public class FastaAlignment implements Alignment, TextFileFormatted {

    final Alignment alignment;

    public FastaAlignment(Alignment source) {
        alignment = source;
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


    @Override
    public void setState(int taxon, int position, int state) {
        throw new UnsupportedOperationException("Unsupported for FastaAlignment !");
    }

    @Override
    public int getState(int taxon, int position) {
        return alignment.getState(taxon, position);
    }

    @Override
    public SequenceType getSequenceType() {
        return alignment.getSequenceType();
    }

    @Override
    public String getTaxonName(int taxonIndex) {
        return alignment.getTaxonName(taxonIndex);
    }

    @Override
    public String toJSON() {
        return alignment.toJSON();
    }

    @Override
    public Integer nchar() {
        return alignment.nchar();
    }

    @Override
    public int ntaxa() {
        return alignment.ntaxa();
    }
}
