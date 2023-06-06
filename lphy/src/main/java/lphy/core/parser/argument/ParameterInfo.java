package lphy.core.parser.argument;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface ParameterInfo {
    String name();
    String narrativeName() default "";
    String verb() default "with";
    String description();
    boolean suppressNameInNarrative() default false;
    boolean optional() default false;
}