package lphy.core.simulator;

import lphy.core.logger.RandomValueFormatter;
import lphy.core.parser.GraphicalLPhyParser;
import lphy.core.parser.LPhyMetaParser;
import lphy.core.parser.graphicalmodel.GraphicalModelChangeListener;

import java.util.List;

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
    public void sample(int numReplicates, List<? extends RandomValueFormatter> loggers) {
        super.sample(numReplicates, loggers);
        parser.notifyListeners();
    }

    @Override
    public LPhyMetaParser getParser() {
        return this.parser;
    }
}
