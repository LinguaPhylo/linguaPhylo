package lphy.graphicalModel.types;

import beast.core.BEASTInterface;
import lphy2beast.BEASTContext;
import lphy.graphicalModel.Generator;
import lphy.graphicalModel.Value;

import java.util.Map;

public class WrappedDoubleValue extends Value<Double[]> {

    Value wrappedValue;

    public WrappedDoubleValue(Value<Double> value) {

        super(value.getId(), new Double[] {value.value()});
        wrappedValue = value;
    }

    public Generator getGenerator() {
        return wrappedValue.getGenerator();
    }

    class WrappedDoubleGenerator implements Generator<Double[]> {

        Generator wrappedGenerator;

        public WrappedDoubleGenerator(Generator g) {
            wrappedGenerator = g;
        }

        @Override
        public String getName() {
            return wrappedGenerator.getName();
        }

        @Override
        public Value<Double[]> generate() {
            return null;
        }

        @Override
        public Map<String, Value> getParams() {
            return null;
        }

        @Override
        public void setParam(String paramName, Value<?> value) {

        }

        @Override
        public String codeString() {
            return null;
        }

        @Override
        public String getUniqueId() {
            return null;
        }

        @Override
        public Object value() {
            return null;
        }

        public BEASTInterface toBEAST(BEASTInterface value, BEASTContext context) {
            throw new UnsupportedOperationException(getClass().getSimpleName() + ".toBEAST not implemented yet!");
        }
    }
}
