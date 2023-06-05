package lphy.core.model.component;

import lphy.core.model.annotation.GeneratorInfo;

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by Alexei Drummond on 17/12/19.
 */
public interface GenerativeDistribution<T> extends Generator<T> {

    /**
     * The {@link RandomVariable} must be re-wrapped
     * to ensure correct behaviour downstream.
     * @return {@link RandomVariable} to connect to this {@link GenerativeDistribution}
     */
    RandomVariable<T> sample();

    default Value<T> generate() {
        return sample();
    }

//    default RandomVariable<T[]> sample(int dim, T[] array) {
//        for (int i = 0; i < array.length; i++) {
//            array[i] = sample().value();
//        }
//        return new RandomVariable<T[]>("x", array, this);
//    }

    default RandomVariable<T> sample(String id) {
        RandomVariable<T> v = sample();
        v.setId(id);
        return v;
    }

    default double density(T t) {
        return Math.exp(logDensity(t));
    }

    default double logDensity(T t) {
        return Math.log(density(t));
    }

    default String getName() {

        String name = this.getClass().getSimpleName();

        GeneratorInfo ginfo = getInfo();
        if (ginfo != null) {
            name = ginfo.name();
        }
        return name;
    }

    default String getUniqueId() {
        return hashCode() + "";
    }

    default String codeString() {
        Map<String, Value> map = getParams();

        Iterator<Map.Entry<String, Value>> iterator = map.entrySet().iterator();

        Map.Entry<String, Value> entry = iterator.next();

        StringBuilder builder = new StringBuilder();

        builder.append(getName()).append("(").append(Generator.getArgumentCodeString(entry));
        while (iterator.hasNext()) {
            entry = iterator.next();
            builder.append(", ").append(Generator.getArgumentCodeString(entry));
        }
        builder.append(");");
        return builder.toString();
    }

    @Override
    default char generatorCodeChar() {
        return '~';
    }

    /**
     * Implementation of GraphicalModelNode value method. Unsupported for Generative Distributions.
     * @throws UnsupportedOperationException
     */
    default T value() {
        throw new UnsupportedOperationException();
    }

    static Class<?> getReturnType(Class<? extends GenerativeDistribution> genClass) {
        try {
            Method method = genClass.getMethod("sample");
            Class returnType = ReflectUtils.getGenericReturnType(method);
            return returnType;
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("GenerativeDistribution has no sample method?!");
        }
    }
}
