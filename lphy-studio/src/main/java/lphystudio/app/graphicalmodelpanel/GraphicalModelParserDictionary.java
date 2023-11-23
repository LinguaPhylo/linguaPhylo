package lphystudio.app.graphicalmodelpanel;

import lphy.core.exception.SimulatorParsingException;
import lphy.core.parser.REPL;
import lphy.core.parser.graphicalmodel.GraphicalModelChangeListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Extend {@link REPL} with adding {@link GraphicalModelChangeListener}.
 * @author Alexei Drummond
 */
public class GraphicalModelParserDictionary extends REPL {

    List<GraphicalModelChangeListener> listeners = new ArrayList<>();

    @Override
    public void parse(String code) throws SimulatorParsingException,IllegalArgumentException {
        super.parse(code);
        notifyListeners();
    }

    @Override
    public void clear() {
        super.clear();
        notifyListeners();
    }

    public void addGraphicalModelChangeListener(GraphicalModelChangeListener listener) {
        listeners.add(listener);
    }

    public void notifyListeners() {
        for (GraphicalModelChangeListener listener : listeners) {
            listener.modelChanged();
        }
    }
}
