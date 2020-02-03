package james.swing;

import james.graphicalModel.Value;

import javax.swing.*;

public interface HasComponentView<U> {

    JComponent getComponent(Value<U> value);
}
