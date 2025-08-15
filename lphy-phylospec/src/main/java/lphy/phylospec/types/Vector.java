package lphy.phylospec.types;

import org.phylospec.types.PhyloSpecType;

public interface Vector<T extends PhyloSpecType> extends org.phylospec.types.Vector<T> {

    default Class getComponentType() {
        if (size() < 1)
            return null;
        return get(0).getClass();
    }

    /**
     * Replaced by {@link org.phylospec.types.Vector#get(int)}
     * @param i  index
     * @return   the element at the specified index
     */
    @Deprecated
    default T getComponent(int i) {
        return get(i);
    }

//    int size();
}
