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
    public void parseScript(String code) throws SimulatorParsingException,IllegalArgumentException {
        super.parseScript(code);
        notifyListeners();
    }

    @Override
    public Object parseConsoleCMD(String consoleCMD, Context context) {
        Object o = super.parseConsoleCMD(consoleCMD, context);
        notifyListeners();
        return o;
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
