package lphy.core.commands;

import lphy.core.LPhyParser;
import lphy.graphicalModel.Command;
import lphy.graphicalModel.Value;

import java.util.Map;

public class Remove implements Command {

    LPhyParser parser;

    public Remove(LPhyParser parser) {
        this.parser = parser;
    }

    public String getName() { return "remove"; }

    public void execute(Map<String, Value<?>> params) {
        for (Value val: params.values()) {
            if (!val.isAnonymous()) {
                parser.getModelDictionary().remove(val.getId());
                parser.getDataDictionary().remove(val.getId());
            }
        }
    }
}
