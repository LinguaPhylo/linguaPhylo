package lphy.core.doc;

import lphy.core.graphicalmodel.components.*;
import net.steppschuh.markdowngenerator.link.Link;
import net.steppschuh.markdowngenerator.list.UnorderedList;
import net.steppschuh.markdowngenerator.text.Text;
import net.steppschuh.markdowngenerator.text.emphasis.BoldText;
import net.steppschuh.markdowngenerator.text.heading.Heading;

import java.lang.reflect.Method;
import java.util.*;

/**
 * A util class to create lphy doc in markdown.
 * @author Alexei Drummond
 * @author Walter Xie
 */
public class GeneratorMarkdown {

    // this is in JEBL lib
    public static final String SEQU_TYPE = "SequenceType";

    static String getGeneratorMarkdown(Class<? extends Generator> generatorClass, final String typesDir) {

        GeneratorInfo generatorInfo = Generator.getGeneratorInfo(generatorClass);

        List<ParameterInfo> pInfo = Generator.getParameterInfo(generatorClass, 0);
        Class[] types = Generator.getParameterTypes(generatorClass, 0);

        StringBuilder md = new StringBuilder();

        StringBuilder signature = new StringBuilder();

        signature.append(Generator.getGeneratorName(generatorClass)).append("(");

        int count = 0;
        for (int i = 0; i < pInfo.size(); i++) {
            ParameterInfo pi = pInfo.get(i);
            if (count > 0) signature.append(", ");
            String typeName = types[i].getSimpleName();
            Link typeLink = new Link(typeName, getTypeURL(typesDir, typeName));
            signature.append(typeLink).append(" ").append(new BoldText(pi.name()));
            count += 1;
        }
        signature.append(")");

        md.append(new Heading(signature.toString(), 2)).append("\n\n");

        if (generatorInfo != null) md.append(generatorInfo.description()).append("\n\n");

        if (pInfo.size() > 0) {
            md.append(new Heading("Parameters", 3)).append("\n\n");
            List<Object> paramText = new ArrayList<>();

            for (int i = 0; i < pInfo.size(); i++) {
                ParameterInfo pi = pInfo.get(i);
                String typeName = types[i].getSimpleName();
                Link typeLink = new Link(typeName,getTypeURL(typesDir, typeName));
                paramText.add(new Text(typeLink + " " + new BoldText(pi.name()) + " - " + pi.description()));
            }
            md.append(new UnorderedList<>(paramText));
        }
        md.append("\n\n");

        md.append(new Heading("Return type", 3)).append("\n\n");

        String returnTypeName = Generator.getReturnType(generatorClass).getSimpleName();
        Link returnTypeLink = new Link(returnTypeName,getTypeURL(typesDir, returnTypeName));
        md.append(returnTypeLink).append("\n\n");

        if (generatorInfo != null){
            String[] examples = generatorInfo.examples();
            if (examples.length > 0) {
                md.append("\n").append(new Heading("Examples",3)).append("\n\n");
                md.append(new UnorderedList<>(Arrays.stream(examples).toList())).append("\n\n");
            }
        }

        Citation citation = Generator.getCitation(generatorClass);
        if (citation != null) {
            md.append(new Heading("Reference", 3)).append("\n\n");
            md.append(citation.value());
            if (citation.DOI().length() > 0) {
                String url = citation.DOI();
                if (!url.startsWith("http")) {
                    url = "http://doi.org/" + url;
                }
                md.append(new Link(url, url));
            }
        }
        return md.toString();
    }

    /**
     * @param typesDir   the dir name where all types are
     * @param name       type md file name without .md
     * @return    the link to this type md file.
     */
    private static String getTypeURL(final String typesDir, final String name) {
        // the working dir should be where index.md is
        return "../" + typesDir + "/" + name + ".md";
    }
//    private static String getTypeURL(final String typesDir, final String name) {
//        // the working dir should be where index.md is
//        String url = typesDir + "/" + name + ".md";
//        // check if file exists, if no then add repo link
//        File md = new File(url);
//        if (!md.exists())
//            url = "https://github.com/LinguaPhylo/linguaPhylo/blob/master/lphy/doc/types/" + name + ".md";
//        else url = "../" + url; // otherwise, add ../ because the link is based on where the $name.md file is.
//        return url;
//    }

