package lphy.core;

import lphy.evolution.alignment.SimpleAlignment;
import lphy.graphicalModel.*;
import lphy.graphicalModel.logger.AlignmentFileLogger;
import lphy.util.LoggerUtils;

import java.util.*;

public class Sampler {

    GraphicalLPhyParser parser;

    Map<Integer, List<Value<?>>> valuesMap;

    public Sampler(GraphicalLPhyParser parser) {
        this.parser = parser;
        this.valuesMap = new TreeMap<>();
        this.valuesMap.clear();
    }

    /**
     * Sample the current model
     *
     * @param reps    the number of times to sample
     * @param loggers the loggers to log to
     */
    public void sample(int reps, List<RandomValueLogger> loggers) {
        for (int i = 0; i < reps; i++) {
            Set<String> sampled = new TreeSet<>();
            List<Value<?>> sinks = parser.getModelSinks();
            for (RandomVariable<?> var : parser.getAllVariablesFromSinks()) {
                parser.getModelDictionary().remove(var.getId());
            }

            for (Value<?> value : sinks) {
                if (value.isRandom()) {
                    Value randomValue;
                    if (value.getGenerator() != null) {
                        randomValue = sampleAll(value, value.getGenerator(), sampled);
                    } else throw new RuntimeException();
                    randomValue.setId(value.getId());

                    addValueToModelDictionary(randomValue);
                }
            }

            if (loggers != null) {
                List<Value<?>> values = GraphicalModel.Utils.getAllValuesFromSinks(parser);
                valuesMap.put(i, values);
                for (RandomValueLogger logger : loggers) {
                    logger.log(i, values);
                }
            }
        }
        if (loggers != null) {
            for (RandomValueLogger logger : loggers) {
                logger.close();
            }
        }
        parser.notifyListeners();
    }

    public Map<Integer, List<Value<?>>> getValuesMap() {
        return this.valuesMap;
    }

    private Value sampleAll(Value oldValue, Generator generator, Set<String> sampled) {

        for (Map.Entry<String, Value> e : getNewlySampledParams(generator, sampled).entrySet()) {
            generator.setInput(e.getKey(), e.getValue());
            if (!e.getValue().isAnonymous()) sampled.add(e.getValue().getId());
        }

        return generator.generate();
    }

    private Map<String, Value> getNewlySampledParams(Generator generator, Set<String> sampled) {

        LoggerUtils.log.fine("getNewlySampledParams(" + generator.getName() + ")");
        Map<String, Value> params = generator.getParams();

        Map<String, Value> newlySampledParams = new TreeMap<>();
        for (Map.Entry<String, Value> e : params.entrySet()) {

            Value val = e.getValue();

            if (val.isRandom()) {
                if (val.isAnonymous() || !sampled.contains(val.getId())) {
                    // needs to be sampled
                    Value nv = sampleAll(val, val.getGenerator(), sampled);
                    nv.setId(val.getId());
                    newlySampledParams.put(e.getKey(), nv);
                    addValueToModelDictionary(nv);
                    if (!val.isAnonymous()) sampled.add(val.getId());

                } else {
                    // already been sampled
                    String id = e.getValue().getId();
                    newlySampledParams.put(e.getKey(), parser.getModelDictionary().get(id));
                }
            }
        }
        return newlySampledParams;
    }

    /**
     * Be careful, this is called frequently.
     * @param value the value to add to the model dictionary.
     */
    private void addValueToModelDictionary(Value value) {
        if (!value.isAnonymous()) {
            String id = value.getId();
            parser.getModelDictionary().put(id, value);
        }
    }
}
