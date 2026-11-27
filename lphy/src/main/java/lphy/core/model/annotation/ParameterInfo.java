package lphy.core.model.annotation;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface ParameterInfo {
    String name();
    /**
     * Deprecated former name(s) for this parameter, retained for backward compatibility.
     * A script that supplies an argument by an alias is matched as if the canonical
     * {@link #name()} had been used, but triggers a deprecation warning. Use this when
     * renaming a parameter to avoid breaking old scripts.
     */
    String[] aliases() default {};
    String narrativeName() default "";
    String verb() default "with";
    String description();
    boolean suppressNameInNarrative() default false;
    boolean optional() default false;
    // for phylospec name
    String phylospec() default "";
}