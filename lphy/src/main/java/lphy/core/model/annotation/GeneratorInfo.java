package lphy.core.model.annotation;

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
    /**
     * Deprecated former name(s) for this generator, retained for backward compatibility.
     * A script that calls the generator by an alias resolves to this generator but triggers
     * a deprecation warning. Use this when renaming a generator to avoid breaking old scripts.
     */
    String[] aliases() default {};
    String narrativeName() default "";
    String verbClause() default "is assumed to come from";
    String description();
    GeneratorCategory category() default GeneratorCategory.NONE;
    String[] examples() default {};
    //Class returnType() default Object.class;
}
