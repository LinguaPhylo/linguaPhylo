package lphystudio.core.logger;

import lphy.base.evolution.alignment.SimpleAlignment;
import lphy.core.logger.ValueFormatter;
import lphy.core.model.Symbols;

public class AlignmentTextFormatter implements ValueFormatter<SimpleAlignment> {

    SimpleAlignment simpleAlignment;
    String valueID;
//    boolean isClamped;

//    public AlignmentTextFormatter(String valueID, SimpleAlignment simpleAlignment, Boolean isClamped) {
//        this.valueID = Symbols.getCanonical(valueID);
//        this.simpleAlignment = simpleAlignment;
//        this.isClamped = isClamped;
//    }

    public AlignmentTextFormatter(String valueID, SimpleAlignment simpleAlignment) {
        this.valueID = Symbols.getCanonical(valueID);
        this.simpleAlignment = simpleAlignment;
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
