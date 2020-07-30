package lphy.core;

import lphy.evolution.tree.TimeTree;
import lphy.graphicalModel.RandomVariable;
import lphy.graphicalModel.RandomVariableLogger;
import lphy.nexus.NexusWriter;

import java.io.PrintStream;
import java.util.*;

/**
 * Created by adru001 on 10/03/20.
 */
public class TreeFileLogger implements RandomVariableLogger {

    String name;

    Map<String, List<TimeTree>> trees;

    public TreeFileLogger(String name) {

        this.name = name;
    }

    public void log(int rep, List<RandomVariable<?>> variables) {
        List<RandomVariable<TimeTree>> treeVariables = getTreeVariables(variables);

        if (rep == 0) {
            trees = new TreeMap<>();
            for (RandomVariable<TimeTree> tv : treeVariables) {
                trees.put(tv.getId(), new ArrayList<TimeTree>());
            }
        }

        for (RandomVariable<TimeTree> v : treeVariables) {
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

    private List<RandomVariable<TimeTree>> getTreeVariables(List<RandomVariable<?>> variables) {
        List<RandomVariable<TimeTree>> trees = new ArrayList<>();
        for (RandomVariable v : variables) {
            if (v.value() instanceof TimeTree) {
                trees.add((RandomVariable<TimeTree>)v);
            }
        }
        trees.sort((o1, o2) -> o1.getId().compareTo(o2.getId()));
        return trees;
    }
}
