package lphy.core.spi;

import lphy.core.logger.LoggerUtils;

import java.util.*;

/**
 * The implementation to load LPhy extensions using {@link ServiceLoader}.
 * All distributions, functions and data types will be collected
 * in this class for later use.
 *
 * @author Walter Xie
 */
public class LPhyCoreLoader {

    private static ServiceLoader<Extension> loader;

    private Map<String, Extension> extensionMap;

    // Loader should be only called once;
    public LPhyCoreLoader() {
        if (loader == null)
            loader = ServiceLoader.load(Extension.class);

        extensionMap = new TreeMap<>();
    }

    /**
     * The method to load classes in a given {@link Extension} registered by SPI mechanism,
     * and then call {@link Extension#register()}.
     */
    public void loadExtensions() {

        //*** LPhyExtensionImpl must have a public no-args constructor ***//
        Iterator<Extension> extensions = loader.iterator();

        while (extensions.hasNext()) {
            Extension ext = null;
            try {
                //*** LPhyExtensionImpl must have a public no-args constructor ***//
                ext = extensions.next();
            } catch (ServiceConfigurationError serviceError) {
                LoggerUtils.log.severe(serviceError.getMessage());
                serviceError.printStackTrace();
                return;
            }

            String fullName = ext.getExtensionName();
            if (extensionMap.containsKey(fullName))
                throw new IllegalArgumentException("The extension " + fullName +
                        " has been loaded, but another same extension is requiring to register again ! " +
                        Arrays.toString(extensionMap.keySet().toArray()));

            System.out.println("Registering extension : " + ext.getClass().getName());

            ext.register(); // fill in the dictionary or collect all types here
            extensionMap.put(fullName, ext);
        }

    }

    public Map<String, Extension> getExtensionMap() {
        return extensionMap;
    }

    /**
     * @param extClsNameList Element is the fully qualified class name of the class
     *                   that implements {@link Extension}, such as lphy.spi.LPhyCoreImpl,
     *                   and contains all classes registered for SPI mechanism,
     *                   such as BasicFunction or GenerativeDistribution.
     * @return  The only extensions requested by the list of their class names.
     */
    public Map<String, Extension> getExtensionMap(List<String> extClsNameList) {
        Map<String, Extension> copy = new TreeMap<> (extensionMap);
        // Retains only the elements in this set that are contained in the specified collection
        copy.keySet().retainAll(extClsNameList);
        return copy;
    }

    /**
     * @param extParentCls  the parent class or interface
     * @return  The filtered extensions which are inherited from extParentCls.
     */
    public Map<String, Extension> getExtensionMap(Class<?> extParentCls) {
        Map<String, Extension> extMap = new TreeMap<> ();
        for (Map.Entry<String, Extension> entry : extensionMap.entrySet()) {
            // extParentCls is either the same as, or is a superclass or superinterface of entry.getValue()
            if (extParentCls.isAssignableFrom(entry.getValue().getClass())) {
                extMap.put(entry.getKey(), entry.getValue());
            }
        }
        return extMap;
    }


//    /**
//     * Key is the class name, values are (overloading) classes using this name.
//     * {@link GenerativeDistribution}
//     */
//    public Map<String, Set<Class<?>>> genDistDictionary;
//    /**
//     * Key is the class name, values are (overloading) classes using this name.
//     * {@link BasicFunction}
//     */
//    public Map<String, Set<Class<?>>> functionDictionary;
//    /**
//     * LPhy data types
//     */
//    public TreeSet<Class<?>> types;

//
//    /**
//     * The method to load all classes registered by SPI mechanism.
//     */
//    public void loadAllExtensions() {
//        if (loader == null)
//            loader = ServiceLoader.load(LPhyExtension.class);
//
//        registerExtensions(null);
//    }

