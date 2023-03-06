package lphy.graphicalModel.logger;

import lphy.evolution.alignment.SimpleAlignment;
import lphy.graphicalModel.RandomValueLogger;
import lphy.graphicalModel.Value;
import lphy.nexus.NexusWriter;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Alexei Drummond on 10/03/20.
 */
public class AlignmentFileLogger implements RandomValueLogger {

    File dir = null;
    String name;
//    List<FileWriter> fileWriter = new ArrayList<>();

    public AlignmentFileLogger(String name) {

        this.name = name;
    }

    public AlignmentFileLogger(String name, File dir) {
        this.name = name;
        this.dir = dir;
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
        //TODO inconsistent behaviour with TreeFileLogger, which write in close()
//        for (FileWriter writer : fileWriter) {
//            try {
//                writer.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
    }

//    private void openFiles(List<RandomVariable<TimeTree>> treeVariables) throws IOException {
//        for (RandomVariable<TimeTree> tree : treeVariables) {
//            String fileName = name + "_" + tree.getId() + ".trees";
//            fileWriter.add(new FileWriter(fileName));
//        }
//    }

    private List<Value<SimpleAlignment>> getAlignmentValues(List<Value<?>> values) {
        List<Value<SimpleAlignment>> alignments = new ArrayList<>();
        for (Value v : values) {
            if (v.value() instanceof SimpleAlignment) {
                alignments.add((Value<SimpleAlignment>)v);
            }
        }
        return alignments;
    }

    // if rep >=0, add postfix, e.g. _0.nexus
    private void logAlignment(Value<SimpleAlignment> alignment, int rep) throws IOException {
        String fileName = name + "_" + alignment.getId();
        fileName += rep >= 0 ? "_" + rep : "";
        fileName += ".nexus";

        File file;
        if (dir != null)
            file = new File(dir + File.separator + fileName);
        else file = new File(fileName);
        PrintStream stream = new PrintStream(file);

        try {
            NexusWriter.write(alignment.value(), new LinkedList<>(),stream);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
