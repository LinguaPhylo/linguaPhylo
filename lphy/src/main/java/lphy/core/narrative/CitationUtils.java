package lphy.core.narrative;

import lphy.core.model.annotation.Citation;

import java.lang.annotation.Annotation;

public class CitationUtils {

    public static Citation getCitation(Class<?> c) {
        Annotation[] annotations = c.getAnnotations();
        for (Annotation annotation : annotations) {
            if (annotation instanceof Citation) {
                return (Citation) annotation;
            }
        }
        return null;
    }

    /**
     * @param citation {@link Citation}
     * @param etAl     e.g. "<i>et al</i>" for html
     * @return         the citation key, such as Drummond et. al. 2005
     */
    public static String getCitationKey(Citation citation, String etAl) {
        StringBuilder builder = new StringBuilder();
        String[] authors = citation.authors();
        if (authors.length > 2) {
            builder.append(authors[0]);
            builder.append(" ").append(etAl);
        } else {
            for (int i = 0; i < authors.length; i++) {
                if (i > 0) {
                    builder.append(" and ");
                }
                builder.append(authors[i]);
            }
        }
        builder.append(" ");
        builder.append(citation.year());
        return builder.toString();
    }
}
