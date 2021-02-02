package lphy.graphicalModel;

import lphy.parser.functions.MethodCall;

public class NarrativeUtils {

    public static String getArticle(Value value, boolean unique) {
        return getArticle(value, unique, false);
    }

    public static String getArticle(Value value, boolean unique, boolean lowercase) {

        String article = lowercase ? "the" : "The";
        if (!unique) article = getIndefiniteArticle(getName(value), lowercase);
        return article;
    }

    public static String getTypeName(Value value) {
        if (value.getGenerator() != null ) return value.getGenerator().getTypeName().toLowerCase();
        return value.getType().getSimpleName().toLowerCase();
    }


    public static String getName(Value value) {
        if (value.getOutputs().size() == 1) {
            Generator generator = (Generator)value.getOutputs().get(0);

            if (MethodCall.isMethodCall(generator)) {
                MethodCall methodCall = (MethodCall)generator;
                if (methodCall.getParams().get("object").equals(value)) {
                    return getTypeName(value);
                }
            }

            return generator.getNarrativeName(value);
        } else return getTypeName(value);
    }


    public static String getValueClause(Value value, boolean unique) {
        return getValueClause(value, unique, false);
    }

    public static String getValueClause(Value value, boolean unique, boolean lowercase) {
        StringBuilder builder = new StringBuilder();

        String name = getName(value);
        if (unique || !name.endsWith("s")) {
            name = getArticle(value, unique, lowercase) + " " + name;
        }
        String firstLetter = name.substring(0,1);
        name = (lowercase ? firstLetter.toLowerCase() : firstLetter.toUpperCase()) + name.substring(1);

        builder.append(name);
        builder.append((value.isAnonymous() ? "" : " (" + value.getId() + ")"));
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
}
