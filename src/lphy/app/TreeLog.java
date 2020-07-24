package lphy.app;

import lphy.evolution.tree.TimeTree;
import lphy.graphicalModel.Loggable;
import lphy.graphicalModel.Value;

import java.util.HashMap;
import java.util.Map;

public class TreeLog extends VariableLog {

    public static Map<Class, Loggable> loggableMap = new HashMap<>();

    static {
        loggableMap.put(TimeTree.class, new Loggable<TimeTree>() {
            @Override
            public String[] getLogTitles(Value<TimeTree> value) {
                return new String[]{value.getId()};
            }

            public String[] getLogValues(Value<TimeTree> value) {
                return new String[]{value.value().toString()};
            }
        });
    }

    public TreeLog() {
        setLoggableMap(loggableMap);
    }
}
