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

    private ServiceLoader<SequenceTypeExtension> loader;

    // Required by ServiceLoader
    public SequenceTypeLoader() {
    }

    // register all SequenceType ext
    @Override
    public void loadAllExtensions() {
        if (loader == null)
            loader = ServiceLoader.load(SequenceTypeExtension.class);
        registerExtensions(null);
    }

    // if extClsName is null, then load all classes,
    // otherwise load classes in a given extension.
    private void registerExtensions(String extClsName) {

        dataTypeMap = new ConcurrentHashMap<>();

        try {
            //*** SequenceTypeExtensionImpl must have a public no-args constructor ***//
            for (SequenceTypeExtension sequenceTypeExtension : loader) {
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
        if (loader == null)
            loader = ServiceLoader.load(SequenceTypeExtension.class);
        else
            loader.reload();
        registerExtensions(extClsName);
    }

    /**
     * for extension manager.
     * @return   a list of detected {@link SequenceTypeExtension}.
     */
    @Override
    public List<SequenceTypeExtension> getExtensions() {
        if (loader == null)
            loader = ServiceLoader.load(SequenceTypeExtension.class);
        else
            loader.reload();
        Iterator<SequenceTypeExtension> extensions = loader.iterator();
        List<SequenceTypeExtension> extList = new ArrayList<>();
        extensions.forEachRemaining(extList::add);
        return extList;
    }

    //*** static ***//

    /**
     * LPhy sequence types {@link SequenceType}
     */
    private static Map<String, SequenceType> dataTypeMap;

    /**
     * @param dataTypeName
     * @return   a registered data type, but not Standard data type.
    //     * @see Standard
     */
    public static SequenceType getDataType(String dataTypeName) {
        if (dataTypeMap == null) return null;
        return dataTypeMap.get(sanitise(dataTypeName));
    }

    /**
     * @param name
     * @return  trimmed lower case
     */
    public static String sanitise(String name) {
        return name.trim().toLowerCase();
    }

    public Set<SequenceType> getAllDataTypes() {
        return new HashSet<>(dataTypeMap.values());
    }



//    /**
//     * @param sequenceType
//     * @return true if it is {@link Standard} data type. Ignore case
//     */
//    public boolean isStandardDataType(SequenceType sequenceType) {
//        return sequenceType != null && sequenceType.getName().equalsIgnoreCase(Standard.NAME);
//    }

}
