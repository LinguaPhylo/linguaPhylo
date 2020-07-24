package lphy.graphicalModel;

import beast.core.BEASTInterface;

import java.util.Map;

public abstract class DeterministicFunction<T> extends Func {

    public abstract Value<T> apply();

    public Value<T> generate() {
        return apply();
    }

    @Override
    public T value() {
    	return apply().value();
    }

    @Override
    public String getUniqueId() {
        return hashCode() + "";
    }

    public BEASTInterface toBEAST(BEASTInterface value, Map beastObjects) {
        throw new UnsupportedOperationException("MigrationCount.toBEAST not implemented yet!");
    }
}
