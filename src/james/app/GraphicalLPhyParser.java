package james.app;

import james.core.LPhyParser;
import james.graphicalModel.Value;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GraphicalLPhyParser implements LPhyParser {

    LPhyParser wrappedParser;
    List<GraphicalModelChangeListener> listeners = new ArrayList<>();

    public GraphicalLPhyParser(LPhyParser parser) {
        wrappedParser = parser;
    }

    @Override
    public Map<String, Value<?>> getDictionary() {
        return wrappedParser.getDictionary();
    }

    @Override
    public void parse(String code) {
        wrappedParser.parse(code);
        notifyListeners();
    }

    @Override
    public Map<String, Set<Class<?>>> getGenerativeDistributionClasses() {
        return wrappedParser.getGenerativeDistributionClasses();
    }

    @Override
    public List<String> getLines() {
        return wrappedParser.getLines();
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
