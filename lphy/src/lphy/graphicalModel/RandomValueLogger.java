package lphy.graphicalModel;

import java.util.List;

public interface RandomValueLogger {

    void log(int rep, List<Value<?>> randomValues);

    /**
     * Called once all replicates have been logged.
     */
    void close();

}