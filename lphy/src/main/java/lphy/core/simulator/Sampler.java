package lphy.core.simulator;

import lphy.core.logger.LoggerUtils;
import lphy.core.logger.RandomValueLogger;
import lphy.core.model.Generator;
import lphy.core.model.RandomVariable;
import lphy.core.model.Value;
import lphy.core.parser.LPhyMetaParser;
import lphy.core.parser.graphicalmodel.GraphicalModelUtils;
import lphy.core.vectorization.CompoundVectorValue;

import java.util.*;

/**
 * Sampling values for {@link RandomVariable}.
 */
public class Sampler {

    public static final int REP_START = 0;

    LPhyMetaParser parser;

    /**
     * Key is the index of replicates, value is the result of each replicate.
     */
    protected Map<Integer, List<Value<?>>> valuesAllRepsMap;

    public Sampler() {
        this.valuesAllRepsMap = new TreeMap<>();
    }

    public Sampler(LPhyMetaParser parser) {
        this.parser = parser;
        this.valuesAllRepsMap = new TreeMap<>();
    }

    /**
     * Sample the current model
     *
     * @param numReplicates    the number of times to sample
     * @param loggers the loggers to log to, cannot be null
     */
    public void sample(int numReplicates, List<? extends RandomValueLogger> loggers) {
        Objects.requireNonNull(loggers, "Simulation result loggers must not be null !");
        // clean the previous set of simulation results
        valuesAllRepsMap.clear();

        for (int i = REP_START; i < numReplicates; i++) {
            Set<String> sampled = new TreeSet<>();
            List<Value<?>> sinks = getParser().getModelSinks();
            for (RandomVariable<?> var : getParser().getAllVariablesFromSinks()) {
                getParser().getModelDictionary().remove(var.getId());
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

            List<Value<?>> values = GraphicalModelUtils.getAllValuesFromSinks(parser);
            valuesAllRepsMap.put(i, values);

            // Logging must be right after sampling each rep
            if (i == REP_START) {
                // header
                for (RandomValueLogger logger : loggers)
                    logger.start(values);
            }
            // log
            for (RandomValueLogger logger : loggers) {
                logger.log(i, values);
            }
        }
        // end
        for (RandomValueLogger logger : loggers) {
            logger.stop();
        }
//        parser.notifyListeners();
    }

    public void log(int reps, List<RandomValueLogger> loggers) {

    }

    public Map<Integer, List<Value<?>>> getValuesAllRepsMap() {
        return this.valuesAllRepsMap;
    }

    // sample all from the generator, but keep the id from old values.
    private Value sampleAll(Value oldValue, Generator generator, Set<String> sampled) {

        for (Map.Entry<String, Value> e : getNewlySampledParams(generator, sampled).entrySet()) {
            generator.setInput(e.getKey(), e.getValue());
            if (!e.getValue().isAnonymous()) sampled.add(e.getValue().getId());
        }

        Value newVal = generator.generate();
        newVal.setId(oldValue.getId());

        //TODO merge to vect class
        if (oldValue instanceof CompoundVectorValue<?> oldCVV && newVal instanceof CompoundVectorValue<?> newCVV) {
            // Must setId to the newly sampled component values inside CompoundVectorValue,
            // otherwise narratives will be broken because of null id.
            for (int i = 0; i < oldCVV.size(); i++) {
                newCVV.getComponentValue(i).setId(oldCVV.getComponentValue(i).getId());
            }
        } else if (oldValue instanceof CompoundVectorValue<?> || newVal instanceof CompoundVectorValue<?>)
            throw new IllegalArgumentException("sampleAll should return a CompoundVectorValue when given a CompoundVectorValue ! ");

        return newVal;
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

                    newlySampledParams.put(e.getKey(), nv);
                    addValueToModelDictionary(nv);
                    if (!val.isAnonymous()) sampled.add(val.getId());

                } else {
                    // already been sampled
                    String id = e.getValue().getId();
                    newlySampledParams.put(e.getKey(), getParser().getModelDictionary().get(id));
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
            getParser().getModelDictionary().put(id, value);
        }
    }

    public LPhyMetaParser getParser() {
        return parser;
    }
}
