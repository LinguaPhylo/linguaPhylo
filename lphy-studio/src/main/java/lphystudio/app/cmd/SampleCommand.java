package lphystudio.app.cmd;

import lphy.core.model.components.Value;
import lphy.core.parser.Command;
import lphy.io.logger.AlignmentFileLogger;
import lphy.io.logger.RandomValueLogger;
import lphy.io.logger.TreeFileLogger;
import lphy.io.logger.VarFileLogger;
import lphystudio.app.graphicalmodelpanel.GraphicalModelPanel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by adru001 on 10/03/20.
 */
public class SampleCommand implements Command {

    GraphicalModelPanel graphicalModelPanel;

    static String[] arguments = {"n", "logFile", "treeFiles", "alignmentFiles", "name"};
    static Object[] defaults = {1, false, false, false, "model"};

    public SampleCommand(GraphicalModelPanel graphicalModelPane) {
        this.graphicalModelPanel = graphicalModelPanel;
    }

    public String getName() {
        return "sample";
    }

    public String[] getArgumentNames() {
        return arguments;
    }

    public Object[] getDefaultValues() {
        return defaults;
    }

    public String getSignature() {
        String[] argumentNames = getArgumentNames();
        Object[] defaultValues = getDefaultValues();

        StringBuilder builder = new StringBuilder();
        builder.append(getName());
        builder.append("(");
        builder.append(argumentNames[0]);
        builder.append("=");
        builder.append(defaultValues[0]);
        for (int i = 1; i < argumentNames.length; i++) {
            builder.append(", ");
            builder.append(argumentNames[i]);
            builder.append("= ");
            String defaultValue = defaultValues[i].toString();
            if (defaultValues[i] instanceof String) {
                defaultValue = "\"" + defaultValue + "\"";
            }
            builder.append(defaultValue);
        }
        builder.append(");");
        return builder.toString();
    }

    public void execute(Map<String, Value<?>> params) {

        Arguments args = new Arguments(params);

        int n = args.getInteger(arguments[0], defaults[0]);
        boolean writeVarsToFile = args.getBoolean(arguments[1], defaults[1]);
        boolean writeTreesToFile = args.getBoolean(arguments[2], defaults[2]);
        boolean writeAlignmentsToFile = args.getBoolean(arguments[3], defaults[3]);
        String name = args.getString(arguments[4], defaults[4]);

        List<RandomValueLogger> loggers = new ArrayList<>();

        if (writeVarsToFile) {
            System.out.println("writing to file!");
            loggers.add(new VarFileLogger(name, true, true));
        }
        if (writeTreesToFile) loggers.add(new TreeFileLogger(name));
        if (writeAlignmentsToFile) loggers.add(new AlignmentFileLogger(name));

        graphicalModelPanel.sample(n, loggers);
    }
}
