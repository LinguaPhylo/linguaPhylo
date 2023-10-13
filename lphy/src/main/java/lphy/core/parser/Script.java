package lphy.core.parser;

import java.io.BufferedReader;
import java.io.IOException;

/**
 * This class was used to recognise the data and model block,
 * which has been moved to the grammar now.
 */
@Deprecated
public class Script {

    public String dataLines;
    public String modelLines;

    public Script(String dataLines, String modelLines) {
        this.dataLines = dataLines;
        this.modelLines = modelLines;

    }

    @Deprecated
    public static Script loadLPhyScript(BufferedReader reader) throws IOException {

        StringBuilder dataLines = new StringBuilder();
        StringBuilder modelLines = new StringBuilder();

        boolean skip;

        // TODO will be used when for loops are fully supported
        int level = 0;

        String line = reader.readLine();
        LPhyMetaData.Context context = LPhyMetaData.Context.model;
        while (line != null) {
            skip = false;
            if (line.matches("[ \\t]*data[ \\t]*\\{[ \\t]*")) {
                context = LPhyMetaData.Context.data;
                skip = true;
            } else if (line.matches("[ \\t]*model[ \\t]*\\{[ \\t]*")) {
                context = LPhyMetaData.Context.model;
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
