package lphy.core.logger;

import lphy.core.model.Value;

@Deprecated
public interface Loggable<U> {

    String[] getLogTitles(Value<U> value);

    Object[] getLogValues(Value<U> value);
}
