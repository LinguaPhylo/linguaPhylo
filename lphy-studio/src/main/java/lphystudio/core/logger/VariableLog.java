package lphystudio.core.logger;

import lphy.core.logger.RandomValueLogger;
import lphy.core.model.Value;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class VariableLog extends JTextArea implements RandomValueLogger {

    boolean logVariables;
    boolean logStatistics;

    RandomNumberLogger randomNumberLogger;

    Font loggerFont = new Font(Font.MONOSPACED, Font.PLAIN, 10);

    public VariableLog(boolean logStatistics, boolean logVariables) {

        setTabSize(4);
        setEditable(false);
        setFont(loggerFont);

        this.logStatistics = logStatistics;
        this.logVariables = logVariables;
        randomNumberLogger = new RandomNumberLogger(logVariables, logStatistics);
    }

    public void clear() {
        setText("");
    }


    @Override
    public void start(List<Value<?>> randomValues) {
        //TODO
    }

    @Override
    public void log(int rep, List<Value<?>> randomValues) {
        randomNumberLogger.log(rep, randomValues);
    }

    @Override
    public void stop() {

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
                String[] titles = randomNumberLogger.loggableMap.get(value.value().getClass()).getLogTitles(value);
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
    }
}
