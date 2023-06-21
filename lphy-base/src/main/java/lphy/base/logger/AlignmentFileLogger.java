package lphy.base.logger;

import lphy.base.evolution.alignment.SimpleAlignment;
import lphy.base.parser.nexus.NexusWriter;
import lphy.core.logger.FileLogger;
import lphy.core.model.Value;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Alexei Drummond on 10/03/20.
 */
public class AlignmentFileLogger implements FileLogger {

    File dir = null;
    String fileStem;

//    public AlignmentFileLogger(String fileStem) {
//
//        this.fileStem = fileStem;
//    }
//
//    public AlignmentFileLogger(String fileStem, File dir) {
//        this.fileStem = fileStem;
//        this.dir = dir;
//    }

    @Override
    public void start(List<Value<?>> randomValues) {
    }

    @Override
    public void log(int rep, List<Value<?>> values) {

        for (Value<SimpleAlignment> v : getAlignmentValues(values)) {
            try {
                writeAlignment(rep, v);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public void stop() {
    }

//    private void openFiles(List<RandomVariable<TimeTree>> treeVariables) throws IOException {
//        for (RandomVariable<TimeTree> tree : treeVariables) {
//            String fileName = name + "_" + tree.getId() + ".trees";
//            fileWriter.add(new FileWriter(fileName));
//        }
//    }

    public List<Value<SimpleAlignment>> getAlignmentValues(List<Value<?>> values) {
        List<Value<SimpleAlignment>> alignments = new ArrayList<>();
        for (Value v : values) {
            if (v.value() instanceof SimpleAlignment) {
                alignments.add((Value<SimpleAlignment>)v);
            }
        }
        return alignments;
    }

    @Override
    public File getFile(String fileName) {
        File file;
        if (dir != null)
            file = new File(dir + File.separator + fileName);
        else file = new File(fileName);
        return file;
    }

    @Override
    public void setDir(File dir) {
        this.dir = dir;
    }


    @Override
    public void setFileStem(String fileStem) {
        this.fileStem = fileStem;
    }


    // if rep >=0, add postfix, e.g. _0.nexus
    private void writeAlignment(int rep, Value<SimpleAlignment> alignment) throws IOException {
        String postfix = "_" + alignment.getId() + (rep >= 0 ? "_" + rep : "");
        String fileName = createFileName(fileStem, postfix, ".nexus");

        File file = getFile(fileName);
        PrintStream stream = new PrintStream(file);

        try {
            NexusWriter.write(alignment.value(), new LinkedList<>(),stream);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
