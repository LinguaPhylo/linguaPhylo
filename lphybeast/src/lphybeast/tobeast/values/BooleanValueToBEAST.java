package lphybeast.tobeast.values;

import beast.core.parameter.BooleanParameter;
import lphy.graphicalModel.Value;
import lphybeast.BEASTContext;
import lphybeast.ValueToBEAST;

public class BooleanValueToBEAST implements ValueToBEAST<Boolean, BooleanParameter> {

    @Override
    public BooleanParameter valueToBEAST(Value<Boolean> value, BEASTContext context) {

        BooleanParameter parameter = new BooleanParameter();
        parameter.setInputValue("value", value.value());
        parameter.setInputValue("dimension", 1);

        parameter.initAndValidate();
        ValueToParameter.setID(parameter, value);
        return parameter;
    }

    @Override
    public Class getValueClass() {
        return Boolean.class;
    }

    @Override
    public Class<BooleanParameter> getBEASTClass() {
        return BooleanParameter.class;
    }
}
