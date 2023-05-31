package lphystudio.core.narrative;

import lphy.core.narrative.Narrative;
import lphy.core.parser.LPhyMetaParser;
import lphystudio.core.layeredgraph.ProperLayeredGraph;

public interface NarrativeLayeredGraph extends Narrative {
    String graphicalModelBlock(LPhyMetaParser parser, ProperLayeredGraph properLayeredGraph);

}
