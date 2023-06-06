package lphystudio.core.codebuilder;

import lphy.core.model.Generator;
import lphy.core.model.Value;
import lphy.core.parser.LPhyMetaParser;
import lphy.core.parser.graphicalmodel.GraphicalModelNodeVisitor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CanonicalCodeBuilder implements CodeBuilder {

    List<String> dataLines = new ArrayList<>();
    List<String> modelLines = new ArrayList<>();

    public String valueToCodeString(Value value) {
        return value.codeString();
    }

    public String generatorToCodeString(Generator generator) {
        return generator.codeString();
    }

    /**
     * @param parser  LPhyParser
     * @return    the lphy code stored in a parser.
     *            NOTE: the code (e.g. constants) may be changed from GUI,
     *            then call this to produce the new code.
     */
    public String getCode(LPhyMetaParser parser) {
        Set<Value> visited = new HashSet<>();
        dataLines.clear();
        modelLines.clear();

        StringBuilder builder = new StringBuilder();
        for (Value value : parser.getModelSinks()) {

            Value.traverseGraphicalModel(value, new GraphicalModelNodeVisitor() {
                @Override
                public void visitValue(Value value) {

                    if (!visited.contains(value)) {

                        if (!value.isAnonymous()) {
                            String str = valueToCodeString(value);
                            if (!str.endsWith(";")) str += ";";
                            if (parser.isNamedDataValue(value)) {
                                dataLines.add(str);
                            } else {
                                modelLines.add(str);
                            }
                        }
                        visited.add(value);
                    }
                }

                public void visitGenerator(Generator generator) {
                }
            }, true);
        }

        if (dataLines.size() > 0) {
            builder.append("data {\n");
            for (String dataLine : dataLines) {
                builder.append("  ");
                builder.append(dataLine);
                builder.append("\n");
            }
            builder.append("}\n");
        }
        if (modelLines.size() > 0) {
            builder.append("model {\n");

            for (String modelLine : modelLines) {
                builder.append("  ");
                builder.append(modelLine);
                builder.append("\n");
            }
            builder.append("}\n");
        }

        return builder.toString();
    }

    /**
     * @return  data lines after calling {@link #getCode(LPhyMetaParser)}
     */
    public String getDataLines() {
        StringBuilder builder = new StringBuilder();
        for (String dataLine : dataLines) {
            builder.append("  ");
            builder.append(dataLine);
            builder.append("\n");
        }
        return builder.toString();
    }

    /**
     * @return  model lines after calling {@link #getCode(LPhyMetaParser)}
     */
    public String getModelLines() {
        StringBuilder builder = new StringBuilder();
        for (String dataLine : modelLines) {
            builder.append("  ");
            builder.append(dataLine);
            builder.append("\n");
        }
        return builder.toString();
    }
}
