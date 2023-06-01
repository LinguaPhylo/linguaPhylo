package lphy.io.logger;

import lphy.core.graphicalmodel.components.RandomVariable;
import lphy.core.graphicalmodel.components.Value;
import lphy.core.graphicalmodel.components.ValueUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RandomNumberLogger implements RandomValueLogger {

    public Map<Class, Loggable> loggableMap;

    boolean logVariables;
    boolean logStatistics;

    public Map<String, List<Double[]>> variableValues = new HashMap<>();

    public List<Value> firstValues = new ArrayList<>();
    int sampleCount;

    public RandomNumberLogger(boolean logVariables, boolean logStatistics) {
        this.logVariables = logVariables;
        this.logStatistics = logStatistics;

        loggableMap = VarFileLogger.loggableMap;
    }

    public int getSampleCount() {
        return sampleCount;
    }

    public void log(int rep, List<Value<?>> randomValues) {
        if (rep == 0) {
            firstValues.clear();
            variableValues.clear();
            firstValues.addAll(randomValues);
        }

        for (Value randomValue : randomValues) {

            if (isLogged(randomValue)) {
                Loggable loggable = loggableMap.get(randomValue.value().getClass());
                if (loggable != null) {
                    Object[] logValues = loggable.getLogValues(randomValue);
                    Double[] values = new Double[logValues.length];
                    for (int i = 0; i < logValues.length; i++) {
                        if (logValues[i] instanceof Number) {
                            values[i] = ((Number)logValues[i]).doubleValue();
                        } else if (logValues[i] instanceof Boolean) {
                            values[i] = ((Boolean)logValues[i]) ? 1.0 : 0.0;
                        }
                    }
                    List<Double[]> varValues = variableValues.computeIfAbsent(randomValue.getId(), k -> new ArrayList<>());
                    varValues.add(values);
                }
            }
        }
        sampleCount = rep + 1;
    }

    public boolean isLogged(Value randomValue) {
        boolean random = (randomValue instanceof RandomVariable && logVariables) ||
                (!(randomValue instanceof RandomVariable) && randomValue.isRandom() && logStatistics);
        boolean number = ValueUtils.isNumberOrNumberArray(randomValue) ||
                ValueUtils.is2DNumberArray(randomValue) ||
                randomValue.value() instanceof Boolean;

        return random && number && !randomValue.isAnonymous();
    }

    /**
     * Called once all replicates have been logged.
     */
    public void close() {

    }
}