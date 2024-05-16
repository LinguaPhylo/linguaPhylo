package lphystudio.app.graphicalmodelpanel;

import lphy.core.model.Value;
import lphy.core.parser.LPhyParserDictionary;
import lphy.core.parser.graphicalmodel.GraphicalModelChangeListener;
import lphy.core.simulator.Sampler;
import lphy.core.simulator.SimulatorListener;

import java.util.List;
import java.util.Map;

/**
 * Sampler for graphical model, which notifies {@link GraphicalModelChangeListener}
 */
public class GraphicalModelSampler extends Sampler {

    GraphicalModelParserDictionary parserDictionary;

    public GraphicalModelSampler(GraphicalModelParserDictionary parserDictionary) {
        super();
        this.parserDictionary = parserDictionary;
    }

    @Override
    public List<Value> sample(Long seed) {
        List<Value> res = super.sample(seed);
        parserDictionary.notifyListeners();
        return res;
    }

    @Override
    public Map<Integer, List<Value>> sampleAll(int numReplicates,
                                               List<? extends SimulatorListener> loggers, Long seed) {
        Map<Integer, List<Value>> res = super.sampleAll(numReplicates, loggers, seed);
        parserDictionary.notifyListeners();
        return res;
    }

    @Override
    public LPhyParserDictionary getParserDictionary() {
        return this.parserDictionary;
    }
}
