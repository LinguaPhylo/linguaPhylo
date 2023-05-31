package lphy.base.logger;

import lphy.core.graphicalmodel.components.Value;

public interface Loggable<U> {

    String[] getLogTitles(Value<U> value);

    Object[] getLogValues(Value<U> value);
}
