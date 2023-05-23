package lphy.core;

import lphy.graphicalModel.Value;
import lphy.parser.SimulatorParsingException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A wrapper for any implementation of LPhyParser that will be used in the Studio.
 * @author Alexei Drummond
 */
public class GraphicalLPhyParser implements LPhyMetaParser {

    LPhyMetaParser wrappedParser;
    List<GraphicalModelChangeListener> listeners = new ArrayList<>();
    String name = null;

    public GraphicalLPhyParser(LPhyMetaParser parser) {
        wrappedParser = parser;
    }

    @Override
    public Map<String, Value<?>> getDataDictionary() {
        return wrappedParser.getDataDictionary();
    }

    @Override
    public Map<String, Value<?>> getModelDictionary() {
        return wrappedParser.getModelDictionary();
    }

    @Override
    public Set<Value> getDataValues() {
        return wrappedParser.getDataValues();
    }

    @Override
    public Set<Value> getModelValues() {
        return wrappedParser.getModelValues();
    }


    @Override
    public Value<?> getValue(String id, Context context) {
        return wrappedParser.getValue(id, context);
    }

    @Override
    public void parse(String code, Context context) throws SimulatorParsingException,IllegalArgumentException {
        wrappedParser.parse(code, context);
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
        return wrappedParser.getGeneratorClasses();
    }

    @Override
    public List<String> getLines() {
        return wrappedParser.getLines();
    }

    @Override
    public void clear() {
        wrappedParser.clear();
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
