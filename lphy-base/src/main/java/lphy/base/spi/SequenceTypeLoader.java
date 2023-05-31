package lphy.base.spi;

import jebl.evolution.sequences.SequenceType;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The implementation to load LPhy extensions using {@link ServiceLoader}.
 * All distributions, functions and data types will be collected
 * in this class for later use.
 *
 * @author Walter Xie
 */
public class SequenceTypeLoader {

    /**
     * LPhy sequence types {@link SequenceType}
     */
    private Map<String, SequenceType> dataTypeMap;

    private static SequenceTypeLoader sequenceTypeLoader;
    final private ServiceLoader<SequenceTypeExtension> loader;

    private SequenceTypeLoader() {
        loader = ServiceLoader.load(SequenceTypeExtension.class);
        // register all ext
        registerExtensions(loader, null);
    }

    // singleton
    public static synchronized SequenceTypeLoader getInstance() {
        if (sequenceTypeLoader == null)
            sequenceTypeLoader = new SequenceTypeLoader();
        return sequenceTypeLoader;
    }


    /**
     * for creating doc only.
     * @param fullClsName  the full name with package of the class
     *                 to implement {@link SequenceTypeExtension},
     *                 such as lphy.spi.LPhyExtImpl
     */
    public void loadExtension(String fullClsName) {
        loader.reload();
        registerExtensions(loader, fullClsName);
    }

    /**
     * for extension manager.
     * @return   a list of detected {@link SequenceTypeExtension}.
     */
    public List<SequenceTypeExtension> getExtensions() {
        loader.reload();
        Iterator<SequenceTypeExtension> extensions = loader.iterator();
        List<SequenceTypeExtension> extList = new ArrayList<>();
        extensions.forEachRemaining(extList::add);
        return extList;
    }

    private void registerExtensions(ServiceLoader<SequenceTypeExtension> loader, String clsName) {

        dataTypeMap = new ConcurrentHashMap<>();

        try {
            Iterator<SequenceTypeExtension> extensions = loader.iterator();

            while (extensions.hasNext()) { // TODO validation if add same name

                //*** LPhyExtensionImpl must have a public no-args constructor ***//
                SequenceTypeExtension sequenceTypeExtension = extensions.next();
                // clsName == null then register all
                if (clsName == null || sequenceTypeExtension.getClass().getName().equalsIgnoreCase(clsName)) {
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
