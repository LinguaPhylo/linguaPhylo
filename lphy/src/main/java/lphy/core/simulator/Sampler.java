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
        final int nDataDictSize = parserDict.getDataDictionary().size();
        final int nDataValSet = parserDict.getDataValues().size();
        final int nModelDictSize = parserDict.getModelDictionary().size();
        final int nModelValSet = parserDict.getModelValues().size();

        List<RandomVariable<?>> variables = parserDict.getAllVariablesFromSinks();
        // remove all random variables from Model Dictionary before resampling,
        // leave the other to be replaced if they are required to update.
        for (RandomVariable<?> var : variables) {
            parserDict.getModelDictionary().remove(var.getId());
        }

        /**
         * Note: 1. The re-sampling is only targeting on the random variables,
         *          except of the case changing constants.
         *       2. When the constant value changed, it requires to call setInput() to update the outputs.
         *          See the code after calling getNewlySampledParams(...).
         *       3. This method cannot remove all var in Model Dictionary,
         *          otherwise console typing is not working;
         *       4. The value to be resampled must be in the value set created from the code in the model block,
         *          known as Model Dictionary value set.
         *          So, the values in Data Dictionary (the data block) value set must be ruled out;
         *       5. The validations suppose to check if the dictionary still maintains
         *          the correct Values during re-sampling process.
         */

        // store the value id which is newly sampled.
        Set<String> sampled = new TreeSet<>();
        for (Value value : sinks) {
            // a random variable, or the value from a deterministic function taking a random value.
            if (value.isRandom()) {
                //*** re-sampling ***//
                Value newValue;
                // re-sample if it has a generator, this will exclude constant Values.
                // AND this value is NOT generated by the code in the data block.
                if (value.getGenerator() != null &&
                        !getParserDictionary().getDataValues().contains(value)) {
                    // resample from generator
                    newValue = resample(value, value.getGenerator(), variables, sampled);
                } else throw new RuntimeException();

                // replace value and set ID
                replaceValueInModelDict(value, newValue, variables, sampled);

                // add new Value back to Model Map
//                addValueToModelDictionary(newValue);
            }
        }
        LoggerUtils.log.info("Resample variable : " + sampled);

        if (parserDict.getModelDictionary().size() != nModelDictSize)
            throw new RuntimeException("The number of stored values are not correct in parser model dictionary during resampling !\n" +
                    "It should be " + nModelDictSize + "; but get " + parserDict.getModelDictionary().size());
        if (parserDict.getModelValues().size() != nModelValSet)
            throw new RuntimeException("The number of stored values are not correct in parser value set during resampling !\n" +
                    "It should be " + nModelValSet + "; but get " + parserDict.getModelValues().size());
        // only re-sample the values in Model Dictionary. Those values in Data dict can be changed by setValue().
        if (parserDict.getDataDictionary().size() != nDataDictSize || parserDict.getDataValues().size() != nDataValSet)
            throw new RuntimeException("The number of values generated from the data block of lphy script cannot be re-sampled !");

        // get the values from traversing the graphical model,
        // if the setInput() not be called after value changes, or setParam() not implemented,
        // this list of values could be wrong.
        return GraphicalModelUtils.getAllValuesFromSinks(parserDict);
    }

    // replace old Value with new Value both in Model Map and Value Set
    private void replaceValueInModelDict(Value oldValue, Value newValue,
                                         List<RandomVariable<?>> removedRandomValues, Set<String> sampled) {
        // oldValue must exist in the list of var being removed in model parser dict before re-sampling
        if (removedRandomValues.contains(oldValue)) {
            String id = oldValue.getId();
            newValue.setId(id);
            getParserDictionary().getModelDictionary().put(oldValue.getId(), newValue);
            sampled.add(id);
        }
        Set<Value> valueSet = getParserDictionary().getModelValues();
        // only add new if oldValue exists in valueSet
        if (valueSet.remove(oldValue))
            valueSet.add(newValue);
        else
            throw new IllegalArgumentException("Try to remove the value " + oldValue +
                    ", which was not existing in parser model dictionary previously !");
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
    private Value resample(Value oldValue, Generator generator,
                           List<RandomVariable<?>> removedRandomValues, Set<String> sampled) {
        // getNewlySampledParams assumes all old values in Model Dictionary have been removed.
        // It only gets the newly sampled params Values
        Map<String, Value> newlySampledParams = getNewlySampledParams(generator, removedRandomValues, sampled);
        // setParam and update the outputs
        for (Map.Entry<String, Value> e : newlySampledParams.entrySet()) {
            Value val = e.getValue();
            // value can be null if it is an optional arg
            if (val != null) {
                // must setInput so that Values all know their outputs
                generator.setInput(e.getKey(), val);
                // Cannot add constants, which have no id
                if (!val.isAnonymous())
                    sampled.add(val.getId());
            }
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

    private Map<String, Value> getNewlySampledParams(Generator generator, List<RandomVariable<?>> removedRandomValues,
                                                     Set<String> sampled) {

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
                // re-sample if it has a generator, this will exclude constant Values.
                // AND this value is NOT generated by the code in the data block.
                if (val.getGenerator() != null &&
                        !getParserDictionary().getDataValues().contains(val)) {
                    // needs to be sampled
                    Value nv = resample(val, val.getGenerator(), removedRandomValues, sampled);

                    newlySampledParams.put(e.getKey(), nv);
//                    addValueToModelDictionary(nv);
                    // replace old Value with new Value both in Model Map and Value Set
                    replaceValueInModelDict(val, nv, removedRandomValues, sampled);
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
     * Change to {@link #replaceValueInModelDict(Value, Value, List, Set)}
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
