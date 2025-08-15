package lphy.phylospec.types;

import org.phylospec.types.Primitive;

public interface PrimitiveType<T> extends Primitive<T> {

    /**
     * Get the primitive type of the value
     *
     * @return the class of the primitive type
     */
    default Class getPrimitiveType() {
        return getPrimitive().getClass();
    }

}
