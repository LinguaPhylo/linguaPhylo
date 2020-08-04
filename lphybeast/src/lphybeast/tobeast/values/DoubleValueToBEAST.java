package lphybeast.tobeast.values;

import beast.core.BEASTInterface;
import beast.core.parameter.RealParameter;
import lphy.core.distributions.Exp;
import lphy.core.distributions.LogNormal;
import lphy.graphicalModel.Value;
import lphybeast.BEASTContext;
import lphybeast.ValueToBEAST;

import java.util.*;

public class DoubleValueToBEAST implements ValueToBEAST<Double> {

    @Override
    public BEASTInterface valueToBEAST(Value<Double> value, BEASTContext context) {

        RealParameter parameter = new RealParameter();
            parameter.setInputValue("value", Collections.singletonList(value.value()));
            parameter.setInputValue("dimension", 1);

            // check domain
            if (value.getGenerator() instanceof LogNormal || value.getGenerator() instanceof Exp) {
                parameter.setInputValue("lower", 0.0);
            }
            parameter.initAndValidate();
        ValueToParameter.setID(parameter, value);
            return parameter;
    }

    public Class getValueClass() {
        return Double.class;
    }
}
