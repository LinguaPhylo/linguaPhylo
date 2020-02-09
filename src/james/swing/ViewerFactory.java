package james.swing;

import javax.swing.*;

public interface ViewerFactory<T> {

    JComponent createViewer(T toBeViewed);
}
