package lphy.base.function;

import lphy.base.ParameterNames;
import lphy.core.model.DeterministicFunction;
import lphy.core.model.Value;
import lphy.core.model.annotation.GeneratorInfo;
import lphy.core.parser.argument.ParameterInfo;

public class IfElse<T> extends DeterministicFunction<T> {

//    public static final String CONDITION = "cond";
//    public static final String IF_TRUE = "true";
//    public static final String ELSE = "false";

    public IfElse(@ParameterInfo(name = ParameterNames.NoParamName0, description = "the logical condition to determine which value to return")
               Value<Boolean> logicVal,
               @ParameterInfo(name = ParameterNames.NoParamName1, description = "the value to return if the condition is true")
               Value<T> trueVal,
               @ParameterInfo(name = ParameterNames.NoParamName2, description = "the value to return if the condition is false")
               Value<T> falseVal) {
        setInput(ParameterNames.NoParamName0, logicVal);
        setInput(ParameterNames.NoParamName1, trueVal);
        setInput(ParameterNames.NoParamName2, falseVal);
    }

    @GeneratorInfo(name="ifelse", verbClause = "checks",
            description = "Return the 1st value if the condition is true, else return the 2nd value.")
    public Value<T> apply() {
        Value<Boolean> booleanValue = (Value<Boolean>)getParams().get(ParameterNames.NoParamName0);

        if (booleanValue.value()) {
            Value<T> value1 = (Value<T>)getParams().get(ParameterNames.NoParamName1);
            return new Value<>(value1.getId(), value1.value(), this);
        } else {
            Value<T> value2 = (Value<T>)getParams().get(ParameterNames.NoParamName2);
            return new Value<>(value2.getId(), value2.value(), this);
        }
    }


}
