package lphy.base.logger;

import lphy.base.evolution.alignment.SimpleAlignment;
import lphy.base.parser.nexus.NexusUtils;
import lphy.core.logger.ValueFormatter;
import lphy.core.model.Symbols;

public class NexusAlignmentFormatter implements ValueFormatter<SimpleAlignment> {

    SimpleAlignment simpleAlignment;
    String valueID;

//    public NexusAlignmentFormatter() {
//    }

    public NexusAlignmentFormatter(String valueID, SimpleAlignment simpleAlignment) {
        this.valueID = Symbols.getCanonical(valueID);
        this.simpleAlignment = simpleAlignment;
    }

    @Override
    public String getExtension() {
        return ".nexus";
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
    public String header() {
//        if (this.valueID == null)
//            setValueID(id);
        return NexusUtils.buildHeader(simpleAlignment);
    }

    @Override
    public String getValueID() {
        return valueID;
    }

    @Override
    public String format(SimpleAlignment simpleAlignment) {
        return NexusUtils.buildBody(simpleAlignment);
    }

    @Override
    public String footer() {
        return NexusUtils.buildFooter();
    }

    @Override
    public String getRowName(int rowId) {
        // not require indent here, CharactersBlock handles indents
        return "";
    }


}
