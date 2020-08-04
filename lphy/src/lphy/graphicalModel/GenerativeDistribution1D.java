package lphy.graphicalModel;

import javax.swing.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by adru001 on 17/12/19.
 */
public interface GenerativeDistribution1D<T> extends GenerativeDistribution<T> {

    /**
     * @return a two-dimensional array containing the lower and the upper bounds of the domain
     */
    T[] getDomainBounds();

}
