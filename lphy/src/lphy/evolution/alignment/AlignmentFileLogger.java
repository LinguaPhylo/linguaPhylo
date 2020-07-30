package lphy.evolution.alignment;

import lphy.evolution.tree.TimeTree;
import lphy.graphicalModel.RandomVariable;
import lphy.graphicalModel.RandomVariableLogger;
import lphy.nexus.NexusWriter;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by adru001 on 10/03/20.
 */
public class AlignmentFileLogger implements RandomVariableLogger {

    String name;
    List<FileWriter> fileWriter = new ArrayList<>();

    public AlignmentFileLogger(String name) {

        this.name = name;
    }

    public void log(int rep, List<RandomVariable<?>> variables) {

        for (RandomVariable<Alignment> v : getAlignmentVariables(variables)) {
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

    private List<RandomVariable<Alignment>> getAlignmentVariables(List<RandomVariable<?>> variables) {
        List<RandomVariable<Alignment>> alignments = new ArrayList<>();
        for (RandomVariable v : variables) {
            if (v.value() instanceof Alignment) {
                alignments.add((RandomVariable<Alignment>)v);
            }
        }
        return alignments;
    }

    private void logAlignment(RandomVariable<Alignment> alignment, int rep) throws IOException {
        String fileName = name + "_" + alignment.getId() + "_" + rep + ".nexus";
        PrintStream stream = new PrintStream(fileName);

        try {
            NexusWriter.write(alignment.value(), new LinkedList<>(),stream);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
