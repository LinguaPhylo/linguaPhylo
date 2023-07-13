package lphystudio.core.logger;

import lphy.base.evolution.tree.TimeTree;
import lphy.core.model.Value;
import lphy.core.simulator.SimulatorListener;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Log tree to GUI,
 */
public class TreeLog extends JTextArea implements SimulatorListener {


    public TreeLog() { }

    public void clear() {
        setText("");
    }

//    List<Value<TimeTree>> treeVariables;

    @Override
    public void start(List<Object> configs) {

    }

    @Override
    public void replicate(int index, List<Value> values) {
        List<Value<TimeTree>> treeVariables = getTreeValues(values);

        if (index == 0) {
            setText("sample");
            for (Value<TimeTree> tv : treeVariables) {
                append("\t" + tv.getId());
            }
            append("\n");
        }

        append(index+"");
        for (Value<TimeTree> v : treeVariables) {
            append("\t" + v.value().toNewick(false));
        }
        append("\n");
    }

    @Override
    public void complete() {

    }

    private List<Value<TimeTree>> getTreeValues(List<Value> variables) {
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
