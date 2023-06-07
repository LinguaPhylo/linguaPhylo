package lphy.core.model;

import lphy.core.model.annotation.Citation;
import lphy.core.parser.LPhyMetaParser;

import java.util.prefs.Preferences;

public interface Narrative {

    /**
     * This initiates the document, including clearing references and any other initialisation of internal state that is necessary.
     *
     * @return the initial document lines.
     */
    String beginDocument(String title);

    /**
     * This ends document constructions, including any other finalization of internal state that is necessary.
     *
     * @return the closing lines of the document.
     */
    String endDocument();

    Preferences getPreferences();

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

    String startMathMode(boolean inline, boolean allowMultiline);

    String mathAlign();
    
    String mathNewLine();

    String endMathMode();

    String codeBlock(LPhyMetaParser parser, int fontSize);

    String posterior(LPhyMetaParser parser);

    String product(String index, String start, String end);

    String subscript(String index);
}
