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
public class LPhyCoreLoader implements LPhyLoader {
    private static LPhyCoreLoader lphyCoreLoader;
    final private ServiceLoader<LPhyExtension> loader;

    private LPhyCoreLoader() {
        loader = ServiceLoader.load(LPhyExtension.class);
        // register all ext
        registerExtensions(null);
    }

    // singleton
    public static LPhyCoreLoader getInstance() {
        if (lphyCoreLoader == null)
            lphyCoreLoader = new LPhyCoreLoader();
        return lphyCoreLoader;
    }

    /**
     * {@link GenerativeDistribution}
     */
    public Map<String, Set<Class<?>>> genDistDictionary;
    /**
     * {@link BasicFunction}
     */
    public Map<String, Set<Class<?>>> functionDictionary;
    /**
     * LPhy data types
     */
    public TreeSet<Class<?>> types;
//    /**
//     * LPhy sequence types {@link SequenceType}
//     */
//    public Map<String, SequenceType> dataTypeMap;

    /**
     * for creating doc only.
     * @param extClsName  the full name with package of the class
     *                 to implement {@link LPhyExtension},
     *                 such as lphy.spi.LPhyExtImpl
     */
    @Override
    public void loadExtension(String extClsName) {
        loader.reload();
        registerExtensions(extClsName);
    }

    @Override
    public void registerExtensions(String extClsName) {

        genDistDictionary = new TreeMap<>();
        functionDictionary = new TreeMap<>();
//        dataTypeMap = new ConcurrentHashMap<>();
        types = new TreeSet<>(Comparator.comparing(Class::getName));

        try {
            Iterator<LPhyExtension> extensions = loader.iterator();

            while (extensions.hasNext()) { // TODO validation if add same name

                //*** LPhyExtensionImpl must have a public no-args constructor ***//
                LPhyExtension lPhyExt = extensions.next();
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
     * for extension manager.
     * @return   a list of detected {@link LPhyExtension}.
     */
    @Override
    public List<LPhyExtension> getExtensions() {
        loader.reload();
        Iterator<LPhyExtension> extensions = loader.iterator();
        List<LPhyExtension> extList = new ArrayList<>();
        extensions.forEachRemaining(extList::add);
        return extList;
    }

}
