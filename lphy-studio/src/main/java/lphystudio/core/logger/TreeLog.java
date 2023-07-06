package lphystudio.core.logger;

import lphy.base.evolution.tree.TimeTree;
import lphy.core.logger.RandomValueFormatter;
import lphy.core.model.Value;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Log tree to GUI,
 */
public class TreeLog extends JTextArea implements RandomValueFormatter {

//    final TreeFileLogger treeFileLogger;

    public TreeLog() { }

    public void clear() {
        setText("");
    }

    List<Value<TimeTree>> treeVariables;


    @Override
    public void setSelectedItems(List<Value<?>> randomValues) {
        treeVariables = getTreeValues(randomValues);
    }

    @Override
    public List<?> getSelectedItems() {
        return null;
    }

    @Override
    public String getHeaderFromValues() {
        //TODO
        return "";
    }

    @Override
    public String getRowFromValues(int rowIndex) {
//        List<Value<TimeTree>> treeVariables = getTreeValues(randomValues);

        if (rowIndex == 0) {
            setText("sample");
            for (Value<TimeTree> tv : treeVariables) {
                append("\t" + tv.getId());
            }
            append("\n");
        }

        append(rowIndex +"");
        for (Value<TimeTree> v : treeVariables) {
            append("\t" + v.value().toNewick(false));
        }
        append("\n");
        return "";
    }

    @Override
    public String getFooterFromValues() {
        return "";
    }

    public String getFormatterDescription() {
        return getFormatterName() + " writes the trees generated from simulations into GUI.";
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
