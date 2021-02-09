package lphy.graphicalModel;

import lphy.evolution.continuous.PhyloBrownian;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class NarrativeUtils {

    public static String getArticle(Value value, boolean unique) {
        return getArticle(value, unique, false);
    }

    public static String getArticle(Value value, boolean unique, boolean lowercase) {

        String article = lowercase ? "the" : "The";
        if (!unique && value.isAnonymous()) article = getIndefiniteArticle(getName(value), lowercase);
        return article;
    }

    public static String getCitationHyperlink(Generator generator) {
        Citation citation = generator.getCitation();
        if (citation == null) return null;
        StringBuilder builder = new StringBuilder();
        builder.append("<a href=\"");
        builder.append(sanitizeDOI(citation.DOI()));
        builder.append("\">(");
        int count = 0;
        String[] authors = citation.authors();
        for (int i = 0; i < authors.length; i++) {
            if (i > 0) {
                if (i == authors.length-1) {
                    builder.append(" and ");
                } else {
                    builder.append(", ");
                }
            }
            builder.append(authors[i]);
        }
        builder.append("; ");
        builder.append(citation.year());
        builder.append(")</a>");
        return builder.toString();
    }

    public static String sanitizeDOI(String doi) {
        if (doi.startsWith("http")) return doi;
        if (doi.startsWith("doi.org")) return "https://" + doi;
        if ("01923456789".indexOf(doi.charAt(0)) >= 0) return "https://doi.org/" + doi;
        return doi;
    }

    static final Map<String, String> TYPE_MAP = Map.of(
            "Double[]", "Vector",
            "Double[][]", "Matrix",
            "Integer[]", "Vector",
            "Integer[][]", "Vector",
            "TimeTree", "Time Tree"
    );

    private static String sanitizeTypeName(String typeName) {
        String sanitizedTypeName = TYPE_MAP.get(typeName);
        if (sanitizedTypeName != null) return sanitizedTypeName.toLowerCase();
        return typeName.toLowerCase();

    }

    public static String getTypeName(Value value) {
        if (value.getGenerator() != null ) return sanitizeTypeName(value.getGenerator().getTypeName());

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


    public static String getName(Value value) {

        String name;

        if (value.getOutputs().size() == 1 && (value.getOutputs().get(0) instanceof Generator) ) {
            Generator generator = (Generator)value.getOutputs().get(0);
            name = generator.getNarrativeName(value);
        } else name = getTypeName(value);
        return name;
    }


    public static String getValueClause(Value value, boolean unique) {
        return getValueClause(value, unique, false, false);
    }

    public static String getValueClause(Value value, boolean unique, boolean lowercase, boolean plural) {
        StringBuilder builder = new StringBuilder();

        String name = getName(value);

        if (plural) {
            name = pluralize(name);
        }

        if (unique || !name.endsWith("s") || plural) {
            name = getArticle(value, unique, lowercase) + " " + name;
        }
        String firstLetter = name.substring(0,1);
        name = (lowercase ? firstLetter.toLowerCase() : firstLetter.toUpperCase()) + name.substring(1);

        builder.append(name);
        builder.append((value.isAnonymous() ? "" : ", <i>" + value.getId() + "</i>"));
        if (value.getGenerator() == null) {
            builder.append(" of ");
            builder.append(ValueUtils.valueToString(value));
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

    public static String pluralize(String noun) {
        if (!noun.endsWith("s")) return noun + "s";
        return noun;
    }
}
