package lphybeast.tobeast.values;

import beast.core.BEASTInterface;
import beast.core.parameter.IntegerParameter;
import beast.core.parameter.RealParameter;
import lphy.core.distributions.Dirichlet;
import lphy.core.distributions.Exp;
import lphy.core.distributions.LogNormal;
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
        parameter.initAndValidate();
        ValueToParameter.setID(parameter, value);
        return parameter;
    }

    public Class getValueClass() {
        return Integer[].class;
    }
}
