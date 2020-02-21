package james.core.commands;

import james.graphicalModel.Command;
import james.graphicalModel.GraphicalModelParser;
import james.graphicalModel.Value;

import java.util.Map;

public class Remove implements Command {

    GraphicalModelParser parser;

    public Remove(GraphicalModelParser parser) {
        this.parser = parser;
    }

    public String getName() { return "remove"; }

    public void execute(Map<String, Value> params) {
        for (Value val: params.values()) {
            if (!val.isAnonymous()) parser.getDictionary().remove(val.getId());
        }
    }
}
