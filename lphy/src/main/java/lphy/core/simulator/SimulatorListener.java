package lphy.core.simulator;

import lphy.core.model.Value;

import java.util.List;

/**
 * To handle side effects here,
 * e.g., add new methods in a child interface to export data containing GUI code.
 */
public interface SimulatorListener {

    //TODO share this with logger pkg
    int REPLICATES_START_INDEX = 0;

    void start(Object... configs);

    /**
     *
     * @param index   the index of each replicates of a simulation,
     *                which starts from 0.
     * @param values  the list of {@link Value} from one replicate of the simulation.
     */
    void replicate(int index, List<Value> values);

    void complete();
}
