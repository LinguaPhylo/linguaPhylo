package lphy.core.narrative;

import lphy.core.exception.LoggerUtils;
import lphy.core.model.*;
import lphy.core.model.annotation.Citation;
import lphy.core.model.annotation.GeneratorInfo;
import lphy.core.model.annotation.ParameterInfo;
import lphy.core.parser.argument.ArgumentUtils;
import lphy.core.parser.graphicalmodel.GraphicalModel;
import lphy.core.parser.graphicalmodel.GraphicalModelNodeVisitor;
import lphy.core.parser.graphicalmodel.ValueCreator;

import java.lang.annotation.Annotation;
import java.util.*;
import java.util.stream.Collectors;

import static lphy.core.model.GeneratorUtils.*;

public class NarrativeUtils {

    private static int wrapLength = 80;

    public static String getArticle(Value value, boolean unique) {
        return getArticle(value, getName(value), unique, false);
    }

    public static String getArticle(Value value, String name, boolean unique, boolean lowercase) {

        //String article = lowercase ? "the" : "The";
        String article = lowercase ? "" : "The";
        if (!unique && value.isAnonymous()) article = getIndefiniteArticle(name, lowercase);
        return article;
    }


    public static String getName(Value value) {
        // empty array
//        if (value == null) return "";

        String name;

        if (GeneratorUtils.hasSingleGeneratorOutput(value)) {
            Generator generator = (Generator) value.getOutputs().get(0);
            name = generator.getNarrativeName(value);
            if (name == null || name.equals("")) {
                if (value.value() != null && value.value() instanceof NarrativeName val) {
                    name = val.getNarrativeName();
                } else name = TypeNameUtils.getTypeName(value);
            }
        } else if (value.value() != null && value.value() instanceof NarrativeName val) {
            name = val.getNarrativeName();
        } else name = TypeNameUtils.getTypeName(value);
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

    public static String getNarrative(GraphicalModel model, Narrative narrative, boolean data, boolean includeModelBlock) {

        Map<String, Integer> nameCounts = new HashMap<>();

        List<Value> dataVisited = new ArrayList<>();
        List<Value> modelVisited = new ArrayList<>();

        StringBuilder builder = new StringBuilder();
        for (Value value : model.getModelSinks()) {

            ValueCreator.traverseGraphicalModel(value, new GraphicalModelNodeVisitor() {
                @Override
                public void visitValue(Value value) {

                    if (model.inDataBlock(value)) {
                        if (!dataVisited.contains(value)) {
                            dataVisited.add(value);

                            String name = getName(value);
                            if (!value.isAnonymous() && !model.isClamped(value.getId())) {
                                nameCounts.merge(name, 1, Integer::sum);
                            }
                        }
                    } else {
                        if (!modelVisited.contains(value)) {
                            modelVisited.add(value);
                            String name = getName(value);
                            nameCounts.merge(name, 1, Integer::sum);
                        }
                    }
                }

                public void visitGenerator(Generator generator) {
                }
            }, false);
        }

        if (dataVisited.size() > 0 && data) {
            builder.append(narrative.section("Data"));
            for (Value dataValue : dataVisited) {

                String name = getName(dataValue);
                Integer count = nameCounts.get(name);
                if (count != null) {
                    String valueNarrative = getValueNarrative(dataValue, narrative, count == 1);
                    builder.append(valueNarrative);
                    if (valueNarrative.length() > 0) builder.append("\n");
                } else {
                    //TODO need log4j or similar to disable debug messages after release
//                        LoggerUtils.log.info("No name count found for " + dataValue + " with name " + name);
                }

            }
            builder.append("\n\n");
        }
        if (modelVisited.size() > 0 && includeModelBlock) {
            builder.append(narrative.section("Model"));

            for (Value modelValue : modelVisited) {

                String name = getName(modelValue);
                Integer count = nameCounts.get(name);

                if (count != null) {
                    String valueNarrative = getValueNarrative(modelValue, narrative, count == 1);
                    builder.append(valueNarrative);
                    if (valueNarrative.length() > 0) builder.append("\n");
                } else {
                    LoggerUtils.log.info("No name count found for " + modelValue + " with name " + name);
                }
            }
            builder.append("\n");
        }

        return builder.toString();
    }

    // it was String getNarrative(boolean unique, Narrative narrative) in Value
    private static String getValueNarrative(Value value, Narrative narrative, boolean unique) {
        if (value.getGenerator() != null) {
            return value.getGenerator().getInferenceNarrative(value, unique, narrative);
        } else {
            if (!value.isAnonymous()) return value.toString();
            return "";
        }
    }

    public static String getInferenceStatement(GraphicalModel model, Narrative narrative) {

        List<Value> modelVisited = new ArrayList<>();
        List<Value> dataValues = new ArrayList<>();

        StringBuilder builder = new StringBuilder();
        for (Value value : model.getModelSinks()) {

            ValueCreator.traverseGraphicalModel(value, new GraphicalModelNodeVisitor() {
                @Override
                public void visitValue(Value value) {

                    if (!model.isNamedDataValue(value)) {
                        if (!modelVisited.contains(value)) {
                            modelVisited.add(value);
                            if (model.isClamped(value.getId()) || value.getOutputs().size() == 0) {
                                dataValues.add(value);
                            }
                        }
                    }
                }

                public void visitGenerator(Generator generator) {
                }
            }, false);
        }

        if (modelVisited.size() > 0) {

            builder.append(narrative.startMathMode(false, true));

            builder.append("P(");
            int count = 0;
            for (Value modelValue : modelVisited) {
                if (!dataValues.contains(modelValue) && modelValue instanceof RandomVariable) {
                    if (count > 0) builder.append(", ");
                    String name = narrative.getId(modelValue, false);
                    builder.append(name);
                    count += 1;
                }
            }
            if (dataValues.size() > 0) builder.append(" | ");
            count = 0;
            for (Value dataValue : dataValues) {

                String name = narrative.getId(dataValue, false);
                if (count > 0 && name != null) builder.append(", ");

                if (name != null) builder.append(name);
                count += 1;
            }
            builder.append(") ");
            builder.append(narrative.symbol("∝"));
            builder.append(" ");
            builder.append(narrative.mathAlign());


            int currentLineLength = 0;

            List<RandomVariable> randomVariables = modelVisited.stream().filter(value -> value instanceof RandomVariable).map(value -> (RandomVariable) value).collect(Collectors.toList());

            for (int i = 0; i < randomVariables.size(); i++) {
                RandomVariable modelVariable = randomVariables.get(i);
                String statement = modelVariable.getGenerator().getInferenceStatement(modelVariable, narrative);
                builder.append(statement);
                currentLineLength += statement.length();

                if (currentLineLength > wrapLength && i < modelVisited.size() - 1) {
                    builder.append(narrative.mathNewLine());
                    builder.append(narrative.mathAlign());
                    currentLineLength = 0;
                    builder.append(" ");
                }
            }

            builder.append(narrative.endMathMode());


            builder.append("\n");
        }

        return builder.toString();
    }

    public static String getGeneratorInferenceStatement(Generator generator, Value value, Narrative narrative) {
        StringBuilder builder = new StringBuilder();

        builder.append("P(");

        String name = narrative.getId(value, false);

        builder.append(name);
        Map<String, Value> params = generator.getParams();

        List<ParameterInfo> parameterInfos = generator.getParameterInfo(0);
        int count = 0;
        for (ParameterInfo parameterInfo : parameterInfos) {
            Value v = params.get(parameterInfo.name());
            if (v != null && v.isRandom()) {

                if (count == 0) {
                    builder.append(" | ");
                } else {
                    builder.append(", ");
                }

                if (v.isAnonymous()) {
                    builder.append(parameterInfo.name());
                } else {

                    name = narrative.getId(v, false);
                    builder.append(name);
                }
                count += 1;
            }
        }
        builder.append(")");

        return builder.toString();
    }

    public static Class<?>[] getParameterTypes(Class<? extends Generator> c, int constructorIndex) {
        return ArgumentUtils.getParameterTypes(c.getConstructors()[constructorIndex]);
    }

    public static String getGeneratorHtml(Class<? extends Generator> generatorClass) {
        GeneratorInfo generatorInfo = getGeneratorInfo(generatorClass);

        List<ParameterInfo> pInfo = getParameterInfo(generatorClass, 0);
        Class[] types = getParameterTypes(generatorClass, 0);

        // parameters
        StringBuilder signature = new StringBuilder();
        signature.append(getGeneratorName(generatorClass)).append("(");

        int count = 0;
        for (int i = 0; i < pInfo.size(); i++) {
            ParameterInfo pi = pInfo.get(i);
            if (count > 0) signature.append(", ");
            signature //.append(types[i].getSimpleName()).append(" ")
                    .append("<i>").append(pi.name()).append("</i>");
            count += 1;
        }
        signature.append(")");

        // main content
        StringBuilder html = new StringBuilder("<html><h2>");
        // check if deprecated
        Annotation a = generatorClass.getAnnotation(Deprecated.class);
        if (a != null) html.append("<s>");
        html.append(signature);
        if (a != null) html.append("</s>").append("<font color=\"#ff0000\">")
                .append(" @Deprecated").append("</font>");
        html.append("</h2>");

        if (generatorInfo != null) html.append("<p>").append(generatorInfo.description()).append("</p>");

        if (pInfo.size() > 0) {
            html.append("<h3>Parameters:</h3>").append("<ul>");
//            int count = 0;
            for (int i = 0; i < pInfo.size(); i++) {
                ParameterInfo pi = pInfo.get(i);
                html.append("<li>").append(types[i].getSimpleName()).
                        append(" <b>").append(pi.name()).append("</b>")
                        .append(" - <font color=\"#808080\">")
                        .append(pi.description()).append("</font></li>");

//                if (count > 0) signature.append(", ");
//                signature.append(new Text(types[i].getSimpleName())).append(" ").append(new BoldText(pi.name()));
//                count += 1;
            }
//            signature.append(")");
//            html.append(new Heading(signature.toString(), 2)).append("\n\n");
            html.append("</ul>");
        }

        List<String> returnType = Collections.singletonList(getReturnType(generatorClass).getSimpleName());
        if (returnType.size() > 0) {
            html.append("<h3>Return type:</h3>").append("<ul>");
            for (String itm : returnType)
                html.append("<li>").append(itm).append("</li>");
            html.append("</ul>");
        }

        Citation citation = CitationUtils.getCitation(generatorClass);
        if (citation != null) {
            html.append("<h3>Reference</h3>");
            html.append(citation.value());
            if (!citation.value().endsWith(".")) html.append(".");
            String url = CitationUtils.getURL(citation);
            if (url.length() > 0)
                html.append("&nbsp;<a href=\"").append(url).append("\">").append(url).append("</a><br>");
        }

        String[] examples = getGeneratorExamples(generatorClass);
        if (examples.length > 0) {
            html.append("<h3>Examples</h3>");
            for (int i = 0; i < examples.length; i++) {
                String ex = examples[i];
                // add hyperlink
                if (ex.startsWith("http"))
                    ex = "&nbsp;<a href=\"" + ex + "\">" + ex + "</a>";
                html.append(ex);
                if (i < examples.length - 1)
                    html.append(", ");
            }
        }

        html.append("</html>");
        return html.toString();
    }
}
