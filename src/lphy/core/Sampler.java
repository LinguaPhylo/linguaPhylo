package lphy.core;

import lphy.app.GraphicalLPhyParser;
import lphy.graphicalModel.*;
import lphy.utils.LoggerUtils;

import java.util.*;

public class Sampler {

    GraphicalLPhyParser parser;

    public Sampler(GraphicalLPhyParser parser) {
        this.parser = parser;
    }

    /**
     * @return a list of all random variables reachable (i.e. that are depended on by) the sinks.
     */
    public List<RandomVariable<?>> getAllVariablesFromSinks() {
        List<RandomVariable<?>> variables = new ArrayList<>();
        for (Value<?> value: LPhyParser.Utils.getAllValuesFromSinks(parser)) {
            if (value instanceof RandomVariable<?>) {
                variables.add((RandomVariable<?>)value);
            }
        }
        return variables;
    }

    /**
     * Sample the current model
     *
     * @param reps    the number of times to sample
     * @param loggers the loggers to log to
     */
    public void sample(int reps, List<RandomVariableLogger> loggers) {

        for (int i = 0; i < reps; i++) {
            Set<String> sampled = new TreeSet<>();
            Set<Value<?>> sinks = parser.getSinks();
            for (RandomVariable<?> var : getAllVariablesFromSinks()) {
                parser.getDictionary().remove(var.getId());
            }

            for (Value<?> value : sinks) {

                if (value.isRandom()) {
                    Value randomValue;
                    if (value.getGenerator() != null) {
                        randomValue = sampleAll(value.getGenerator(), sampled);
                    } else throw new RuntimeException();
                    randomValue.setId(value.getId());
                    addValueToDictionary(randomValue);
                }
            }

            if (loggers != null) {
                List<RandomVariable<?>> variables = getAllVariablesFromSinks();
                for (RandomVariableLogger logger : loggers) {
                    logger.log(i, variables);
                }
            }
        }
        if (loggers != null) {
            List<RandomVariable<?>> variables = getAllVariablesFromSinks();
            for (RandomVariableLogger logger : loggers) {
                logger.close();
            }
        }
        parser.notifyListeners();
    }

    private Value sampleAll(Generator generator, Set<String> sampled) {

        for (Map.Entry<String, Value> e : getNewlySampledParams(generator, sampled).entrySet()) {
            generator.setInput(e.getKey(), e.getValue());
            if (!e.getValue().isAnonymous()) sampled.add(e.getValue().getId());
        }

        return generator.generate();
    }

    private Map<String, Value> getNewlySampledParams(Generator parameterized, Set<String> sampled) {

        LoggerUtils.log.fine("getNewlySampledParams(" + parameterized.getName() + ")");
        Map<String, Value> params = parameterized.getParams();

        Map<String, Value> newlySampledParams = new TreeMap<>();
        for (Map.Entry<String, Value> e : params.entrySet()) {

            Value val = e.getValue();

            if (val.isRandom()) {
                if (val.isAnonymous() || !sampled.contains(val.getId())) {
                    // needs to be sampled
                    Value nv = sampleAll(val.getGenerator(), sampled);
                    nv.setId(val.getId());
                    newlySampledParams.put(e.getKey(), nv);
                    addValueToDictionary(nv);
                    if (!val.isAnonymous()) sampled.add(val.getId());

                } else {
                    // already been sampled
                    String id = e.getValue().getId();
                    newlySampledParams.put(e.getKey(), parser.getDictionary().get(id));
                }
            }
        }
        return newlySampledParams;
    }

    private void addValueToDictionary(Value value) {

        LoggerUtils.log.fine("addValueToDictionary(" + value + ")");

        if (!value.isAnonymous()) {
            String id = value.getId();
            Value oldValue = parser.getDictionary().get(id);
            // Can't change the name as this will mess with updating of expression nodes!
//            if (oldValue != null) {
//                oldValue.setId(id + ".old");
//            }
            LoggerUtils.log.fine("  parser.getDictionary().put(" + id + ":" + value + ")");

            parser.getDictionary().put(id, value);
        }
    }
}
