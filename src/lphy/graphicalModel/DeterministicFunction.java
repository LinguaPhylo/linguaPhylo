package lphy.graphicalModel;

import beast.core.BEASTInterface;
import lphy2beast.BEASTContext;

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

    public BEASTInterface toBEAST(BEASTInterface value, BEASTContext context) {
        throw new UnsupportedOperationException("not implemented yet!");
    }
}
