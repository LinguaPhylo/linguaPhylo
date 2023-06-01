package lphy.core.model;

import lphy.core.model.components.Value;

//TODO not used?
public class PlaceHolder<T> extends Value<T> {

    public PlaceHolder(String key) {
        super(key, null);
    }
}
