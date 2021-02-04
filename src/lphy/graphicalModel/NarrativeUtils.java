package lphy.graphicalModel;

import lphy.app.NarrativePanel;

public class NarrativeUtils {

    public static String getArticle(Value value, boolean unique) {
        return getArticle(value, unique, false);
    }

    public static String getArticle(Value value, boolean unique, boolean lowercase) {

        String article = lowercase ? "the" : "The";
        if (!unique && value.isAnonymous()) article = getIndefiniteArticle(getName(value), lowercase);
        return article;
    }

    public static String getTypeName(Value value) {
        if (value.getGenerator() != null ) return value.getGenerator().getTypeName().toLowerCase();
        return value.getType().getSimpleName().toLowerCase();
    }


    public static String getName(Value value) {

        String name = null;

        if (value.getOutputs().size() == 1 && (value.getOutputs().get(0) instanceof GenerativeDistribution) ) {
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

    public static String getIndefiniteArticle(String noun, boolean lowercase) {
        String article = "A";
        if ("aeiou".indexOf(noun.charAt(0)) >= 0) article = "An";
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
