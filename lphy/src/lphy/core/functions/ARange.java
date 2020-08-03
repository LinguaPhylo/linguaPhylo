package lphy.core.functions;

import lphy.graphicalModel.DeterministicFunction;
import lphy.graphicalModel.GeneratorInfo;
import lphy.graphicalModel.ParameterInfo;
import lphy.graphicalModel.Value;

public class ARange extends DeterministicFunction<Double[]> {

    String startParamName;
    String stopParamName;
    String stepParamName;

    public ARange(@ParameterInfo(name="start", description ="value of the range to start at (inclusive)") Value<Double> start,
                  @ParameterInfo(name="stop", description ="value of the range to stop at (inclusive)") Value<Double> stop,
                  @ParameterInfo(name="step", description ="the step size") Value<Double> step) {

        startParamName = getParamName(0);
        stopParamName = getParamName(1);
        stepParamName = getParamName(2);
        setParam(startParamName, start);
        setParam(stopParamName, stop);
        setParam(startParamName, step);
    }

    @Override
    @GeneratorInfo(name = "arange", description = "A function to produce an equally space array of doubles. Takes a start value, and stop value and a step value and returns a double array value.")
    public Value<Double[]> apply() {

        double s = start().value();
        double e = stop().value();
        double t = step().value();

        int len = (int)Math.floor((e-s)/t)+1;

        Double[] sequence = new Double[len];

        int c = 0;
        for (double i = s; i <= e; i += t) {
            sequence[c] = i;
            c += 1;
        }
        return new Value<>(null, sequence, this);
    }

    public Value<Double> start() {
        return (Value<Double>)paramMap.get(startParamName);
    }
    public Value<Double> stop() {
        return (Value<Double>) paramMap.get(stopParamName);
    }
    public Value<Double> step() {
        return (Value<Double>) paramMap.get(stepParamName);
    }
}
