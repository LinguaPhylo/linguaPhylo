package lphystudio.core.log;

import lphy.evolution.alignment.SimpleAlignment;
import lphy.graphicalModel.RandomValueLogger;
import lphy.graphicalModel.Value;
import lphy.nexus.NexusWriter;
import lphy.util.LoggerUtils;

import javax.swing.*;
import java.io.File;
import java.io.PrintStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import static lphystudio.app.graphicalmodelpanel.AlignmentLogPanel.getAlignmentDir;
import static lphystudio.app.graphicalmodelpanel.AlignmentLogPanel.isLogAlignment;

/**
 * @author Walter Xie
 */
public class AlignmentLog extends JTextArea implements RandomValueLogger {

    public AlignmentLog() {
    }

    public void clear() {
        setText("");
    }

    public void log(int rep, List<Value<?>> values) {
        List<Value<SimpleAlignment>> alignmentVariables = getAlignmentValues(values);

        if (rep == 0) {
            setText("alignment");
            for (Value<SimpleAlignment> al : alignmentVariables) {
                append("\t" + al.getId());
            }
            append("\n");
        }

        append(rep+"");
        for (Value<SimpleAlignment> al : alignmentVariables) {
            append("\t" + al.value().getSummary());
            if (isLogAlignment())
                logAlignment(al, rep);
        }
        append("\n");
    }

    public void close() {

    }

    private List<Value<SimpleAlignment>> getAlignmentValues(List<Value<?>> variables) {
        List<Value<SimpleAlignment>> values = new ArrayList<>();
        for (Value<?> v : variables) {
            if (v.value() instanceof SimpleAlignment)
                values.add((Value<SimpleAlignment>) v);
        }
        values.sort(Comparator.comparing(Value::getCanonicalId));
        return values;
    }

    private void logAlignment(Value<SimpleAlignment> alignment, int rep) {
        String dir = getAlignmentDir();
        String fileName = alignment.getCanonicalId() + "_" + rep + ".nexus";

        try {
            File file = Paths.get(dir, fileName).toFile();
            if (!file.getParentFile().exists())
                throw new IllegalArgumentException("Directory " + file.getParentFile() + " does not exist !");
            PrintStream stream = new PrintStream(file);
            // no tree
            NexusWriter.write(alignment.value(), new LinkedList<>(), stream);
        } catch (Exception e) {
            LoggerUtils.logStackTrace(e);
            e.printStackTrace();
        }
    }

}