package lphy.core.spi;

import lphy.core.model.BasicFunction;
import lphy.core.model.GenerativeDistribution;
import lphy.core.model.GeneratorUtils;
import lphy.core.model.NarrativeUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * The implementation to load LPhy extensions using {@link ServiceLoader}.
 * All distributions, functions and data types will be collected
 * in this class for later use.
 *
 * @author Walter Xie
 */
public class LPhyCoreLoader {
    private ServiceLoader<LPhyExtension> loader;

    // Required by ServiceLoader
    public LPhyCoreLoader() { }

    /**
     * Key is the class name, values are (overloading) classes using this name.
     * {@link GenerativeDistribution}
     */
    public Map<String, Set<Class<?>>> genDistDictionary;
    /**
     * Key is the class name, values are (overloading) classes using this name.
     * {@link BasicFunction}
     */
    public Map<String, Set<Class<?>>> functionDictionary;
    /**
     * LPhy data types
     */
    public TreeSet<Class<?>> types;


    /**
     * The method to load all classes registered by SPI mechanism.
     */
    public void loadAllExtensions() {
        if (loader == null)
            loader = ServiceLoader.load(LPhyExtension.class);

        registerExtensions(null);
    }

    // if extClsName is null, then load all classes,
    // otherwise load classes in a given extension.
    private void registerExtensions(String extClsName) {

        genDistDictionary = new TreeMap<>();
        functionDictionary = new TreeMap<>();
//        dataTypeMap = new ConcurrentHashMap<>();
        types = new TreeSet<>(Comparator.comparing(Class::getName));

        try {
            //*** LPhyExtensionImpl must have a public no-args constructor ***//
            for (LPhyExtension lPhyExt : loader) {
                // extClsName == null then register all
                if (extClsName == null || lPhyExt.getClass().getName().equalsIgnoreCase(extClsName)) {
                    System.out.println("Registering extension from " + lPhyExt.getClass().getName());

                    // GenerativeDistribution
                    List<Class<? extends GenerativeDistribution>> genDist = lPhyExt.getDistributions();

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
                    List<Class<? extends BasicFunction>> funcs = lPhyExt.getFunctions();

                    for (Class<? extends BasicFunction> functionClass : funcs) {
                        String name = GeneratorUtils.getGeneratorName(functionClass);

                        Set<Class<?>> funcSet = functionDictionary.computeIfAbsent(name, k -> new HashSet<>());
                        funcSet.add(functionClass);
                        // collect LPhy data types from Func
                        Collections.addAll(types, NarrativeUtils.getParameterTypes(functionClass, 0));
                        Collections.addAll(types, GeneratorUtils.getReturnType(functionClass));
                    }

                    // sequence types
//                    Map<String, ? extends SequenceType> newDataTypes = lPhyExt.getSequenceTypes();
//                    if (newDataTypes != null)
//                        // TODO validate same sequence type?
//                        newDataTypes.forEach(dataTypeMap::putIfAbsent);
                }
            }

            System.out.println("\nGenerativeDistribution : " + Arrays.toString(genDistDictionary.keySet().toArray()));
            System.out.println("Functions : " + Arrays.toString(functionDictionary.keySet().toArray()));
            // for non-module release
            if (genDistDictionary.size() < 1 || functionDictionary.size() < 1)
                throw new RuntimeException("LPhy core did not load properly using SPI mechanism !");

            TreeSet<String> typeNames = types.stream().map(Class::getSimpleName).collect(Collectors.toCollection(TreeSet::new));
            System.out.println("LPhy data types : " + typeNames);
//            System.out.println("LPhy sequence types : " + Arrays.toString(dataTypeMap.values().toArray(new SequenceType[0])));

        } catch (ServiceConfigurationError serviceError) {
            System.err.println(serviceError);
            serviceError.printStackTrace();
        }

    }

    /**
     * The method to load classes in a given extension registered by SPI mechanism.
     * @param extClsName The fully qualified class name of the class that implements
     *                   {@link LPhyExtension}, such as lphy.spi.LPhyCoreImpl,
     *                   and contains all classes registered for SPI mechanism,
     *                   such as BasicFunction or GenerativeDistribution.
     */
    public void loadExtension(String extClsName) {
        if (loader == null)
            loader = ServiceLoader.load(LPhyExtension.class);
        else
            loader.reload();
        registerExtensions(extClsName);
    }

    /**
     * for extension manager.
     * @return   a list of detected {@link LPhyExtension}.
     */
    public List<LPhyExtension> getExtensions() {
        if (loader == null)
            loader = ServiceLoader.load(LPhyExtension.class);
        else
            loader.reload();
        Iterator<LPhyExtension> extensions = loader.iterator();
        List<LPhyExtension> extList = new ArrayList<>();
        extensions.forEachRemaining(extList::add);
        return extList;
    }

}
