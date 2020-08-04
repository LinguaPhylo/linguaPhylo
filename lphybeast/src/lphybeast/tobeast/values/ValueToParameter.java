package lphybeast.tobeast.values;

import beast.core.parameter.Parameter;
import lphy.graphicalModel.Value;

public class ValueToParameter {

    public static void setID(Parameter.Base parameter, Value value) {
        if (!value.isAnonymous()) parameter.setID(value.getCanonicalId());
    }
}
