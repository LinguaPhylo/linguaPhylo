package james.app;

import james.TimeTree;
import james.graphicalModel.Loggable;
import james.graphicalModel.RandomVariable;
import james.graphicalModel.Value;

import javax.swing.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
