package lphy.graphicalModel;

import java.lang.annotation.Inherited;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * This is an annotation that can be used to add a reference
 * to a class.
 * <p/>
 * Example: @Citation("Darwin & Wallace (1858) 'On the Tendency
 * of Species to form Varieties and on the Perpetuation of Varieties
 * and Species by Natural Means of Selection.' Linnean Society")
 * just before class declarations.
 * <p/>
 */
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Repeatable(Citation.Citations.class)
public @interface Citation {

    /**
     * @return the citation for the class
     */
    String value();

    String DOI() default "";

    int year() default 0;

    String firstAuthorSurname() default "";

    /**
     * The Citations annotation is required to retrieve classes annotated with
     * multiple citations.
     **/
    @Inherited
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Citations {
        Citation[] value();
    }
}


