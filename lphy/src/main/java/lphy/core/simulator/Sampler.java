package lphy.core.simulator;

import lphy.core.logger.LoggerUtils;
import lphy.core.model.Generator;
import lphy.core.model.RandomVariable;
import lphy.core.model.Value;
import lphy.core.parser.LPhyParserDictionary;
import lphy.core.parser.REPL;
import lphy.core.parser.graphicalmodel.GraphicalModelUtils;
import lphy.core.vectorization.CompoundVectorValue;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Sampling values from a LPhy script.
 */
public class Sampler {

    LPhyParserDictionary parser;

    public Sampler() {

    }

    public Sampler(LPhyParserDictionary parser) {
        this.parser = parser;
    }

    /**
     * @param lphyFile  a File containing LPhy script.
     * @return          a Sampler created by the given LPhy script.
     * @throws IOException
     */
    public static Sampler createSampler(File lphyFile) throws IOException {
        //*** Parse LPhy file ***//
        LPhyParserDictionary parser = new REPL();
        parser.source(lphyFile);

        // Sampler requires GraphicalLPhyParser
        Sampler sampler = new Sampler(parser);
        return sampler;
    }

    /**
     * @param lphyScript   String containing LPhy script,
     *                     which must use \n to split lines.
     * @return Sampler
     */
    public static Sampler createSampler(String lphyScript) {
        //*** Parse LPhy script in string ***//
        LPhyParserDictionary parser = new REPL();
        parser.parseScript(lphyScript);

        // Sampler requires GraphicalLPhyParser
        Sampler sampler = new Sampler(parser);
        return sampler;
    }

    /**
     * Sample the current model stored in the {@link LPhyParserDictionary} at once.
     * @param seed  the seed value, if null then use a random number.
     * @return the list {@link Value} from one simulation.
     */
    public List<Value> sample(Long seed) {
        if (seed != null)
            RandomUtils.setSeed(seed);

        Set<String> sampled = new TreeSet<>();
        List<Value<?>> sinks = getParserDictionary().getModelSinks();
        for (RandomVariable<?> var : getParserDictionary().getAllVariablesFromSinks()) {
            getParserDictionary().getModelDictionary().remove(var.getId());
        }

        for (Value<?> value : sinks) {
            if (value.isRandom()) {
                Value randomValue;
                if (value.getGenerator() != null) {
                    randomValue = sample(value, value.getGenerator(), sampled);
                } else throw new RuntimeException();
                randomValue.setId(value.getId());

                addValueToModelDictionary(randomValue);
            }
        }

        return GraphicalModelUtils.getAllValuesFromSinks(getParserDictionary());
    }


    /**
     * Sample add replicates, and call the given listeners.
     * Such as, it can be used by GUI with GUI log listeners.
     *
     * @param numReplicates    the number of times to sample
     * @param loggers the loggers to log to, cannot be null
     * @param seed  the seed value, if null then use a random number.
     * @return  a map whose key is the index of replicates, value is the result of each replicate.
     */
    public Map<Integer, List<Value>> sampleAll(int numReplicates,
                                               List<? extends SimulatorListener> loggers, Long seed) {
        Objects.requireNonNull(loggers, "Simulation result loggers must not be null !");

        Map<Integer, List<Value>> valuesAllRepsMap = new TreeMap<>();
        // start
        for (SimulatorListener logger : loggers)
            // pass numReplicates to loggers
            logger.start(numReplicates);

        for (int i = SimulatorListener.REPLICATES_START_INDEX; i < numReplicates; i++) {

            // sampling at seed
            List<Value> values = sample(seed);
            // store result
            valuesAllRepsMap.put(i, values);

            // log
            for (SimulatorListener logger : loggers) {
                logger.replicate(i, values);
            }
        }
        // end
        for (SimulatorListener logger : loggers)
            logger.complete();

//        parser.notifyListeners();
        return valuesAllRepsMap;
    }


    // sample all from the generator, but keep the id from old values.
    private Value sample(Value oldValue, Generator generator, Set<String> sampled) {

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
                    Value nv = sample(val, val.getGenerator(), sampled);

                    newlySampledParams.put(e.getKey(), nv);
                    addValueToModelDictionary(nv);
                    if (!val.isAnonymous()) sampled.add(val.getId());

                } else {
                    // already been sampled
                    String id = e.getValue().getId();
                    newlySampledParams.put(e.getKey(), getParserDictionary().getModelDictionary().get(id));
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
            getParserDictionary().getModelDictionary().put(id, value);
        }
    }

    public LPhyParserDictionary getParserDictionary() {
        return parser;
    }
}
