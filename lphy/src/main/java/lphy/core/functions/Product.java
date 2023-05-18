package lphy.core.functions;

import lphy.graphicalModel.DeterministicFunction;
import lphy.graphicalModel.GeneratorInfo;
import lphy.graphicalModel.ParameterInfo;
import lphy.graphicalModel.Value;
import lphy.graphicalModel.types.NumberValue;

import static lphy.core.ParameterNames.ArrayParamName;

public class Product extends DeterministicFunction<Number> {

    public Product(@ParameterInfo(name = ArrayParamName, description = "the array to product the elements of.")
                    Value<Number[]> x) {
        setParam(ArrayParamName, x);
    }

    @GeneratorInfo(name = "product", description = "The product of the elements of the given array")
    public Value<Number> apply() {
        Number[] x = (Number[])getParams().get(ArrayParamName).value();
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
