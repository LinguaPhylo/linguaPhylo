package lphybeast.tobeast.values;

import beast.core.BEASTInterface;
import beast.core.parameter.IntegerParameter;
import lphy.core.distributions.RandomComposition;
import lphy.graphicalModel.Value;
import lphybeast.BEASTContext;
import lphybeast.ValueToBEAST;

import java.util.Arrays;
import java.util.List;

public class IntegerArrayValueToBEAST implements ValueToBEAST<Integer[]> {

    @Override
    public BEASTInterface valueToBEAST(Value<Integer[]> value, BEASTContext context) {

        IntegerParameter parameter = new IntegerParameter();
        List<Integer> values = Arrays.asList(value.value());
        parameter.setInputValue("value", values);
        parameter.setInputValue("dimension", values.size());

        // check domain
        if (value.getGenerator() instanceof RandomComposition) {
            parameter.setInputValue("lower", 1);
        }

        parameter.initAndValidate();
        ValueToParameter.setID(parameter, value);
        return parameter;
    }

    @Override
    public Class getValueClass() {
        return Integer[].class;
    }

    @Override
    public Class<IntegerParameter> getBEASTClass() {
        return IntegerParameter.class;
    }

}
