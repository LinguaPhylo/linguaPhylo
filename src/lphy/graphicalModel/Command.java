package lphy.graphicalModel;

import java.util.Map;

public interface Command {

    String getName();

    void execute(Map<String, Value> params);
}
