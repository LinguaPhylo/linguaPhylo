package lphy.core.logger;

import lphy.core.model.Value;
import lphy.core.model.ValueUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Deprecated
public class RandomNumberFormatter implements RandomValueFormatter {

//    public Map<Class<?>, Loggable<?>> loggableMap;
//    boolean logVariables;
//    boolean logStatistics;

    public Map<String, List<Double[]>> variableValues = new HashMap<>();

    public List<Value> firstValues = new ArrayList<>();
    int sampleCount;

    List<Value<?>> loggableValues;

    public RandomNumberFormatter() {
//        this.logVariables = logVariables;
//        this.logStatistics = logStatistics;

//        loggableMap = LoggableImpl.getLoggableMap();
    }

    public int getSampleCount() {
        return sampleCount;
    }

    @Override
    public void setSelectedItems(List<Value<?>> randomValues) {
        this.loggableValues = new ArrayList<>();
        for (Value<?> randomValue : randomValues) {
            if (isLogged(randomValue)) {
                Loggable<?> loggable = LoggableImpl.getLoggable(randomValue.value().getClass());
                if (loggable != null)
                    this.loggableValues.add(randomValue);
            }
        }
    }

    @Override
    public List<Value<?>> getSelectedItems() {
        return this.loggableValues;
    }

    @Override
    public String getHeaderFromValues() {
        return null;
    }

    @Override
    public String getRowFromValues(int rowIndex) {
        if (rowIndex == 0) {
            firstValues.clear();
            variableValues.clear();
            firstValues.addAll(getSelectedItems());
        }

        for (Value randomValue : getSelectedItems()) {

            if (isLogged(randomValue)) {
                Loggable loggable = LoggableImpl.getLoggable(randomValue.value().getClass());
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
        sampleCount = rowIndex + 1;
        throw new UnsupportedOperationException();
    }

    public boolean isLogged(Value randomValue) {
        boolean random = ValueLoggerListener.isNamedRandomValue(randomValue);
        boolean number = ValueUtils.isNumberOrNumberArray(randomValue) ||
                ValueUtils.is2DNumberArray(randomValue) ||
                // 0|1
                randomValue.value() instanceof Boolean;

        return random && number && !randomValue.isAnonymous();
    }

    /**
     * Called once all replicates have been logged.
     */
    @Override
    public String getFooterFromValues() {
        return null;
    }

    public String getFormatterDescription() {
        return getFormatterName() + " writes the values of random variables " +
                "generated from each simulation.";
    }

}