    static String generateTypeMarkdown(Class<?> type) {
        StringBuilder builder = new StringBuilder();

        String typeName = type.getSimpleName();
        builder.append(new Heading(typeName,2)).append("\n");

        // from TypeInfo
        TypeInfo typeInfo = type.getAnnotation(TypeInfo.class);
        String desc;
        String[] examples = new String[0];
        if (typeInfo == null) {
            desc = getTypeDescription(typeName);
        } else {
            desc = typeInfo.description();
            examples = typeInfo.examples();
        }
        if (!desc.isEmpty())
            builder.append("\n").append(new Text(desc)).append("\n\n");

        TreeMap<String, MethodInfo> methodInfoTreeMap = new TreeMap<>();
        for (Method method : type.getMethods()) {
            MethodInfo methodInfo = method.getAnnotation(MethodInfo.class);
            if (methodInfo != null) {
                methodInfoTreeMap.put(method.getName(), methodInfo);
            }
        }
        if (methodInfoTreeMap.size() > 0) {
            builder.append(new Heading("Methods",3)).append("\n\n");
            List<Object> methodText = new ArrayList<>();

            for (Map.Entry<String,MethodInfo> methodInfoEntry : methodInfoTreeMap.entrySet()) {
                methodText.add(new BoldText(methodInfoEntry.getKey()) + "\n  - " + methodInfoEntry.getValue().description());
            }
            builder.append(new UnorderedList<>(methodText));
        }

        // Examples
        if (examples.length > 0) {
            builder.append("\n").append(new Heading("Examples",3)).append("\n\n");
            builder.append(new UnorderedList<>(Arrays.stream(examples).toList())).append("\n\n");
        }

        return builder.toString();
    }

    static String generateSequenceTypeMarkdown() {
        StringBuilder builder = new StringBuilder();

        builder.append(new Heading(SEQU_TYPE,2)).append("\n");
        final String desc = "Sequences data types, such as nucleotide, amino acid, binary, " +
                "standard type (e.g. morphology, locations, traits, ...), etc.";
        builder.append("\n").append(new Text(desc)).append("\n\n");
        final String seealso = "https://github.com/LinguaPhylo/linguaPhylo/tree/master/lphy/doc/sequence-type";
        builder.append("\n").append(new Text("See also: " + seealso)).append("\n\n");

        return builder.toString();
    }

    private static String getTypeDescription(String name) {
        return switch (name) {
            case "Boolean"     -> "The [Boolean](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/lang/Boolean.html) data type wraps a boolean value, such as true or false.";
            case "Boolean[]"   -> "The [Boolean](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/lang/Boolean.html) array.";
            case "Double"      -> "The [Double](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/lang/Double.html) data type wraps a real number, such as 0.1.";
            case "Double[]"    -> "The [Double](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/lang/Double.html) data type array.";
            case "Double[][]"  -> "The [Double](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/lang/Double.html) data type 2-d array.";
            case "Integer"     -> "The [Integer](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/lang/Integer.html) data type wraps an integer number, such as 1.";
            case "Integer[]"   -> "The [Integer](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/lang/Integer.html) data type array.";
            case "Integer[][]" -> "The [Integer](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/lang/Integer.html) data type 2-d array.";
            case "Number"      -> "The [Number](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/lang/Number.html) is the superclass of data type wrapping numeric values, such as Double or Integer.";
            case "Number[]"    -> "The [Number](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/lang/Number.html) data type array.";
            case "Number[][]"  -> "The [Number](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/lang/Number.html) data type 2-d array.";
            case "Object"      -> "It could be any data type or class.";
            case "Object[]"    -> "The Object array.";
            case "Object[][]"  -> "The Object 2-d array.";
            case "String"      -> "The [String](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/lang/String.html) data type represents character strings, such as \"lphy\".";
            case "String[]"    -> "The [String](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/lang/String.html) data type array.";
            case "String[][]"  -> "The [String](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/lang/String.html) data type 2-d array.";
            default            -> "";
        };
    }

}
