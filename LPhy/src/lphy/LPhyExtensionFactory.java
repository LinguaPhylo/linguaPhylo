package lphy;

import jebl.evolution.sequences.SequenceType;
import lphy.graphicalModel.Func;
import lphy.graphicalModel.GenerativeDistribution;
import lphy.graphicalModel.Generator;
import lphy.spi.LPhyExtension;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author Walter Xie
 */
public class LPhyExtensionFactory {
    private static LPhyExtensionFactory factory;
//    private ServiceLoader<LPhyExtension> loader;

    private LPhyExtensionFactory() {
        ServiceLoader<LPhyExtension> loader = ServiceLoader.load(LPhyExtension.class);

        registerTypes(loader);
    }

    public static synchronized LPhyExtensionFactory getInstance() {
        if (factory == null) {
            factory = new LPhyExtensionFactory();
        }
        return factory;
    }

    public Map<String, Set<Class<?>>> genDistDictionary;
    public Map<String, Set<Class<?>>> functionDictionary;

    public TreeSet<Class<?>> types = new TreeSet<>(Comparator.comparing(Class::getName));

    public Map<String, SequenceType> dataTypeMap;

    private void registerTypes(ServiceLoader<LPhyExtension> loader) {

        genDistDictionary = new TreeMap<>();
        functionDictionary = new TreeMap<>();
        dataTypeMap = new ConcurrentHashMap<>();

        try {
            Iterator<LPhyExtension> extensions = loader.iterator();

            while (extensions.hasNext()) { // TODO validation if add same name

                /*
                LPhyExtensionImpl must has a public no-args constructor
                 */
                LPhyExtension lPhyExt = extensions.next();

                // GenerativeDistribution
                List<Class<? extends GenerativeDistribution>> genDist = lPhyExt.getDistributions();

                for (Class<? extends GenerativeDistribution> genClass : genDist) {
                    String name = Generator.getGeneratorName(genClass);

                    Set<Class<?>> genDistSet = genDistDictionary.computeIfAbsent(name, k -> new HashSet<>());
                    genDistSet.add(genClass);

                    types.add(GenerativeDistribution.getReturnType(genClass));

                    Collections.addAll(types, Generator.getParameterTypes(genClass, 0));

                    Collections.addAll(types, Generator.getReturnType(genClass));
                }
//        for (Class<?> genClass : lightWeightGenClasses) {
//            String name = Generator.getGeneratorName(genClass);
//
//            Set<Class<?>> genDistSet = genDistDictionary.computeIfAbsent(name, k -> new HashSet<>());
//            genDistSet.add(genClass);
//
//            types.add(LGenerator.getReturnType((Class<LGenerator>)genClass));
//        }

                // Func
                List<Class<? extends Func>> funcs = lPhyExt.getFunctions();

                for (Class<? extends Func> functionClass : funcs) {
                    String name = Generator.getGeneratorName(functionClass);

                    Set<Class<?>> funcSet = functionDictionary.computeIfAbsent(name, k -> new HashSet<>());
                    funcSet.add(functionClass);

                    Collections.addAll(types, Generator.getParameterTypes(functionClass, 0));

                    Collections.addAll(types, Generator.getReturnType(functionClass));
                }

                // data types
                Map<String, ? extends SequenceType> newDataTypes = lPhyExt.getSequenceTypes();
                if (newDataTypes != null)
                    // TODO validate same data type?
                    newDataTypes.forEach(dataTypeMap::putIfAbsent);

            }

            System.out.println("\nGenerativeDistribution : " + Arrays.toString(genDistDictionary.keySet().toArray()));
            System.out.println("Functions : " + Arrays.toString(functionDictionary.keySet().toArray()));

            TreeSet<String> typeNames = types.stream().map(Class::getSimpleName).collect(Collectors.toCollection(TreeSet::new));

            System.out.println("LPhy data types : " + typeNames);

            System.out.println("LPhy sequence types : " + Arrays.toString(dataTypeMap.values().toArray(new SequenceType[0])));

        } catch (ServiceConfigurationError serviceError) {
            System.err.println(serviceError);
            serviceError.printStackTrace();
        }

    }
}
