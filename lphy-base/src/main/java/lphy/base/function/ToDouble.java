package lphy.base.function;

import lphy.base.ParameterNames;
import lphy.core.model.DeterministicFunction;
import lphy.core.model.Value;
import lphy.core.model.annotation.GeneratorCategory;
import lphy.core.model.annotation.GeneratorInfo;
import lphy.core.model.annotation.ParameterInfo;
import lphy.core.model.datatype.DoubleValue;

/**
 * @author Walter Xie
 */
public class ToDouble extends DeterministicFunction<Double> {

    public ToDouble(@ParameterInfo(name = ParameterNames.NoParamName0, description = "given a string.") Value<String> str) {
        // When there is no arg name (e.g. "0"), it requires setInput (not setParam)
        setInput(ParameterNames.NoParamName0, str);
    }

    @GeneratorInfo(name = "strToDouble",
            category = GeneratorCategory.RATE_MATRIX, examples = {"readDelim.lphy"},
            description = "Cast string to double.")
    public Value<Double> apply() {
        String s = String.valueOf(getParams().get(ParameterNames.NoParamName0).value());
        double d = Double.parseDouble(s);
        return new DoubleValue(d, this);
    }
}
