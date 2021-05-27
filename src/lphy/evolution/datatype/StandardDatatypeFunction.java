package lphy.evolution.datatype;

import jebl.evolution.sequences.SequenceType;
import lphy.evolution.sequences.Standard;
import lphy.graphicalModel.*;

/**
 * Created by adru001 on 2/02/20.
 */
public class StandardDatatypeFunction extends DeterministicFunction<SequenceType> {

    public static final String numStatesParamName = "numStates";

    public StandardDatatypeFunction(@ParameterInfo(name = numStatesParamName, description = "the number of distinct states in this data type.") Value<Integer> stateCount) {

        setParam(numStatesParamName, stateCount);
    }

    @GeneratorInfo(name = "standard",
            verbClause = "is",
            narrativeName = "the Standard data type",
            description = "The Standard data type function. Takes a state count and produces a Standard data type with that number of states.")
    public Value<SequenceType> apply() {
        Value<Integer> stateCountValue = getParams().get(numStatesParamName);
        int stateCount = stateCountValue.value();
        return new Value<>(null, new Standard(stateCount), this);
    }
}
