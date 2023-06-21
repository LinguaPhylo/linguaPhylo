package lphy.core.logger;

import lphy.core.model.Value;

public interface Loggable<U> {

    String[] getLogTitles(Value<U> value);

    Object[] getLogValues(Value<U> value);
}
