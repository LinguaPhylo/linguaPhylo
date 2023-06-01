package lphy.core.model.components;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by Alexei Drummond on 2/02/20.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface GeneratorInfo {
    String name();
    String narrativeName() default "";
    String verbClause() default "is assumed to come from";
    String description();
    GeneratorCategory category() default GeneratorCategory.NONE;
    String[] examples() default {};
    //Class returnType() default Object.class;
}
