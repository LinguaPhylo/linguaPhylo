package lphystudio.app.graphicalmodelpanel;

import lphy.core.exception.SimulatorParsingException;
import lphy.core.model.Value;
import lphy.core.parser.LPhyMetaData;
import lphy.core.parser.REPL;
import lphy.core.parser.graphicalmodel.GraphicalModelChangeListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A wrapper for any implementation of LPhyParser that will be used in the Studio,
 * the current difference with {@link REPL} is adding {@link GraphicalModelChangeListener}.
 * @author Alexei Drummond
 */
public class GraphicalModelContainer implements LPhyMetaData {

    protected LPhyMetaData wrappedMetaData;
    List<GraphicalModelChangeListener> listeners = new ArrayList<>();
    String name = null;

    public GraphicalModelContainer(LPhyMetaData metaData) {
        wrappedMetaData = metaData;
    }

    @Override
    public Map<String, Value<?>> getDataDictionary() {
        return wrappedMetaData.getDataDictionary();
    }

    @Override
    public Map<String, Value<?>> getModelDictionary() {
        return wrappedMetaData.getModelDictionary();
    }

    @Override
    public Set<Value> getDataValues() {
        return wrappedMetaData.getDataValues();
    }

    @Override
    public Set<Value> getModelValues() {
        return wrappedMetaData.getModelValues();
    }


    @Override
    public Value<?> getValue(String id, Context context) {
        return wrappedMetaData.getValue(id, context);
    }

    @Override
    public void parseScript(String code) throws SimulatorParsingException,IllegalArgumentException {
        wrappedMetaData.parseScript(code);
        notifyListeners();
    }

    @Override
    public void parseConsoleCMD(String consoleCMD, Context context) {
        wrappedMetaData.parseConsoleCMD(consoleCMD, context);
        notifyListeners();
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public Map<String, Set<Class<?>>> getGeneratorClasses() {
        return wrappedMetaData.getGeneratorClasses();
    }

    @Override
    public List<String> getLines() {
        return wrappedMetaData.getLines();
    }

    @Override
    public void clear() {
        wrappedMetaData.clear();
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
