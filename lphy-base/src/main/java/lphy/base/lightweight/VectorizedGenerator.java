package lphy.base.lightweight;

import lphy.core.graphicalmodel.components.Argument;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public abstract class VectorizedGenerator<T> implements LGenerator<T[]> {

    Class<? extends LGenerator> baseGeneratorClass;
    LGenerator<T> baseGenerator;

    private SortedMap<Argument, Object> argumentValues;
    private SortedMap<String, Argument> argumentsByName = new TreeMap<>();

    private List<Method> vectorizedSetMethods = new ArrayList<>();
    private List<Object> vectorizedArgumentValues = new ArrayList<>();

    int dim;

    public VectorizedGenerator(Class<? extends LGenerator> baseGeneratorClass, SortedMap<Argument, Object> argumentValues) {
        this.baseGeneratorClass = baseGeneratorClass;
        for (Argument arg : LGenerator.getArguments(baseGeneratorClass, 0)) {
            argumentsByName.put(arg.name, arg);
        }
        if (argumentValues == null) {
            this.argumentValues = new TreeMap<>();
        }
        setup();
    }

    public VectorizedGenerator(LGenerator<T> baseGenerator, SortedMap<Argument, Object> argumentValues) {
        baseGeneratorClass = baseGenerator.getClass();
        this.baseGenerator = baseGenerator;
        this.argumentValues = argumentValues;

        for (Argument arg : baseGenerator.getArguments()) {
            argumentsByName.put(arg.name, arg);
        }

        if (argumentValues == null) {
            this.argumentValues = new TreeMap<>();
        }
        setup();
    }

    void setup() {

        // create first instance of lightweight generator if necessary
        if (baseGenerator == null) {
            List<Argument> args = LGenerator.getArguments(baseGeneratorClass, 0);
            Object[] initArgs = new Object[args.size()];
            for (int i = 0; i < initArgs.length; i++) {
                Argument arg = args.get(i);
                Object value = argumentValues.get(arg.name);
                if (isArrayOfType(value, arg.type)) {
                    initArgs[i] = Array.get(value, 0);
                } else {
                    initArgs[i] = value;
                }
            }
        }

        vectorizedSetMethods.clear();
        vectorizedArgumentValues.clear();

        dim = 1;
        for (Map.Entry<Argument, Object> entry : argumentValues.entrySet()) {
            Argument arg = entry.getKey();
            Object value = entry.getValue();
            if (isArrayOfType(value, arg.type)) {
                int vectorSize = Array.getLength(value);
                if (dim == 1) {
                    dim = vectorSize;
                } else if (dim != vectorSize) {
                    throw new RuntimeException("Vector sizes do not match!");
                }
                vectorizedSetMethods.add(baseGenerator.getSetMethod(arg));
                vectorizedArgumentValues.add(value);
                Object input = Array.get(value, 0);
                baseGenerator.setArgumentValue(arg, input);
            } else {
                baseGenerator.setArgumentValue(arg, value);
            }
        }
    }

    public boolean isRandomGenerator() {
        return baseGenerator.isRandomGenerator();
    }

    public T[] generateRaw() {

        T first = baseGenerator.generateRaw();

        T[] result = (T[]) Array.newInstance(first.getClass(), dim);
        result[0] = first;
        for (int i = 1; i < result.length; i++) {
            for (int j = 0; j < vectorizedSetMethods.size(); j++) {
                Object input = Array.get(vectorizedArgumentValues.get(j), i);
                try {
                    vectorizedSetMethods.get(j).invoke(baseGenerator, input);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
                result[i] = baseGenerator.generateRaw();
            }
        }
        return result;
    }

    @Override
    public void setArgumentValue(Argument argument, Object val) {
        argumentValues.put(argument, val);
        setup();
    }

    @Override
    public void setArgumentValue(String argument, Object val) {
        argumentValues.put(argumentsByName.get(argument), val);
        setup();
    }

    public Object getArgumentValue(Argument argument) {
        return argumentValues.get(argument);
    }

    static boolean isArrayOfType(Object maybeArray, Class ofType) {

        if (maybeArray.getClass().isArray()) {
            Object firstElement = Array.get(maybeArray, 0);
            return firstElement.getClass() == ofType;
        }
        return false;
    }
}
