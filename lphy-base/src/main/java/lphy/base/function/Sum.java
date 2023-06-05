package lphy.base.function;

import lphy.base.ParameterNames;
import lphy.core.model.annotation.GeneratorInfo;
import lphy.core.model.annotation.ParameterInfo;
import lphy.core.model.component.DeterministicFunction;
import lphy.core.model.component.Value;
import lphy.core.model.datatype.NumberValue;

public class Sum extends DeterministicFunction<Number> {

    public Sum(@ParameterInfo(name = ParameterNames.ArrayParamName, description = "the array to sum the elements of.")
                    Value<Number[]> x) {
        setParam(ParameterNames.ArrayParamName, x);
    }

    @GeneratorInfo(name = "sum", description = "The sum of the elements of the given array")
    public Value<Number> apply() {
        Number[] x = (Number[])getParams().get(ParameterNames.ArrayParamName).value();

        double sum = 0.0;
        for (int i = 0; i < x.length; i++ ) {
            sum += x[i].doubleValue();
        }

        if(x.length > 0 && x[0] instanceof Integer) {
            return new NumberValue<>(null, (int) sum, this);
        }
        return new NumberValue<>(null, sum, this);
    }

}
