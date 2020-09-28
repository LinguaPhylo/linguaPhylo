package lphy.core;

import lphy.evolution.tree.TimeTree;
import lphy.graphicalModel.RandomVariable;
import lphy.graphicalModel.RandomValueLogger;
import lphy.graphicalModel.Value;
import lphy.nexus.NexusWriter;

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
                NexusWriter.write(null, treeList, new PrintStream(name + "_" + key + ".trees"));
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
            }
        }
        trees.sort(Comparator.comparing(Value::getId));
        return trees;
    }
}
