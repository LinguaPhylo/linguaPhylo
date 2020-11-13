package lphy.app;

import lphy.evolution.tree.TimeTree;
import lphy.graphicalModel.RandomValueLogger;
import lphy.graphicalModel.Value;

import javax.swing.*;
import java.util.*;

/**
 * Created by adru001 on 10/03/20.
 */
public class TreeLog extends JTextArea implements RandomValueLogger {

    public TreeLog() { }

    public void clear() {
        setText("");
    }

    public void log(int rep, List<Value<?>> values) {
        List<Value<TimeTree>> treeVariables = getTreeValues(values);

        if (rep == 0) {
            setText("sample");
            for (Value<TimeTree> tv : treeVariables) {
                append("\t" + tv.getId());
            }
            append("\n");
        }

        append(rep+"");
        for (Value<TimeTree> v : treeVariables) {
            append("\t" + v.value().toNewick(false));
        }
        append("\n");
    }

    public void close() {
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
