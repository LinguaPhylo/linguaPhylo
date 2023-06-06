package lphy.core.narrative;

import lphy.core.model.Value;

import java.util.Map;

public class TypeNameUtils {
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
}
