package lphy.base.spi;

import jebl.evolution.sequences.SequenceType;
import lphy.core.spi.LPhyLoader;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The implementation to load LPhy extensions using {@link ServiceLoader}.
 * All distributions, functions and data types will be collected
 * in this class for later use.
 *
 * @author Walter Xie
 */
public class SequenceTypeLoader implements LPhyLoader {

    /**
     * LPhy sequence types {@link SequenceType}
     */
    private Map<String, SequenceType> dataTypeMap;

    private static SequenceTypeLoader sequenceTypeLoader;
    final private ServiceLoader<SequenceTypeExtension> loader;

    private SequenceTypeLoader() {
        loader = ServiceLoader.load(SequenceTypeExtension.class);
        // register all ext
        loadAllExtensions();
    }

    // singleton
    public static synchronized SequenceTypeLoader getInstance() {
        if (sequenceTypeLoader == null)
            sequenceTypeLoader = new SequenceTypeLoader();
        return sequenceTypeLoader;
    }


    @Override
    public void loadAllExtensions() {
        registerExtensions(null);
    }

    // if extClsName is null, then load all classes,
    // otherwise load classes in a given extension.
    private void registerExtensions(String extClsName) {

        dataTypeMap = new ConcurrentHashMap<>();

        try {
            Iterator<SequenceTypeExtension> extensions = loader.iterator();

            while (extensions.hasNext()) { // TODO validation if add same name

                //*** LPhyExtensionImpl must have a public no-args constructor ***//
                SequenceTypeExtension sequenceTypeExtension = extensions.next();
                // extClsName == null then register all
                if (extClsName == null || sequenceTypeExtension.getClass().getName().equalsIgnoreCase(extClsName)) {
                    System.out.println("Registering extension from " + sequenceTypeExtension.getClass().getName());

                    // sequence types
                    Map<String, ? extends SequenceType> newDataTypes = sequenceTypeExtension.getSequenceTypes();
                    if (newDataTypes != null)
                        // TODO validate same sequence type?
                        newDataTypes.forEach(dataTypeMap::putIfAbsent);
                }
            }

            System.out.println("LPhy sequence types : " + Arrays.toString(dataTypeMap.values().toArray(new SequenceType[0])));

        } catch (ServiceConfigurationError serviceError) {
            System.err.println(serviceError);
            serviceError.printStackTrace();
        }

    }


    /**
     * for creating doc only.
     * @param extClsName  the full name with package of the class
     *                 to implement {@link SequenceTypeExtension},
     *                 such as lphy.base.spi.SequenceTypeBaseImpl
     */
    @Override
    public void loadExtension(String extClsName) {
        loader.reload();
        registerExtensions(extClsName);
    }

    /**
     * for extension manager.
     * @return   a list of detected {@link SequenceTypeExtension}.
     */
    @Override
    public List<SequenceTypeExtension> getExtensions() {
        loader.reload();
        Iterator<SequenceTypeExtension> extensions = loader.iterator();
        List<SequenceTypeExtension> extList = new ArrayList<>();
        extensions.forEachRemaining(extList::add);
        return extList;
    }


    public Set<SequenceType> getAllDataTypes() {
        return new HashSet<>(dataTypeMap.values());
    }

    /**
     * @param dataTypeName
     * @return   a registered data type, but not Standard data type.
    //     * @see Standard
     */
    public SequenceType getDataType(String dataTypeName) {
        return dataTypeMap.get(sanitise(dataTypeName));
    }

    /**
     * @param name
     * @return  trimmed lower case
     */
    public static String sanitise(String name) {
        return name.trim().toLowerCase();
    }

//    /**
//     * @param sequenceType
//     * @return true if it is {@link Standard} data type. Ignore case
//     */
//    public boolean isStandardDataType(SequenceType sequenceType) {
//        return sequenceType != null && sequenceType.getName().equalsIgnoreCase(Standard.NAME);
//    }

}
