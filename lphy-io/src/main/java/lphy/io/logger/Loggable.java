package lphy.io.logger;

import lphy.core.model.component.Value;

public interface Loggable<U> {

    String[] getLogTitles(Value<U> value);

    Object[] getLogValues(Value<U> value);
}
