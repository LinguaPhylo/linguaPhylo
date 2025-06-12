package lphy.core.model.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;

/**
 * Annotation to mark functions as file I/O operations for the model builder.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface IOFunction {
    enum Role { dataInput, dataOutput }

    /** Whether this function reads data or writes data */
    Role role();

    /** Supported file extensions, e.g. {".nex", ".fasta"} */
    String[] extensions();

    /** Name of the argument that represents the file path */
    String fileArgument();
}