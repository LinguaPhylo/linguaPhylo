package lphystudio.core.log;

import lphy.base.evolution.tree.TimeTree;
import lphy.core.graphicalmodel.components.Value;
import lphy.core.logger.RandomValueLogger;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

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
