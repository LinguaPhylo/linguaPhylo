package lphystudio.app.modelguide;

import lphy.graphicalModel.DeterministicFunction;
import lphy.graphicalModel.Generator;
import lphy.graphicalModel.GeneratorCategory;
import lphy.graphicalModel.GeneratorInfo;
import lphy.parser.functions.MethodCall;
import lphy.util.LoggerUtils;

/**
 * @author Walter Xie
 */
public class Model {

    private final String name;
    private String description;
    private GeneratorCategory category;
    private String[] examples;

//    final Class<?> cls;
    boolean isDeterFunc;

    String htmlDoc;

    public Model(Class<?> cls) {
        if (Generator.class.isAssignableFrom(cls)) {
             GeneratorInfo generatorInfo = Generator.getGeneratorInfo(cls);
            // GeneratorInfo must be compulsory
            if (generatorInfo == null) {
                LoggerUtils.log.severe("Cannot create model from class "+ cls +
                        "\nGeneratorInfo annotation is not found !");
                name = cls.getSimpleName();
            } else {
                name = generatorInfo.name();
                description = generatorInfo.description();
                category = generatorInfo.category();
                examples = generatorInfo.examples();
            }

            this.isDeterFunc = cls.isAssignableFrom(DeterministicFunction.class);
            // cls is Generator
            htmlDoc = Generator.getGeneratorHtml((Class<? extends Generator>) cls);
        } else { // MethodInfo

            name = cls.getSimpleName();
            description = "Method calls";
            htmlDoc = MethodCall.getHtmlDoc(cls);

        }

    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        if (description == null) return "";
        return description;
    }

    public GeneratorCategory getCategory() {
        if (category ==null) return GeneratorCategory.NONE;
        return category;
    }

    public boolean isDeterFunc() {
        return isDeterFunc;
    }

    public String[] getExamples() {
        if (examples ==null) examples = new String[]{""};
        return examples;
    }
}
