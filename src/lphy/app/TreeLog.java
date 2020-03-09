package lphy.app;

import lphy.TimeTree;
import lphy.graphicalModel.Loggable;
import lphy.graphicalModel.Value;

public class TreeLog extends Log {


    public TreeLog() {

        loggableMap.clear();

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
}
