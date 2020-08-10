package lphybeast.tobeast.values;

import beast.core.parameter.RealParameter;
import lphy.graphicalModel.GenerativeDistribution1D;
import lphy.graphicalModel.Value;
import lphybeast.BEASTContext;
import lphybeast.ValueToBEAST;

import java.util.Collections;

public class DoubleValueToBEAST implements ValueToBEAST<Double, RealParameter> {

    @Override
    public RealParameter valueToBEAST(Value<Double> value, BEASTContext context) {

        RealParameter parameter = new RealParameter();
        parameter.setInputValue("value", Collections.singletonList(value.value()));
        parameter.setInputValue("dimension", 1);

        // check domain
        if (value.getGenerator() instanceof GenerativeDistribution1D) {
            GenerativeDistribution1D<Double> gd = (GenerativeDistribution1D<Double>)value.getGenerator();

            Double[] bounds = gd.getDomainBounds();

            if (bounds[0] != Double.NEGATIVE_INFINITY) parameter.setInputValue("lower", bounds[0]);
            if (bounds[1] != Double.POSITIVE_INFINITY) parameter.setInputValue("upper", bounds[1]);
        }
        parameter.initAndValidate();
        ValueToParameter.setID(parameter, value);
        return parameter;
    }

    @Override
    public Class getValueClass() {
        return Double.class;
    }

    @Override
    public Class<RealParameter> getBEASTClass() {
        return RealParameter.class;
    }

}
