package lphybeast.tobeast.values;

import beast.core.parameter.BooleanParameter;
import lphy.graphicalModel.Value;
import lphybeast.BEASTContext;
import lphybeast.ValueToBEAST;

import java.util.Arrays;
import java.util.List;

public class BooleanArrayValueToBEAST implements ValueToBEAST<Boolean[], BooleanParameter> {

    @Override
    public BooleanParameter valueToBEAST(Value<Boolean[]> value, BEASTContext context) {

        BooleanParameter parameter = new BooleanParameter();
        List<Boolean> values = Arrays.asList(value.value());
        parameter.setInputValue("value", values);
        parameter.setInputValue("dimension", values.size());

        parameter.initAndValidate();
        ValueToParameter.setID(parameter, value);
        return parameter;
    }

    @Override
    public Class getValueClass() {
        return Boolean[].class;
    }

    @Override
    public Class<BooleanParameter> getBEASTClass() {
        return BooleanParameter.class;
    }

}
