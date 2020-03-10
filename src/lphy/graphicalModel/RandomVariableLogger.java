package lphy.graphicalModel;

import java.util.List;

public interface RandomVariableLogger {

    void log(int rep, List<RandomVariable<?>> variables);

    /**
     * Called once all replicates have been logged.
     */
    void close();
}