package lphy.core.model.annotation;

import lphy.core.model.component.GeneratorCategory;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by Alexei Drummond on 2/02/20.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface MethodInfo {
    String description();
    String verbClause() default "is";
    String narrativeName() default "";
    GeneratorCategory category() default GeneratorCategory.NONE;
    String[] examples() default {};
}
