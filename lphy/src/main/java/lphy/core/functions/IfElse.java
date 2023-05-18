package lphy.core.functions;

import lphy.graphicalModel.DeterministicFunction;
import lphy.graphicalModel.GeneratorInfo;
import lphy.graphicalModel.ParameterInfo;
import lphy.graphicalModel.Value;

import static lphy.core.ParameterNames.*;

public class IfElse<T> extends DeterministicFunction<T> {

//    public static final String CONDITION = "cond";
//    public static final String IF_TRUE = "true";
//    public static final String ELSE = "false";

    public IfElse(@ParameterInfo(name = NoParamName0, description = "the logical condition to determine which value to return")
               Value<Boolean> logicVal,
               @ParameterInfo(name = NoParamName1, description = "the value to return if the condition is true")
               Value<T> trueVal,
               @ParameterInfo(name = NoParamName2, description = "the value to return if the condition is false")
               Value<T> falseVal) {
        setInput(NoParamName0, logicVal);
        setInput(NoParamName1, trueVal);
        setInput(NoParamName2, falseVal);
    }

    @GeneratorInfo(name="ifelse", verbClause = "checks",
            description = "Return the 1st value if the condition is true, else return the 2nd value.")
    public Value<T> apply() {
        Value<Boolean> booleanValue = (Value<Boolean>)getParams().get(NoParamName0);

        if (booleanValue.value()) {
            Value<T> value1 = (Value<T>)getParams().get(NoParamName1);
            return new Value<>(value1.getId(), value1.value(), this);
        } else {
            Value<T> value2 = (Value<T>)getParams().get(NoParamName2);
            return new Value<>(value2.getId(), value2.value(), this);
        }
    }


}
