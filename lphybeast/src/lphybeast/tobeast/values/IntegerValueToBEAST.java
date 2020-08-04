package lphybeast.tobeast.values;

import beast.core.BEASTInterface;
import beast.core.parameter.IntegerParameter;
import beast.core.parameter.RealParameter;
import lphy.core.distributions.Exp;
import lphy.core.distributions.LogNormal;
import lphy.graphicalModel.Value;
import lphybeast.BEASTContext;
import lphybeast.ValueToBEAST;

import java.util.Collections;

public class IntegerValueToBEAST implements ValueToBEAST<Integer> {

    @Override
    public BEASTInterface valueToBEAST(Value<Integer> value, BEASTContext context) {

        IntegerParameter parameter = new IntegerParameter();
        parameter.setInputValue("value", Collections.singletonList(value.value()));
        parameter.setInputValue("dimension", 1);
        parameter.initAndValidate();
        ValueToParameter.setID(parameter, value);
        return parameter;
    }

    public Class getValueClass() {
        return Integer.class;
    }
}
