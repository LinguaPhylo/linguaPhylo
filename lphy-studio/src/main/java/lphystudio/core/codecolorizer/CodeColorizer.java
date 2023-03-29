package lphystudio.core.codecolorizer;

import javax.swing.text.Style;

public interface CodeColorizer {

    enum ElementType {
        randomVariable,
        clampedVar,
        literal,
        argumentName,
        function,
        distibution,
        punctuation,
        value,
        keyword
    };

    Style getStyle(ElementType elementType);
}
