package lphystudio.core.narrative;

import lphy.core.model.Narrative;
import lphy.core.parser.LPhyMetaData;
import lphystudio.core.layeredgraph.ProperLayeredGraph;

public interface NarrativeLayeredGraph extends Narrative {
    String graphicalModelBlock(LPhyMetaData parser, ProperLayeredGraph properLayeredGraph);

}
