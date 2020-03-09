package lphy.app;

import lphy.graphicalModel.Value;

import javax.swing.*;

public interface HasComponentView<U> {

    JComponent getComponent(Value<U> value);
}
