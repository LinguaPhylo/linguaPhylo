package lphy.base.logger;

import lphy.base.evolution.alignment.SimpleAlignment;
import lphy.base.parser.nexus.NexusUtils;
import lphy.core.logger.ValueFormatter;
import lphy.core.model.Value;

import java.util.Arrays;

public class NexusAlignmentFormatter implements ValueFormatter {


    public NexusAlignmentFormatter() {
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
    public String[] header(Value<?> value) {
        if (value.value() instanceof SimpleAlignment ||
                value.value() instanceof SimpleAlignment[] ||
                value.value() instanceof SimpleAlignment[][]) {

            return Arrays.stream(getValues(value))
                    .map(SimpleAlignment.class::cast)
                    .map(NexusUtils::buildHeader)
                    .toArray(String[]::new);
        }
        return ValueFormatter.super.header(value);
    }

    @Override
    public String[] format(Value<?> value) {
        if (value.value() instanceof SimpleAlignment ||
                value.value() instanceof SimpleAlignment[] ||
                value.value() instanceof SimpleAlignment[][]) {

            return Arrays.stream(getValues(value))
                    .map(SimpleAlignment.class::cast)
                    .map(NexusUtils::buildBody)
                    .toArray(String[]::new);
        }
        return ValueFormatter.super.format(value);
    }

    @Override
    public String[] footer(Value<?> value) {
        if (value.value() instanceof SimpleAlignment ||
                value.value() instanceof SimpleAlignment[] ||
                value.value() instanceof SimpleAlignment[][]) {

            return new String[]{NexusUtils.buildFooter()};
        }
        return ValueFormatter.super.footer(value);
    }

    @Override
    public String getRowName(int rowId) {
        return "\t"; // used to indent
    }
}
