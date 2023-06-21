package lphy.base.logger;

import lphy.base.evolution.tree.TimeTree;
import lphy.base.parser.nexus.NexusWriter;
import lphy.core.logger.FileLogger;
import lphy.core.model.Symbols;
import lphy.core.model.Value;
import lphy.core.vectorization.VectorUtils;

import java.io.File;
import java.io.PrintStream;
import java.util.*;

/**
 * Created by Alexei Drummond on 10/03/20.
 */
public class TreeFileLogger implements FileLogger {
    File dir = null;
    String fileStem;

    Map<String, List<TimeTree>> trees;

//    public TreeFileLogger(String fileStem) {
//
//        this.fileStem = fileStem;
//    }
//
//    public TreeFileLogger(String fileStem, File dir) {
//        this.fileStem = fileStem;
//        this.dir = dir;
//    }

    @Override
    public void start(List<Value<?>> randomValues) {

    }

    public void log(int rep, List<Value<?>> values) {
        List<Value<TimeTree>> treeVariables = getTreeValues(values);

        if (rep == 0) {
            trees = new TreeMap<>();
            for (Value<TimeTree> tv : treeVariables) {
                trees.put(tv.getId(), new ArrayList<>());
            }
        }

        for (Value<TimeTree> v : treeVariables) {
            trees.get(v.getId()).add(v.value());
        }
    }

    public void stop() {
        trees.forEach((key, treeList) -> {
            String fileName = createFileName(fileStem, "_" + Symbols.getCanonical(key), ".trees");
            File file = getFile(fileName);
            try {
                NexusWriter.write(null, treeList,  new PrintStream(file));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
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


    private String fileName;

    private List<Value<TimeTree>> getTreeValues(List<Value<?>> variables) {
        List<Value<TimeTree>> trees = new ArrayList<>();
        for (Value v : variables) {
            if (v.value() instanceof TimeTree) {
                trees.add((Value<TimeTree>)v);
            } else if (v.value() instanceof TimeTree[]) {
                // VectorizedRandomVariable
                TimeTree[] value = (TimeTree[]) v.value();
                for (int i = 0; i < value.length; i++) {
                    TimeTree t = value[i];
                    trees.add(new Value<>(v.getId()+ VectorUtils.INDEX_SEPARATOR +i, t));
                }
            }
        }
        trees.sort(Comparator.comparing(Value::getId));
        return trees;
    }
}
