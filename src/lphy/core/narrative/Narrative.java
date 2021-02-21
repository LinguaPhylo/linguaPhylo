package lphy.core.narrative;

import lphy.app.graphicalmodelcomponent.GraphicalModelComponent;
import lphy.core.LPhyParser;
import lphy.graphicalModel.Citation;
import lphy.graphicalModel.Value;
import lphy.graphicalModel.ValueUtils;

import static lphy.graphicalModel.NarrativeUtils.*;

public interface Narrative {

    /**
     * This initiates the document, including clearing references and any other initialisation of internal state that is necessary.
     * @return the initial document lines.
     */
    String beginDocument();

    /**
     * This ends document constructions, including any other finalization of internal state that is necessary.
     * @return the closing lines of the document.
     */
    String endDocument();

    /**
     * @param header the heading of the section
     * @return a string representing the start of a new section
     */
    String section(String header);

    String text(String text);

    String cite(Citation citation);

    void clearReferences();

    String referenceSection();

    String getId(Value value, boolean inlineMath);

    default String symbol(String symbol) {
        return symbol;
    }

    String startMathMode(boolean inline);

    String endMathMode();

    String codeBlock(LPhyParser parser);

    String graphicalModelBlock(GraphicalModelComponent component);
}
