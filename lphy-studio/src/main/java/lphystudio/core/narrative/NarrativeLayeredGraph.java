package lphystudio.core.narrative;

import lphy.core.model.Narrative;
import lphy.core.parser.LPhyParserDictionary;
import lphystudio.core.layeredgraph.ProperLayeredGraph;

public interface NarrativeLayeredGraph extends Narrative {
    String graphicalModelBlock(LPhyParserDictionary parser, ProperLayeredGraph properLayeredGraph);

}
