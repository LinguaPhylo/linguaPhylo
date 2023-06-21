package lphy.core.logger;

import lphy.core.model.Value;

import java.util.List;

/**
 * The logger interface has no side effect.
 */
public interface RandomValueLogger {

    /**
     * Called once for all replicates, e.g., build the header.
     */
    void start(List<Value<?>> randomValues);

    /**
     * Build the string of contents should be logged per replicate.
     * @param rep           the index of a replicate
     * @param randomValues  all available {@link Value}
     */
    void log(int rep, List<Value<?>> randomValues);

    /**
     * Called once, all replicates have been logged.
     */
    void stop();

}