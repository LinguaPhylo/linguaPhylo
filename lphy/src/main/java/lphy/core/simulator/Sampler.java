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
 * Re-sampling values from a {@link LPhyParserDictionary}.
 * During parsing the lphy script, the values will be generated.
 * But this can be used for resampling, such as multiple simulations,
 * which simulates the new values from their generators directly,
 * instead of calling parsers.
 */
public class Sampler {

    LPhyParserDictionary parser; // storing {@link Value}s

    public Sampler() {

    }

    /**
     * Class to sample values from a parser dictionary.
     * @param parser  The parser dictionary created from a lphy script,
     *                also storing {@link Value}s.
     */
    public Sampler(LPhyParserDictionary parser) {
        this.parser = parser;
    }

    /**
     * This will create a parser dictionary internally and then return a {@link Sampler}
     * @param lphyFile  a File containing LPhy script.
     * @param constants      constants inputted by user using macro
     * @return          a Sampler created by the given LPhy script.
     * @throws IOException
     */
    public static Sampler createSampler(File lphyFile, String[] constants) throws IOException {
        //*** Parse LPhy file ***//
        LPhyParserDictionary parser = new REPL();
        parser.source(lphyFile, constants);

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
        parser.parse(lphyScript);

        // Sampler requires GraphicalLPhyParser
        Sampler sampler = new Sampler(parser);
        return sampler;
    }

    /**
     * Sample the current model stored in the {@link LPhyParserDictionary} at once.
     * @param seed  the seed value, if null then use a random seed.
     * @return the list {@link Value} from one simulation.
     */
    public List<Value> sample(Long seed) {
        if (seed != null) {
            RandomUtils.setSeed(seed);
            LoggerUtils.log.info("Set seed = " + seed );
        }

        // store all values
        List<Value<?>> sinks = getParserDictionary().getDataModelSinks();

        // remove all variables from sinks before resampling,
        // but these are not necessary, as ModelDictionary is a Map.
        List<RandomVariable<?>> variables = getParserDictionary().getAllVariablesFromSinks();
        for (RandomVariable<?> var : variables) {
            getParserDictionary().getModelDictionary().remove(var.getId());
        }

        // store the value id which is newly sampled.
        Set<String> sampled = new TreeSet<>();
        for (Value value : sinks) {
            // a random variable, or the function of a random value.
            if (value.isRandom()) {
                Value randomValue;
                if (value.getGenerator() != null) {
                    // resample from generator
                    randomValue = resample(value, value.getGenerator(), sampled);
                } else throw new RuntimeException();
                randomValue.setId(value.getId());

                // add new Value back to Model Map
                addValueToModelDictionary(randomValue);
                //TODO replace the Model Value Set?
//                replaceValueInModelValueSet(value, randomValue);
            }
        }

        // to fix the links among all nodes
        return GraphicalModelUtils.getAllValuesFromSinks(getParserDictionary());
    }

    /**
     * This is used by studio sample button.
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

    /**
     * This is used to resample the values from a {@link LPhyParserDictionary},
     * instead of parsing lphy script again, the new values are simulated from their generators directly.
     * @param oldValue    given Value
     * @param generator   the generator of a given Value
     * @param sampled     Set to store the Value already be resampled
     * @return  a new Value generated by the generator of a given Value,
     *          and also keep the id from the given Value.
     */
    private Value resample(Value oldValue, Generator generator, Set<String> sampled) {
        // getNewlySampledParams assumes all old values in Model Dictionary have been removed.
        Map<String, Value> newlySampledParams = getNewlySampledParams(generator, sampled);
        for (Map.Entry<String, Value> e : newlySampledParams.entrySet()) {
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
                    Value nv = resample(val, val.getGenerator(), sampled);

                    newlySampledParams.put(e.getKey(), nv);
                    // add new Value back to Model Map, as the old values are removed before this method
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


//    private void replaceValueInModelValueSet(Value oldValue, Value newValue) {
//        Set<Value> vS = getParserDictionary().getModelValues();
//        int size = vS.size();
          //TODO replace the value() not the object Value,
          //TODO or after GraphicalModelUtils.getAllValuesFromSinks(getParserDictionary());
          //TODO which links all nodes
//        vS.remove(oldValue);
//        vS.add(newValue);
//
//        if (vS.size() != size)
//            throw new IllegalArgumentException("Err");
//    }
}
