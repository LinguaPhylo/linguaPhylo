package lphystudio.core.logger;

import lphy.base.evolution.alignment.SimpleAlignment;
import lphy.core.logger.ValueFormatter;
import lphy.core.model.Symbols;

import java.io.BufferedWriter;

public class AlignmentTextFormatter implements ValueFormatter<SimpleAlignment> {

    SimpleAlignment simpleAlignment;
    String valueID;

    public AlignmentTextFormatter(String valueID, SimpleAlignment simpleAlignment) {
        this.valueID = Symbols.getCanonical(valueID);
        this.simpleAlignment = simpleAlignment;
    }

    @Override
    public void writeToFile(BufferedWriter writer, SimpleAlignment simpleAlignment) {
        for (int i = 0; i < simpleAlignment.ntaxa(); i++) {
            try {
                writer.write(">" + simpleAlignment.getTaxonName(i) + "\n");
                for (int j = 0; j < simpleAlignment.nchar(); j++) {
                    writer.write(simpleAlignment.getCharacter(i, j));
                }
                writer.write("\n");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    @Override
    public String getExtension() {
        return ".txt";
    }

    @Override
    public Mode getMode() {
        return Mode.VALUE_PER_FILE;
    }

    @Override
    public Class<SimpleAlignment> getDataTypeClass() {
        return SimpleAlignment.class;
    }

    @Override
    public String getValueID() {
        return valueID;
    }

    @Override
    public String format(SimpleAlignment simpleAlignment) {
        return simpleAlignment.toString();
    }


}
