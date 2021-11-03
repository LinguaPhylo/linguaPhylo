package lphy.core;

import lphy.evolution.tree.TimeTree;
import lphy.graphicalModel.RandomValueLogger;
import lphy.graphicalModel.Value;
import lphy.graphicalModel.VectorUtils;
import lphy.nexus.NexusWriter;
import lphy.util.Symbols;

import java.io.PrintStream;
import java.util.*;

/**
 * Created by adru001 on 10/03/20.
 */
public class TreeFileLogger implements RandomValueLogger {

    String name;

    Map<String, List<TimeTree>> trees;

    public TreeFileLogger(String name) {

        this.name = name;
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

    public void close() {
        trees.forEach((key, treeList) -> {
            try {
                NexusWriter.write(null, treeList,
                        new PrintStream(name + "_" + Symbols.getCanonical(key) + ".trees"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

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
