package lphy.base.logger;

import lphy.base.evolution.tree.TimeTree;
import lphy.base.parser.nexus.NexusUtils;
import lphy.core.logger.ValueFormatter;
import lphy.core.model.Value;

import java.util.Arrays;

public class NexusTreeFormatter implements ValueFormatter {


    public NexusTreeFormatter() {
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
    public String[] header(Value<?> value) {
        if (value.value() instanceof TimeTree ||
                value.value() instanceof TimeTree[] ||
                value.value() instanceof TimeTree[][]) {

            return Arrays.stream(getValues(value))
                    .map(TimeTree.class::cast)
                    .map(NexusUtils::buildHeader)
                    .toArray(String[]::new);
        }
        return ValueFormatter.super.header(value);
    }

    @Override
    public String[] format(Value<?> value) {
        if (value.value() instanceof TimeTree ||
                value.value() instanceof TimeTree[] ||
                value.value() instanceof TimeTree[][]) {

            return Arrays.stream(getValues(value))
                    .map(TimeTree.class::cast)
                    .map(NexusUtils::buildBody)// do not use buildBody
                    .toArray(String[]::new);
        }
        return ValueFormatter.super.format(value);
    }

    @Override
    public String[] footer(Value<?> value) {
        if (value.value() instanceof TimeTree ||
                value.value() instanceof TimeTree[] ||
                value.value() instanceof TimeTree[][]) {

            return new String[]{NexusUtils.buildFooter()};
        }
        return ValueFormatter.super.footer(value);
    }

    @Override
    public String getRowName(int rowId) {
        return "\ttree TREE_" + rowId + "= [&R] ";
    }
}
