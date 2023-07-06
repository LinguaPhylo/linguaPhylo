package lphystudio.core.logger;

import lphy.core.logger.LoggableImpl;
import lphy.core.logger.RandomNumberFormatter;
import lphy.core.logger.RandomValueFormatter;
import lphy.core.model.Value;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class VariableLog extends JTextArea implements RandomValueFormatter {

//    boolean logVariables;
//    boolean logStatistics;

    RandomNumberFormatter randomNumberLogger;

    Font loggerFont = new Font(Font.MONOSPACED, Font.PLAIN, 10);

    public VariableLog() {//boolean logStatistics, boolean logVariables

        setTabSize(4);
        setEditable(false);
        setFont(loggerFont);

//        this.logStatistics = logStatistics;
//        this.logVariables = logVariables;
        randomNumberLogger = new RandomNumberFormatter(); // logVariables, logStatistics
    }

    public void clear() {
        setText("");
    }


    @Override
    public void setSelectedItems(List<Value<?>> randomValues) {

    }

    @Override
    public List<?> getSelectedItems() {
        return null;
    }

    @Override
    public String getHeaderFromValues() {
        //TODO
        return "";
    }

    @Override
    public String getRowFromValues(int rowIndex) {
        return randomNumberLogger.getRowFromValues(rowIndex);
    }

    @Override
    public String getFooterFromValues() {

        clear();

        List<Value> loggedFirstValues = new ArrayList<>();
        List<Boolean> lengthSummary = new ArrayList<>();
        for (Value value : randomNumberLogger.firstValues) {
            if (randomNumberLogger.isLogged(value)) {
                loggedFirstValues.add(value);
                String id = value.getId();
                if (id == null) {
                    throw new RuntimeException("Not expecting null id in variable summary!");
                }
                lengthSummary.add(!Summary.allSameLength(randomNumberLogger.variableValues.get(id)));
            }
        }

        StringBuilder builder = new StringBuilder();
        builder.append("sample");
        for (int j = 0; j < loggedFirstValues.size(); j++) {
            Value value = loggedFirstValues.get(j);
            if (lengthSummary.get(j)) {
                builder.append("\t" + value.getId() + ".length");
            } else {
                String[] titles = LoggableImpl.getLoggable(value.value().getClass()).getLogTitles(value);
                for (String title : titles) {
                    builder.append("\t");
                    builder.append(title);
                }
            }
        }
        builder.append("\n");
        for (int sample = 0; sample < randomNumberLogger.getSampleCount(); sample++) {
            builder.append(sample+"");
            for (int j = 0; j < loggedFirstValues.size(); j++) {
                Value value = loggedFirstValues.get(j);
                Double[] values = randomNumberLogger.variableValues.get(value.getId()).get(sample);
                if (lengthSummary.get(j)) {
                    builder.append("\t");
                    builder.append(values.length);
                } else {
                    for (Object val : values) {
                        builder.append("\t");
                        builder.append(val.toString());
                    }
                }
            }
            builder.append("\n");
        }

        setText(builder.toString());
        return "";
    }

    public String getFormatterDescription() {
        return getFormatterName() + " writes the values of random variables " +
                "generated from simulations into GUI.";
    }
}
