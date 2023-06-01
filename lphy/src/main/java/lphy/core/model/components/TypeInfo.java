package lphy.core.model.components;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The LPhy data type metadata, used by LPhy doc types.
 * @author Walter Xie
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface TypeInfo {
//    String name();
    String description() default "";
    GeneratorCategory category() default GeneratorCategory.TAXA_ALIGNMENT;
    String[] examples() default {};
}
