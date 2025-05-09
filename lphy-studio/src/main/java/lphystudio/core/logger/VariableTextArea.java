package lphystudio.core.logger;

import lphy.core.logger.RandomNumberLoggerListener;
import lphy.core.model.Value;
import lphy.core.simulator.SimulatorListener;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Map;

public class VariableTextArea extends JTextArea implements SimulatorListener {

//    boolean logVariables;
//    boolean logStatistics;

    RandomNumberLoggerListener randomNumberLogger;

    Font loggerFont = new Font(Font.MONOSPACED, Font.PLAIN, 10);

    public VariableTextArea() {//boolean logStatistics, boolean logVariables

        setTabSize(4);
        setEditable(false);
        setFont(loggerFont);

        randomNumberLogger = new RandomNumberLoggerListener(); // logVariables, logStatistics
    }

    public void clear() {
        setText("");
    }


    @Override
    public void start(Object... configs) {

    }

    @Override
    public void replicate(int index, List<Value> values) {
        randomNumberLogger.replicate(index, values);
    }

    @Override
    public void complete() {
        clear();

        List<String> headers = randomNumberLogger.getHeaders();
        Map<String, List<Double>> formattedValuesById = randomNumberLogger.getFormattedValuesById();
        List<String> rowNames = randomNumberLogger.getRowNames();
        final int sampleCount = randomNumberLogger.getSampleCount();

        if ( formattedValuesById.size() > 0 ) {

            boolean allSameLen = true;
            for (Map.Entry<String, List<Double>> entry : formattedValuesById.entrySet()) {
                if (entry.getValue().size() != sampleCount) {
                    allSameLen = false;
                    break;
                }
            }

            if (!allSameLen || headers.size() != formattedValuesById.size() ||
                    rowNames.size() != sampleCount) {
                throw new RuntimeException("The lists of random number values cannot have different size ! ");
            }

            StringBuilder builder = new StringBuilder();
            // Sample
            builder.append(RandomNumberLoggerListener.COL_NAME_OF_INDEX);
            // col names
            for (String colName : headers) {
                builder.append("\t").append(colName);
            }
            builder.append("\n");

            for (int sample = 0; sample < sampleCount; sample++) {
//            builder.append(sample+"");
                builder.append(rowNames.get(sample));

                for (String valId : headers) {

                    List<Double> formattedValues = formattedValuesById.get(valId);

                    builder.append("\t").append(formattedValues.get(sample));
                }
                builder.append("\n");
            }

            setText(builder.toString());
        }

    }

}
