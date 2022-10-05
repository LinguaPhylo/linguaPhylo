package lphy.graphicalModel.logger;

import lphy.evolution.alignment.SimpleAlignment;
import lphy.evolution.tree.TimeTree;
import lphy.graphicalModel.RandomValueLogger;
import lphy.graphicalModel.RandomVariable;
import lphy.graphicalModel.Value;
import lphy.nexus.NexusWriter;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Alexei Drummond on 10/03/20.
 */
public class AlignmentFileLogger implements RandomValueLogger {

    String name;
    List<FileWriter> fileWriter = new ArrayList<>();

    public AlignmentFileLogger(String name) {

        this.name = name;
    }

    public void log(int rep, List<Value<?>> values) {

        for (Value<SimpleAlignment> v : getAlignmentValues(values)) {
            try {
                logAlignment(v, rep);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void close() {
        for (FileWriter writer : fileWriter) {
            try {
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void openFiles(List<RandomVariable<TimeTree>> treeVariables) throws IOException {
        for (RandomVariable<TimeTree> tree : treeVariables) {
            String fileName = name + "_" + tree.getId() + ".trees";
            fileWriter.add(new FileWriter(fileName));
        }
    }

    private List<Value<SimpleAlignment>> getAlignmentValues(List<Value<?>> values) {
        List<Value<SimpleAlignment>> alignments = new ArrayList<>();
        for (Value v : values) {
            if (v.value() instanceof SimpleAlignment) {
                alignments.add((Value<SimpleAlignment>)v);
            }
        }
        return alignments;
    }

    private void logAlignment(Value<SimpleAlignment> alignment, int rep) throws IOException {
        String fileName = name + "_" + alignment.getId() + "_" + rep + ".nexus";
        PrintStream stream = new PrintStream(fileName);

        try {
            NexusWriter.write(alignment.value(), new LinkedList<>(),stream);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
