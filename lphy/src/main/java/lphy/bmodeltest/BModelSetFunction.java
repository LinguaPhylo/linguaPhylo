package lphy.bmodeltest;

import lphy.graphicalModel.*;

public class BModelSetFunction extends DeterministicFunction<BModelSet> {

    public static final String paramName = "0";

    public BModelSetFunction(@ParameterInfo(name = paramName, narrativeName = "name", description = "the bModelTest model set name.") Value<String> modelSetName) {
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
