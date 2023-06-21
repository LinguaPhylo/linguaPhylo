package lphystudio.app.modelguide;

import lphy.core.logger.LoggerUtils;
import lphy.core.model.DeterministicFunction;
import lphy.core.model.Generator;
import lphy.core.model.GeneratorUtils;
import lphy.core.model.NarrativeUtils;
import lphy.core.model.annotation.*;
import lphy.core.parser.function.MethodCall;

import java.util.TreeMap;

/**
 * @author Walter Xie
 */
public class Model {

    private final String name;
    private String narrativeName;
    private String description;
    private GeneratorCategory category;
    private String[] examples;

//    final Class<?> cls;
    boolean isDeterFunc;

    String htmlDoc;

    Citation citation; // TODO multiple?

    public Model(Class<?> cls) {
        if (Generator.class.isAssignableFrom(cls)) {
             GeneratorInfo generatorInfo = GeneratorUtils.getGeneratorInfo(cls);
            // GeneratorInfo must be compulsory
            if (generatorInfo == null) {
                LoggerUtils.log.severe("Cannot create model from class "+ cls +
                        "\nGeneratorInfo annotation is not found !");
                name = cls.getSimpleName();
            } else {
                name = generatorInfo.name();
                narrativeName = generatorInfo.narrativeName();
                description = generatorInfo.description();
                category = generatorInfo.category();
                examples = generatorInfo.examples();
            }

            this.isDeterFunc = cls.isAssignableFrom(DeterministicFunction.class);
            // cls is Generator
            htmlDoc = NarrativeUtils.getGeneratorHtml((Class<? extends Generator>) cls);

        } else { // MethodInfo

            name = cls.getSimpleName();
            description = "Method calls";
            TreeMap<String, MethodInfo> methodInfoTreeMap = MethodCall.getMethodCalls(cls);
            category = MethodCall.getCategory(methodInfoTreeMap);
            examples = MethodCall.getExamples(methodInfoTreeMap);
            htmlDoc = MethodCall.getHtmlDoc(name, methodInfoTreeMap, examples);

        }
        // can be null
        citation = CitationUtils.getCitation(cls);

    }

    public String getName() {
        return name;
    }

    public String getNarrativeName() {
        if (narrativeName == null) return "";
        return narrativeName;
    }

    public String getDescription() {
        if (description == null) return "";
        return description;
    }

    public GeneratorCategory getCategory() {
        if (category ==null) return GeneratorCategory.NONE;
        return category;
    }

    public String[] getExamples() {
        if (examples ==null) examples = new String[]{""};
        return examples;
    }

    public Citation getCitation() {
        return citation;
    }
}
