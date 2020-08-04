package lphybeast.tobeast.values;

import beast.core.BEASTInterface;
import beast.core.parameter.RealParameter;
import lphy.core.distributions.Dirichlet;
import lphy.core.distributions.Exp;
import lphy.core.distributions.LogNormal;
import lphy.graphicalModel.Value;
import lphybeast.BEASTContext;
import lphybeast.ValueToBEAST;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DoubleArray2DValueToBEAST implements ValueToBEAST<Double[][]> {

    @Override
    public BEASTInterface valueToBEAST(Value<Double[][]> value, BEASTContext context) {

        RealParameter parameter = new RealParameter();

        Double[][] val = value.value();

        List<Double> values = new ArrayList<>(val.length * val[0].length);
        for (Double[] doubles : val) {
            values.addAll(Arrays.asList(doubles));
        }
        parameter.setInputValue("value", values);
        parameter.setInputValue("dimension", values.size());
        parameter.setInputValue("minordimension", val[0].length); // TODO check this!
        parameter.initAndValidate();
        ValueToParameter.setID(parameter, value);
        return parameter;
    }

    public Class getValueClass() {
        return Double[][].class;
    }
}
