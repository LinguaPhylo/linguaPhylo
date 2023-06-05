package lphy.core.narrative;

import lphy.core.model.annotation.Citation;
import lphy.core.model.component.Generator;
import lphy.core.model.component.Value;
import lphy.core.model.component.ValueUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class NarrativeUtils {

    public static String getArticle(Value value, boolean unique) {
        return getArticle(value, getName(value), unique, false);
    }

    public static String getArticle(Value value, String name, boolean unique, boolean lowercase) {

        //String article = lowercase ? "the" : "The";
        String article = lowercase ? "" : "The";
        if (!unique && value.isAnonymous()) article = getIndefiniteArticle(name, lowercase);
        return article;
    }

    private static String sanitizeDOI(String doi) {
        if (doi.startsWith("http")) return doi;
        if (doi.startsWith("doi.org")) return "https://" + doi;
        if (doi.length() > 0 && "01923456789".indexOf(doi.charAt(0)) >= 0) return "https://doi.org/" + doi;
        return doi;
    }

    /**
     * @param citation  {@link Citation}
     * @return a URL to this citation. If it has DOI, then use https://doi.org/,
     *         if it has ISBN, then use https://books.google.co.nz/books?vid=ISBN,
     *         otherwise return an empty string.
     */
    public static String getURL(Citation citation) {
        if (citation.DOI().length() >0)
            return sanitizeDOI(citation.DOI());
        // a book
        else if (citation.ISBN().length() >0)
            return "https://books.google.co.nz/books?vid=ISBN" + citation.ISBN();
        return "";
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


    static final Map<String, String> TYPE_MAP = Map.of(
            "Alignment[]", "Alignments",
            "Double[]", "Vector",
            "Double[][]", "Matrix",
            "Integer[]", "Vector",
            "Integer[][]", "Matrix",
            "TimeTree", "Time Tree"
    );

    private static String sanitizeTypeName(String typeName) {
        String sanitizedTypeName = TYPE_MAP.get(typeName);
        if (sanitizedTypeName != null) return sanitizedTypeName.toLowerCase();
        return typeName.toLowerCase();

    }

    public static String getTypeName(Value value) {
        if (value.getGenerator() != null) return sanitizeTypeName(value.getGenerator().getTypeName());
        return getSimpleTypeName(value);
    }

    public static String getSimpleTypeName(Value value) {
        String s = value.getType().getSimpleName();

        String[] r = s.split("(?<=.)(?=\\p{Lu})");

        if (r.length > 1) {
            StringBuilder b = new StringBuilder();
            int count = 0;
            for (String part : r) {
                if (count > 0) b.append(" ");
                b.append(part.toLowerCase());
                count += 1;
            }
            return b.toString();
        } else return sanitizeTypeName(s);
    }

    public static boolean hasSingleGeneratorOutput(Value value) {
        return value != null && value.getOutputs().size() == 1 && (value.getOutputs().get(0) instanceof Generator);
    }


    public static String getName(Value value) {
        // empty array
//        if (value == null) return "";

        String name;

        if (hasSingleGeneratorOutput(value)) {
            Generator generator = (Generator) value.getOutputs().get(0);
            name = generator.getNarrativeName(value);
            if (name == null || name.equals("")) {
                if (value.value() != null && value.value() instanceof NarrativeName val) {
                    name = val.getNarrativeName();
                } else name = getTypeName(value);
            }
        } else if (value.value() != null && value.value() instanceof NarrativeName val) {
            name = val.getNarrativeName();
        } else name = getTypeName(value);
        return name;
    }

    public static String getValueClause(Value value, boolean unique, Narrative narrative) {
        return getValueClause(value, unique, false, false, narrative);
    }

    public static String getValueClause(Value value, boolean unique, boolean lowercase, boolean plural, Narrative narrative) {
        return getValueClause(value, unique, lowercase, plural, null, narrative);
    }

    public static String getValueClause(Value value, boolean unique, boolean lowercase, boolean plural, Generator generator, Narrative narrative) {
        // empty array
//        if (value == null) return "";

        StringBuilder builder = new StringBuilder();

        String name;
        if (generator != null) {
            name = generator.getNarrativeName(value);
            if (name == null || name.equals("")) name = getName(value);
        } else {
            name = getName(value);
        }

        if (plural) {
            name = pluralize(name);
        }
        String narrativeId = null;
        if (!value.isAnonymous()) {
            narrativeId = narrative.getId(value, true);
        }

        boolean match = !value.isAnonymous() && name.equals(value.getId());

        if (unique || !name.endsWith("s") || plural) {
            String article = getArticle(value, name, unique, lowercase);
            if (match) {
                narrativeId = article + " " + narrativeId;
            } else name = article + " " + name;
        }
        String firstLetter = name.substring(0, 1);
        name = (lowercase ? firstLetter.toLowerCase() : firstLetter.toUpperCase()) + name.substring(1);

        if (match) {
            builder.append(narrativeId);
        } else {
            builder.append(name);
            builder.append((value.isAnonymous() ? "" : ", " + narrativeId));
        }
        if (value.getGenerator() == null && !value.isRandom()) {
            builder.append(" of ");
            builder.append(narrative.text(ValueUtils.valueToString(value.value())));
        }
        return builder.toString();
    }

    static List<String> anNouns = new ArrayList<>(List.of("n", "m", "HKY"));

    public static String getIndefiniteArticle(String noun, boolean lowercase) {
        String article = "A";
        if ("aeiou".indexOf(noun.charAt(0)) >= 0 || anNouns.contains(noun.split(" ")[0])) article = "An";
        if (lowercase) article = article.toLowerCase();
        return article;
    }

    public static String getDefiniteArticle(String noun, boolean lowercase) {
        String article = "The";
        if (lowercase) article = article.toLowerCase();
        return article;
    }


    static List<String> pluralNouns = new ArrayList<>(List.of("taxa"));

    public static String pluralize(String noun) {
        if (!noun.endsWith("s") && !pluralNouns.contains(noun)) return noun + "s";
        return noun;
    }
}