    // if extClsName is null, then load all classes,
    // otherwise load classes in a given extension.
//    private void registerExtensions(List<String> extClsNameList) {
//
//        genDistDictionary = new TreeMap<>();
//        functionDictionary = new TreeMap<>();
////        dataTypeMap = new ConcurrentHashMap<>();
//        types = new TreeSet<>(Comparator.comparing(Class::getName));
//
//
//        //*** LPhyExtensionImpl must have a public no-args constructor ***//
//        Iterator<LPhyExtension> extensions = loader.iterator();
//
//        while (extensions.hasNext()) {
//            LPhyExtension lPhyExt = null;
//            try {
//                //*** LPhyExtensionImpl must have a public no-args constructor ***//
//                lPhyExt = extensions.next();
//            } catch (ServiceConfigurationError serviceError) {
//                System.err.println(serviceError.getMessage());
//            }
//            // extClsName == null then register all
//            if (lPhyExt != null && (extClsName == null || lPhyExt.getClass().getName().equalsIgnoreCase(extClsName))) {
//                System.out.println("Registering extension from " + lPhyExt.getClass().getName());
//
//                // GenerativeDistribution
//                List<Class<? extends GenerativeDistribution>> genDist = lPhyExt.getDistributions();
//
//                for (Class<? extends GenerativeDistribution> genClass : genDist) {
//                    String name = GeneratorUtils.getGeneratorName(genClass);
//
//                    Set<Class<?>> genDistSet = genDistDictionary.computeIfAbsent(name, k -> new HashSet<>());
//                    genDistSet.add(genClass);
//                    // collect LPhy data types from GenerativeDistribution
//                    types.add(GeneratorUtils.getReturnType(genClass));
//                    Collections.addAll(types, NarrativeUtils.getParameterTypes(genClass, 0));
//                    Collections.addAll(types, GeneratorUtils.getReturnType(genClass));
//                }
////        for (Class<?> genClass : lightWeightGenClasses) {
////            String name = Generator.getGeneratorName(genClass);
////
////            Set<Class<?>> genDistSet = genDistDictionary.computeIfAbsent(name, k -> new HashSet<>());
////            genDistSet.add(genClass);
////            types.add(LGenerator.getReturnType((Class<LGenerator>)genClass));
////        }
//                // Func
//                List<Class<? extends BasicFunction>> funcs = lPhyExt.getFunctions();
//
//                for (Class<? extends BasicFunction> functionClass : funcs) {
//                    String name = GeneratorUtils.getGeneratorName(functionClass);
//
//                    Set<Class<?>> funcSet = functionDictionary.computeIfAbsent(name, k -> new HashSet<>());
//                    funcSet.add(functionClass);
//                    // collect LPhy data types from Func
//                    Collections.addAll(types, NarrativeUtils.getParameterTypes(functionClass, 0));
//                    Collections.addAll(types, GeneratorUtils.getReturnType(functionClass));
//                }
//
//                // sequence types
////                    Map<String, ? extends SequenceType> newDataTypes = lPhyExt.getSequenceTypes();
////                    if (newDataTypes != null)
////                        // TODO validate same sequence type?
////                        newDataTypes.forEach(dataTypeMap::putIfAbsent);
//            }
//        }
//
//        System.out.println("\nGenerativeDistribution : " + Arrays.toString(genDistDictionary.keySet().toArray()));
//        System.out.println("Functions : " + Arrays.toString(functionDictionary.keySet().toArray()));
//        // for non-module release
//        if (genDistDictionary.size() < 1 || functionDictionary.size() < 1)
//            LoggerUtils.log.warning("LPhy base or equivalent lib was not loaded ! ");
//
//        TreeSet<String> typeNames = types.stream().map(Class::getSimpleName).collect(Collectors.toCollection(TreeSet::new));
//        System.out.println("LPhy data types : " + typeNames);
////            System.out.println("LPhy sequence types : " + Arrays.toString(dataTypeMap.values().toArray(new SequenceType[0])));
//
//    }

    /**
     * The method to load classes in a given extension registered by SPI mechanism.
     * @param extClsName The fully qualified class name of the class that implements
     *                   {@link LPhyExtension}, such as lphy.spi.LPhyCoreImpl,
     *                   and contains all classes registered for SPI mechanism,
     *                   such as BasicFunction or GenerativeDistribution.

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
     */
}
