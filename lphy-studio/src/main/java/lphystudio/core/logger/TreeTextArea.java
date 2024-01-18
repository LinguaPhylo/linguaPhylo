package lphystudio.core.logger;

import lphy.base.evolution.tree.TimeTree;
import lphy.core.logger.ValueFormatResolver;
import lphy.core.logger.ValueFormatter;
import lphy.core.model.Value;
import lphy.core.simulator.SimulatorListener;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Log tree to GUI,
 */
public class TreeTextArea extends JTextArea implements SimulatorListener {

    int numReplicates = 1000;

    public TreeTextArea() { }

    public void clear() {
        setText("");
    }

//    List<Value<TimeTree>> treeVariables;

    @Override
    public void start(Object... configs) {
        for (int i = 0; i < configs.length; i++) {
            if (configs[0] instanceof Integer numReplicates)
                this.numReplicates = numReplicates;
        }
    }

    @Override
    public void replicate(int index, List<Value> values) {
        List<Value> treeValuePerRep = getTreeValues(values);

        if (index == 0) {
            setText("sample");
            for (Value treV : treeValuePerRep) {
                List<ValueFormatter> valueFormatterList = ValueFormatResolver
                        .createFormatter(TreeTextFormatter.class, treV);
                for (ValueFormatter valueFormatter : valueFormatterList) {
                    append("\t" + valueFormatter.header());
                }
            }
            append("\n");
        }

        append(index+"");
        for (Value treV : treeValuePerRep) {
            List<ValueFormatter> valueFormatterList = ValueFormatResolver
                    .createFormatter(TreeTextFormatter.class, treV);
            for (ValueFormatter valueFormatter : valueFormatterList) {
                append("\t" + valueFormatter.format(treV.value()));
            }
        }
        append("\n");
    }

    @Override
    public void complete() {

    }

    // can be TimeTree or SimpleATimeTreelignment[]
    private List<Value> getTreeValues(List<Value> variables) {
        List<Value> values = new ArrayList<>();
        for (Value v : variables) {
            if (v.value() instanceof TimeTree || v.value() instanceof TimeTree[]) {
                values.add(v);
            }
        }
        values.sort(Comparator.comparing(Value::getId));
        return values;
    }

}
