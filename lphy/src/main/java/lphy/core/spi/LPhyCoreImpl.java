package lphy.core.spi;

import lphy.core.model.BasicFunction;
import lphy.core.model.GenerativeDistribution;
import lphy.core.model.GeneratorUtils;
import lphy.core.model.NarrativeUtils;
import lphy.core.parser.function.MapFunction;
import lphy.core.simulator.Simulate;
import lphy.core.vectorization.operation.ElementsAt;
import lphy.core.vectorization.operation.Range;
import lphy.core.vectorization.operation.Slice;
import lphy.core.vectorization.operation.SliceDoubleArray;

import java.util.*;

/**
 * The "Container" provider class that implements SPI
 * which include a list of {@link GenerativeDistribution}, {@link BasicFunction} required in the core.
 * It requires a public no-args constructor.
 * @author Walter Xie
 */
public class LPhyCoreImpl implements LPhyExtension {

    //+++ fill in these two getter for the registration process +++//
    @Override
    public List<Class<? extends GenerativeDistribution>> declareDistributions() {
        return new ArrayList<>();
    }

    @Override
    public List<Class<? extends BasicFunction>> declareFunctions() {
        return Arrays.asList(
                Range.class, Slice.class, SliceDoubleArray.class, ElementsAt.class,
                Simulate.class, MapFunction.class);
    }

    /**
     * Required by ServiceLoader.
     */
    public LPhyCoreImpl() { }

    /**
     * Key is the class name, values are (overloading) classes using this name.
     * {@link GenerativeDistribution}
     */
    protected Map<String, Set<Class<?>>> genDistDictionary;
    /**
     * Key is the class name, values are (overloading) classes using this name.
     * {@link BasicFunction}
     */
    protected Map<String, Set<Class<?>>> functionDictionary;
    /**
     * LPhy data types
     */
    protected TreeSet<Class<?>> types;


    @Override
    public void register() {
        genDistDictionary = new TreeMap<>();
        functionDictionary = new TreeMap<>();
//        dataTypeMap = new ConcurrentHashMap<>();
        types = new TreeSet<>(Comparator.comparing(Class::getName));

        // GenerativeDistribution
        List<Class<? extends GenerativeDistribution>> genDist = declareDistributions();

        for (Class<? extends GenerativeDistribution> genClass : genDist) {
            String name = GeneratorUtils.getGeneratorName(genClass);

            Set<Class<?>> genDistSet = genDistDictionary.computeIfAbsent(name, k -> new HashSet<>());
            genDistSet.add(genClass);
            // collect LPhy data types from GenerativeDistribution
            types.add(GeneratorUtils.getReturnType(genClass));
            Collections.addAll(types, NarrativeUtils.getParameterTypes(genClass, 0));
            Collections.addAll(types, GeneratorUtils.getReturnType(genClass));
        }
//        for (Class<?> genClass : lightWeightGenClasses) {
//            String name = Generator.getGeneratorName(genClass);
//
//            Set<Class<?>> genDistSet = genDistDictionary.computeIfAbsent(name, k -> new HashSet<>());
//            genDistSet.add(genClass);
//            types.add(LGenerator.getReturnType((Class<LGenerator>)genClass));
//        }
        // Func
        List<Class<? extends BasicFunction>> funcs = declareFunctions();

        for (Class<? extends BasicFunction> functionClass : funcs) {
            String name = GeneratorUtils.getGeneratorName(functionClass);

            Set<Class<?>> funcSet = functionDictionary.computeIfAbsent(name, k -> new HashSet<>());
            funcSet.add(functionClass);
            // collect LPhy data types from Func
            Collections.addAll(types, NarrativeUtils.getParameterTypes(functionClass, 0));
            Collections.addAll(types, GeneratorUtils.getReturnType(functionClass));
        }

    }

    @Override
    public Map<String, Set<Class<?>>> getDistributions() {
        return genDistDictionary;
    }

    @Override
    public Map<String, Set<Class<?>>> getFunctions() {
        return functionDictionary;
    }

    @Override
    public TreeSet<Class<?>> getTypes() {
        return types;
    }

    public String getExtensionName() {
        return "LPhy core";
    }
}
