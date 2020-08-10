package lphybeast.tobeast.values;

import beast.core.parameter.IntegerParameter;
import lphy.graphicalModel.GenerativeDistribution1D;
import lphy.graphicalModel.Value;
import lphybeast.BEASTContext;
import lphybeast.ValueToBEAST;

import java.util.Collections;

public class IntegerValueToBEAST implements ValueToBEAST<Integer, IntegerParameter> {

    @Override
    public IntegerParameter valueToBEAST(Value<Integer> value, BEASTContext context) {

        IntegerParameter parameter = new IntegerParameter();
        parameter.setInputValue("value", Collections.singletonList(value.value()));
        parameter.setInputValue("dimension", 1);

        // check domain
        if (value.getGenerator() instanceof GenerativeDistribution1D) {
            GenerativeDistribution1D<Integer> gd = (GenerativeDistribution1D<Integer>)value.getGenerator();

            Integer[] bounds = gd.getDomainBounds();

            if (bounds[0] > Integer.MIN_VALUE) parameter.setInputValue("lower", bounds[0]);
            if (bounds[1] < Integer.MAX_VALUE) parameter.setInputValue("upper", bounds[1]);
        }

        parameter.initAndValidate();
        ValueToParameter.setID(parameter, value);
        return parameter;
    }

    @Override
    public Class getValueClass() {
        return Integer.class;
    }

    @Override
    public Class<IntegerParameter> getBEASTClass() {
        return IntegerParameter.class;
    }

}
