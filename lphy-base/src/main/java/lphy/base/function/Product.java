package lphy.base.function;

import lphy.base.ParameterNames;
import lphy.core.model.DeterministicFunction;
import lphy.core.model.Value;
import lphy.core.model.annotation.GeneratorInfo;
import lphy.core.model.datatype.NumberValue;
import lphy.core.parser.argument.ParameterInfo;

public class Product extends DeterministicFunction<Number> {

    public Product(@ParameterInfo(name = ParameterNames.ArrayParamName, description = "the array to product the elements of.")
                    Value<Number[]> x) {
        setParam(ParameterNames.ArrayParamName, x);
    }

    @GeneratorInfo(name = "product", description = "The product of the elements of the given array")
    public Value<Number> apply() {
        Number[] x = (Number[])getParams().get(ParameterNames.ArrayParamName).value();
        double product = 0.0;
        if (x.length > 0) {
            product = 1.0;
            for (Number number : x) {
                product *= number.doubleValue();
            }

            if (x[0] instanceof Integer) {
                return new NumberValue<>(null, (int) product, this);
            }
        }
        return new NumberValue<>(null, product, this);
    }

}
