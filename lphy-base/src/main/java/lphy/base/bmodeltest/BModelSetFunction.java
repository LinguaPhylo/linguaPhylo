package lphy.base.bmodeltest;

import lphy.core.model.DeterministicFunction;
import lphy.core.model.Value;
import lphy.core.model.annotation.GeneratorCategory;
import lphy.core.model.annotation.GeneratorInfo;
import lphy.core.model.annotation.ParameterInfo;

public class BModelSetFunction extends DeterministicFunction<BModelSet> {

    public static final String paramName = "0";

    public BModelSetFunction(@ParameterInfo(name = paramName, narrativeName = "name",
            description = "the bModelTest model set name. " +
                    "The option includes: allreversible, transitionTransversionSplit, namedSimple, namedExtended.")
                             Value<String> modelSetName) {
        setParam(paramName, modelSetName);
    }

    @GeneratorInfo(name = "bModelSet", verbClause = "is",
            category = GeneratorCategory.MODEL_AVE_SEL, examples = {"simpleBModelTest.lphy"},
            description = "Returns the set of models for the given bModelTest model set name.")
    public Value<BModelSet> apply() {
        Value<String> v = (Value<String>) getParams().get(paramName);

        BModelSet.ModelSet modelSet;
        modelSet = BModelSet.ModelSet.valueOf(v.value());

        return new Value(new BModelSet(modelSet), this);
    }
}
