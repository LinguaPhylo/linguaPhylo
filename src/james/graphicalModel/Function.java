package james.graphicalModel;

import javax.swing.*;
import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.*;

public abstract class Function<U, V> extends Func implements java.util.function.Function<Value<U>, Value<V>>, Parameterized, Viewable {

    public Value<V> apply(Value<U> v, String id) {
        Value<V> val = apply(v);
        val.id = id;
        return val;
    }

}
