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
 * The implementation to load LPhy extensions using {@link ServiceLoader}.
 * All distributions, functions and data types will be collected
 * in this class for later use.
 *
 * @author Walter Xie
 */
public class LPhyExtensionFactory {
    private static LPhyExtensionFactory factory;
    final private ServiceLoader<LPhyExtension> loader;

    private LPhyExtensionFactory() {
        loader = ServiceLoader.load(LPhyExtension.class);
        // register all ext
        registerExtensions(loader, null);
    }

    // singleton
    public static synchronized LPhyExtensionFactory getInstance() {
        if (factory == null)
            factory = new LPhyExtensionFactory();
        return factory;
    }

    /**
     * {@link GenerativeDistribution}
     */
    public Map<String, Set<Class<?>>> genDistDictionary;
    /**
     * {@link Func}
     */
    public Map<String, Set<Class<?>>> functionDictionary;
    /**
     * LPhy data types
     */
    public TreeSet<Class<?>> types = new TreeSet<>(Comparator.comparing(Class::getName));
    /**
     * LPhy sequence types {@link SequenceType}
     */
    public Map<String, SequenceType> dataTypeMap;

    /**
     * for creating doc only.
     * @param fullClsName  the full name with package of the class
     *                 to implement {@link LPhyExtension},
     *                 such as lphy.spi.LPhyExtImpl
     */
    public void loadExtension(String fullClsName) {
        loader.reload();
        registerExtensions(loader, fullClsName);
    }

    public List<LPhyExtension> getExtensions() {
        loader.reload();
        Iterator<LPhyExtension> extensions = loader.iterator();
        List<LPhyExtension> extList = new ArrayList<>();
        extensions.forEachRemaining(extList::add);
        return extList;
    }

    private void registerExtensions(ServiceLoader<LPhyExtension> loader, String clsName) {

        genDistDictionary = new TreeMap<>();
        functionDictionary = new TreeMap<>();
        dataTypeMap = new ConcurrentHashMap<>();

        try {
            Iterator<LPhyExtension> extensions = loader.iterator();

            while (extensions.hasNext()) { // TODO validation if add same name

                //*** LPhyExtensionImpl must have a public no-args constructor ***//
                LPhyExtension lPhyExt = extensions.next();
                // clsName == null then register all
                if (clsName == null || lPhyExt.getClass().getName().equalsIgnoreCase(clsName)) {
                    System.out.println("Registering extension from " + lPhyExt.getClass().getName());

                    // GenerativeDistribution
                    List<Class<? extends GenerativeDistribution>> genDist = lPhyExt.getDistributions();

                    for (Class<? extends GenerativeDistribution> genClass : genDist) {
                        String name = Generator.getGeneratorName(genClass);

                        Set<Class<?>> genDistSet = genDistDictionary.computeIfAbsent(name, k -> new HashSet<>());
                        genDistSet.add(genClass);
                        // collect LPhy data types from GenerativeDistribution
                        types.add(GenerativeDistribution.getReturnType(genClass));
                        Collections.addAll(types, Generator.getParameterTypes(genClass, 0));
                        Collections.addAll(types, Generator.getReturnType(genClass));
                    }
//        for (Class<?> genClass : lightWeightGenClasses) {
//            String name = Generator.getGeneratorName(genClass);
//
//            Set<Class<?>> genDistSet = genDistDictionary.computeIfAbsent(name, k -> new HashSet<>());
//            genDistSet.add(genClass);
//            types.add(LGenerator.getReturnType((Class<LGenerator>)genClass));
//        }
                    // Func
                    List<Class<? extends Func>> funcs = lPhyExt.getFunctions();

                    for (Class<? extends Func> functionClass : funcs) {
                        String name = Generator.getGeneratorName(functionClass);

                        Set<Class<?>> funcSet = functionDictionary.computeIfAbsent(name, k -> new HashSet<>());
                        funcSet.add(functionClass);
                        // collect LPhy data types from Func
                        Collections.addAll(types, Generator.getParameterTypes(functionClass, 0));
                        Collections.addAll(types, Generator.getReturnType(functionClass));
                    }

                    // sequence types
                    Map<String, ? extends SequenceType> newDataTypes = lPhyExt.getSequenceTypes();
                    if (newDataTypes != null)
                        // TODO validate same sequence type?
                        newDataTypes.forEach(dataTypeMap::putIfAbsent);
                }
            }

            System.out.println("\nGenerativeDistribution : " + Arrays.toString(genDistDictionary.keySet().toArray()));
            System.out.println("Functions : " + Arrays.toString(functionDictionary.keySet().toArray()));
            // for non-module release
            if (genDistDictionary.size() < 1 || functionDictionary.size() < 1)
                throw new RuntimeException("LPhy core did not load properly using SPI mechanism !");

            TreeSet<String> typeNames = types.stream().map(Class::getSimpleName).collect(Collectors.toCollection(TreeSet::new));
            System.out.println("LPhy data types : " + typeNames);
            System.out.println("LPhy sequence types : " + Arrays.toString(dataTypeMap.values().toArray(new SequenceType[0])));

        } catch (ServiceConfigurationError serviceError) {
            System.err.println(serviceError);
            serviceError.printStackTrace();
        }

    }
}
