package lphy.core.functions;

import lphy.evolution.TaxaAges;
import lphy.evolution.alignment.AbstractAlignment;
import lphy.graphicalModel.DeterministicFunction;
import lphy.graphicalModel.GeneratorInfo;
import lphy.graphicalModel.ParameterInfo;
import lphy.graphicalModel.Value;

public class TaxaAgesFromFunction extends DeterministicFunction<TaxaAges> {

    final String alignmentParamName;

    public TaxaAgesFromFunction(@ParameterInfo(name = "alignment", description = "the alignment") Value<AbstractAlignment> alignment) {
        alignmentParamName = getParamName(0);
        setParam(alignmentParamName, alignment);
    }

    @GeneratorInfo(name="taxaAgesFrom", description = "The taxa ages map from alignment.")
    public Value<TaxaAges> apply() {
        Value<AbstractAlignment> alignment = (Value<AbstractAlignment>) getParams().get(alignmentParamName);

        return new Value<>(null, alignment.value(), this);
    }
}
