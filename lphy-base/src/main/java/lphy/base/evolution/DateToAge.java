package lphy.base.evolution;

import lphy.core.model.DeterministicFunction;
import lphy.core.model.Value;
import lphy.core.model.annotation.GeneratorInfo;
import lphy.core.model.annotation.ParameterInfo;

public class DateToAge extends DeterministicFunction<Double[]> {
    public final String datesName = "dates";
    public DateToAge(@ParameterInfo(name = datesName, description = "the doubel array of the tip dates") Value<Double[]> dates) {
        if (dates == null){
            throw new IllegalArgumentException("dates is null");
        }
        setParam(datesName, dates);
    }

    @GeneratorInfo(name = "dateToAge", examples = {"readNexusCalibrations.lphy"}, description = "convert from dates to relative ages for setting in taxa")
    @Override
    public Value<Double[]> apply() {
        Double[] tipDates = (Double[]) getParams().get(datesName).value();
        double max = 0.0;
        for (double tipDate : tipDates) {
            max = Math.max(max, tipDate);
        }

        Double[] result = new Double[tipDates.length];
        for (int i = 0; i < tipDates.length; i++) {
            result[i] = max - tipDates[i];
        }

        return new Value<>("", result, this);
    }
}
