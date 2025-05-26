package lphy.base.logger;

import lphy.base.evolution.tree.TimeTree;
import lphy.base.parser.nexus.NexusUtils;
import lphy.core.logger.ValueFormatter;
import lphy.core.model.Symbols;

import java.io.BufferedWriter;
import java.io.IOException;

public class NexusTreeFormatter implements ValueFormatter<TimeTree> {

    String valueID;
    TimeTree tree;

//    public NexusTreeFormatter() {
//    }

    public NexusTreeFormatter(String valueID, TimeTree tree) {
        this.valueID = Symbols.getCanonical(valueID);
        this.tree = tree;
    }

    @Override
    public void writeToFile(BufferedWriter writer, TimeTree value) {
        // TODO: write trees
        try {
            writer.write(format(value));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public String getExtension() {
        return ".trees";
    }

    @Override
    public Mode getMode() {
        return Mode.VALUE_PER_LINE;
    }

    @Override
    public Class<TimeTree> getDataTypeClass() {
        return TimeTree.class;
    }

    /**
     * @return  the header, which will be shared by the replicates of this tree during simulation.
     */
    @Override
    public String header() {
//        if (this.valueID == null)
//            setValueID(id);
        return NexusUtils.buildHeader(tree);
    }

    @Override
    public String getValueID() {
        return valueID;
    }

    /**
     * @param tree  {@link TimeTree}
     * @return  the Newick representation of the provided {@link TimeTree} as a string,
     *          which will be one line per replicate of this tree during simulation.
     */
    @Override
    public String format(TimeTree tree) {
        return NexusUtils.buildBody(tree);
    }

    /**
     * @return  the footer, which will be shared by the replicates of this tree during simulation.
     */
    @Override
    public String footer() {
        return NexusUtils.buildFooter();
    }

    /**
     * @param rowId  also replicate index
     * @return       the prefix of trees in nexus, such as "tree TREE_0= [&R] ...".
     */
    @Override
    public String getRowName(int rowId) {
        return "\ttree TREE_" + rowId + "= [&R] ";
    }
}
