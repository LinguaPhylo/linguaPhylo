package lphy.core.simulator;

import lphy.core.model.Value;
import lphy.core.parser.GraphicalLPhyParser;
import lphy.core.parser.LPhyMetaParser;
import lphy.core.parser.graphicalmodel.GraphicalModelChangeListener;

import java.util.List;
import java.util.Map;

/**
 * Sampler for graphical model, which notifies {@link GraphicalModelChangeListener}
 */
public class GraphicalSampler extends Sampler {

    GraphicalLPhyParser parser;

    public GraphicalSampler(GraphicalLPhyParser parser) {
        super();
        this.parser = parser;
    }

    @Override
    public List<Value> sample(Long seed) {
        parser.notifyListeners();
        return super.sample(seed);
    }

    @Override
    public Map<Integer, List<Value>> sampleAll(int numReplicates,
                                               List<? extends SimulatorListener> loggers, Long seed) {
        parser.notifyListeners();
        return super.sampleAll(numReplicates, loggers, seed);
    }

    @Override
    public LPhyMetaParser getParser() {
        return this.parser;
    }
}
