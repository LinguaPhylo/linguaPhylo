package lphy.base.logger;

import lphy.base.evolution.Taxa;
import lphy.base.evolution.alignment.ContinuousCharacterData;
import lphy.core.logger.ValueFormatter;
import lphy.core.model.Symbols;

import java.io.BufferedWriter;
import java.io.IOException;

/**
 * Writes a {@link ContinuousCharacterData} value as a CSV file with header
 * {@code taxon,dim0,dim1,...} and one row per taxon. One file per value per
 * replicate, extension {@code .csv}.
 *
 * <p>This is the default serialisation for continuous character matrices
 * produced by e.g. {@link lphy.base.evolution.continuous.PhyloBrownian} and
 * {@link lphy.base.evolution.continuous.PhyloMultivariateBrownian}.</p>
 */
public class ContinuousCharacterDataCSVFormatter implements ValueFormatter<ContinuousCharacterData> {

    private final String valueID;
    private final ContinuousCharacterData value;

    public ContinuousCharacterDataCSVFormatter(String valueID, ContinuousCharacterData value) {
        this.valueID = Symbols.getCanonical(valueID);
        this.value = value;
    }

    @Override
    public void writeToFile(BufferedWriter writer, ContinuousCharacterData value) {
        try {
            writer.write(format(value));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public String getExtension() {
        return ".csv";
    }

    @Override
    public Mode getMode() {
        return Mode.VALUE_PER_FILE;
    }

    @Override
    public Class<ContinuousCharacterData> getDataTypeClass() {
        return ContinuousCharacterData.class;
    }

    @Override
    public String header() {
        int dims = value.nchar();
        StringBuilder sb = new StringBuilder("taxon");
        for (int d = 0; d < dims; d++) sb.append(",dim").append(d);
        sb.append("\n");
        return sb.toString();
    }

    @Override
    public String getValueID() {
        return valueID;
    }

    @Override
    public String format(ContinuousCharacterData ccd) {
        Taxa taxa = ccd.getTaxa();
        int ntaxa = taxa.ntaxa();
        int dims = ccd.nchar();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < ntaxa; i++) {
            sb.append(taxa.getTaxon(i).getName());
            for (int d = 0; d < dims; d++) {
                sb.append(",");
                sb.append(ccd.getState(i, d));
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    @Override
    public String footer() {
        return "";
    }

    @Override
    public String getRowName(int rowId) {
        return "";
    }
}
