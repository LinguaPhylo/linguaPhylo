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
 *
 * During parsing the lphy script, the values will be generated.
 * But this can be used for resampling without calling parser, such as multiple simulations,
 * which simulates the new values from their generators directly,
 * instead of calling parsers.
 * {@link #sample(Long)} is the main method to process simulations in lphy.
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
     * The <b>only main</b> method to re-sample values stored in {@link LPhyParserDictionary}.
     * This will not use parser.
     * @param seed  the seed value, if null then use a random seed.
     * @return the list {@link Value} from one simulation.
     */
    public List<Value> sample(Long seed) {
        if (seed != null) {
            RandomUtils.setSeed(seed);
            LoggerUtils.log.info("Set seed = " + seed );
        }

        LPhyParserDictionary parserDict = getParserDictionary();
        // store all values
        List<Value<?>> sinks = parserDict.getDataModelSinks();
        int nModelDict = parserDict.getModelDictionary().size();
        int nValSet = parserDict.getModelValues().size();

        List<RandomVariable<?>> variables = parserDict.getAllVariablesFromSinks();
        // remove all random variables from Model Dictionary before resampling,
        // leave the other to be replaced if they are required to update.
        for (RandomVariable<?> var : variables) {
            parserDict.getModelDictionary().remove(var.getId());
        }

        //TODO: cannot remove all, otherwise console typing is not working

        // record the value are removed before re-sampling
//        Set<String> removed = parserDict.getModelDictionary().keySet();
//        int nValSet = parserDict.getModelValues().size();
//        Collection<Value<?>> modelValues = parserDict.getModelDictionary().values();
        // clean old values both in Map and Set
//        parserDict.getModelValues().removeAll(modelValues);
//        parserDict.getModelDictionary().clear();

        // store the value id which is newly sampled.
        Set<String> sampled = new TreeSet<>();
        for (Value value : sinks) {
            // a random variable, or the value from a deterministic function taking a random value.
            if (value.isRandom()) {
                //  re-sampling
                Value randomValue;
                if (value.getGenerator() != null) {
                    // resample from generator
                    randomValue = resample(value, value.getGenerator(), sampled);
                } else throw new RuntimeException();

                // replace value and set ID
                replaceValueInModelDict(value, randomValue, sampled);

                // add new Value back to Model Map
//                addValueToModelDictionary(randomValue);
            }
        }
        System.out.println("Resample variable : " + sampled);
        if (parserDict.getModelDictionary().size() != nModelDict)
            throw new RuntimeException("The number of stored values are not correct in parser model dictionary during resampling !\n" +
                    "It should be " + nModelDict + "; but get " + parserDict.getModelDictionary().size());
        if (parserDict.getModelValues().size() != nValSet)
            throw new RuntimeException("The number of stored values are not correct in parser value set during resampling !\n" +
                    "It should be " + nValSet + "; but get " + parserDict.getModelValues().size());

        // get the values from traversing the graphical model,
        // if the setInput() not be called after value changes, or setParam() not implemented,
        // this list of values could be wrong.
        return GraphicalModelUtils.getAllValuesFromSinks(parserDict);
    }

    // replace old Value with new Value both in Model Map and Value Set
    private void replaceValueInModelDict(Value oldValue, Value newValue, Set<String> sampled) {
        if (!oldValue.isAnonymous()) {
            String id = oldValue.getId();
            newValue.setId(id);
            getParserDictionary().getModelDictionary().put(oldValue.getId(), newValue);
            sampled.add(id);
        }
        Set<Value> valueSet = getParserDictionary().getModelValues();
        valueSet.remove(oldValue);
        valueSet.add(newValue);
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
        // It only gets the newly sampled params Values
        Map<String, Value> newlySampledParams = getNewlySampledParams(oldValue, generator, sampled);
        // setParam and update the outputs
        for (Map.Entry<String, Value> e : newlySampledParams.entrySet()) {
            Value val = e.getValue();
            // must setInput so that Values all know their outputs
            generator.setInput(e.getKey(), val);
            // Cannot add constants, which have no id
            if (!val.isAnonymous())
                sampled.add(val.getId());
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

    private Map<String, Value> getNewlySampledParams(Value oldValue, Generator generator, Set<String> sampled) {

        LoggerUtils.log.fine("getNewlySampledParams(" + generator.getName() + ")");
        Map<String, Value> params = generator.getParams();

        Map<String, Value> newlySampledParams = new TreeMap<>();
        for (Map.Entry<String, Value> e : params.entrySet()) {
            Value val = e.getValue();

            // Here, it should not only re-generate its value when Value is isRandom(),
            // but also re-generate its value for the deterministic functions,
            // when its parameter value is changed.

//            if (val.isRandom()) {
            if (val.isAnonymous() || !sampled.contains(val.getId())) {
                // until the constants who have no generator
                if (val.getGenerator() != null) {
                    // needs to be sampled
                    Value nv = resample(val, val.getGenerator(), sampled);

                    newlySampledParams.put(e.getKey(), nv);
//                    addValueToModelDictionary(nv);
                    // replace old Value with new Value both in Model Map and Value Set
                    replaceValueInModelDict(val, nv, sampled);
                } else {
                    // do not know which constant is changed, so add it anyway to setInput again
                    newlySampledParams.put(e.getKey(), val);
                }
            } else {
                // already been sampled
                String id = e.getValue().getId();
                newlySampledParams.put(e.getKey(), getParserDictionary().getModelDictionary().get(id));
            }
//            }
        }
        return newlySampledParams;
    }


    /**
     * Change to {@link #replaceValueInModelDict(Value, Value, Set)}
     * Be careful, this is called frequently.
     * @param value the value to add to the model dictionary.
     */
    @Deprecated
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
