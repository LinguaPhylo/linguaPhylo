package lphy.graphicalModel.code;

import lphy.core.LPhyParser;
import lphy.graphicalModel.Generator;
import lphy.graphicalModel.GraphicalModelNodeVisitor;
import lphy.graphicalModel.Value;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public interface CodeBuilder {

    String valueToCodeString(Value value);

    String generatorToCodeString(Generator generator);

    default String getCode(LPhyParser parser) {
        Set<Value> visited = new HashSet<>();

        List<String> dataLines = new ArrayList<>();
        List<String> modelLines = new ArrayList<>();

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
}
