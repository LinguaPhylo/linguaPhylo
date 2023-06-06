package lphy.base.function;

import lphy.core.model.DeterministicFunction;
import lphy.core.model.Value;
import lphy.core.model.annotation.GeneratorInfo;
import lphy.core.model.annotation.ParameterInfo;
import lphy.core.parser.graphicalmodel.ValueCreator;

public class Select extends DeterministicFunction<Number> {

    public static final String valueParamName = "x";
    public static final String indicatorParamName = "indicator";

    public Select(@ParameterInfo(name = valueParamName, description = "the value.") Value<Number> x,
                  @ParameterInfo(name = indicatorParamName, description = "indicator for whether the number should be selected, or replace with zero") Value<Boolean> indicator) {

        setParam(valueParamName, x);
        setParam(indicatorParamName, indicator);
    }

    @Override
    @GeneratorInfo(name = "select", description = "A function to select a value if the indicator is true, or return 0 otherwise.")
    public Value<Number> apply() {

        Value<Number> x = getX();
        Boolean indicator = getIndicator().value();

        return ValueCreator.createValue(indicator ? x.value() : 0.0, this);
    }

    public Value<Number> getX() {
        return (Value<Number>) paramMap.get(valueParamName);
    }

    public Value<Boolean> getIndicator() {
        return (Value<Boolean>) paramMap.get(indicatorParamName);
    }
}
