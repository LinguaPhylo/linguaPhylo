package lphystudio.core.logger;

import lphy.base.evolution.tree.TimeTree;
import lphy.core.logger.ValueFormatter;
import lphy.core.model.Symbols;

public class TreeTextFormatter implements ValueFormatter<TimeTree> {

    String valueID;
    TimeTree tree;

//    public NexusTreeFormatter() {
//    }

    public TreeTextFormatter(String valueID, TimeTree tree) {
        this.valueID = Symbols.getCanonical(valueID);
        this.tree = tree;
    }

    @Override
    public String getExtension() {
        return ".txt";
    }

    @Override
    public Mode getMode() {
        return Mode.VALUE_PER_LINE;
    }

    @Override
    public Class<TimeTree> getDataTypeClass() {
        return TimeTree.class;
    }

    @Override
    public String getValueID() {
        return valueID;
    }

    @Override
    public String format(TimeTree tree) {
        return tree.toNewick(false);
    }

}
