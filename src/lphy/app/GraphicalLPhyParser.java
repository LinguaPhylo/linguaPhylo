package lphy.app;

import lphy.core.LPhyParser;
import lphy.graphicalModel.Command;
import lphy.graphicalModel.Value;

import java.util.*;

/**
 * A wrapper for any implementation of LPhyParser that will be used in the Studio.
 * @author Alexei Drummond
 */
public class GraphicalLPhyParser implements LPhyParser {

    LPhyParser wrappedParser;
    List<GraphicalModelChangeListener> listeners = new ArrayList<>();

    public GraphicalLPhyParser(LPhyParser parser) {
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
    public Value<?> getValue(String id, Context context) {
        return wrappedParser.getValue(id, context);
    }

    @Override
    public void addCommand(Command command) {
        wrappedParser.addCommand(command);
    }

    public Collection<Command> getCommands() {
        return wrappedParser.getCommands();
    }

    @Override
    public void parse(String code, Context context) {
        wrappedParser.parse(code, context);
        notifyListeners();
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
