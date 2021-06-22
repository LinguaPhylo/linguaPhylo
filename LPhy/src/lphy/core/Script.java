package lphy.core;

import java.io.BufferedReader;
import java.io.IOException;

public class Script {

    public String dataLines;
    public String modelLines;

    public Script(String dataLines, String modelLines) {
        this.dataLines = dataLines;
        this.modelLines = modelLines;

    }

    public static Script loadLPhyScript(BufferedReader reader) throws IOException {

        StringBuilder dataLines = new StringBuilder();
        StringBuilder modelLines = new StringBuilder();

        boolean skip;

        // TODO will be used when for loops are fully supported
        int level = 0;

        String line = reader.readLine();
        LPhyParser.Context context = LPhyParser.Context.model;
        while (line != null) {
            skip = false;
            if (line.matches("[ \\t]*data[ \\t]*\\{[ \\t]*")) {
                context = LPhyParser.Context.data;
                skip = true;
            } else if (line.matches("[ \\t]*model[ \\t]*\\{[ \\t]*")) {
                context = LPhyParser.Context.model;
                skip = true;
            } else if (line.matches("[ \\t]*}[ \\t]*")) {
                // this line is just closing a data or model block.
                skip = true;
            }

            if (!skip) {
                switch (context) {
                    case data:
                        dataLines.append(line);
                        dataLines.append("\n");
                        break;
                    case model:
                        modelLines.append(line);
                        modelLines.append("\n");
                        break;
                }
            }
            line = reader.readLine();
        }
        reader.close();

        return new Script(dataLines.toString(), modelLines.toString());
    }
}
