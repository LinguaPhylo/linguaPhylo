package lphystudio.app.modelguide;

import lphy.graphicalModel.DeterministicFunction;
import lphy.graphicalModel.Generator;
import lphy.graphicalModel.GeneratorCategory;
import lphy.graphicalModel.GeneratorInfo;
import lphy.util.LoggerUtils;

/**
 * @author Walter Xie
 */
public class Model {

    final GeneratorInfo generatorInfo;
    //TODO this should be removed, GeneratorInfo must be compulsory
    final Class<? extends Generator> generatorClass;

    final boolean isDeterFunc;

    final String htmlDoc;

    public Model(Class<? extends Generator> generatorClass) {
        this.generatorClass = generatorClass;
        this.generatorInfo = Generator.getGeneratorInfo(generatorClass);
        if (generatorInfo == null)
            LoggerUtils.log.severe("Cannot create model from class "+ generatorClass +
                    "\nGeneratorInfo annotation is not found !");

        this.isDeterFunc = generatorClass.isAssignableFrom(DeterministicFunction.class);

        htmlDoc = Generator.getGeneratorHtml(generatorClass);

//        Class<?> cls = Utils.getClass(generatorClass);
//        if (!cls.isAssignableFrom(Generator.class))
//            throw new IllegalArgumentException("The input Class type must be a Generator ! " + generatorClass);



    }

    public String getName() {
        if (generatorInfo ==null) return generatorClass.getSimpleName();
        return generatorInfo.name();
    }

    public String getDescription() {
        if (generatorInfo ==null) return "";
        return generatorInfo.description();
    }

    public GeneratorCategory getCategory() {
        if (generatorInfo ==null) return GeneratorCategory.NONE;
        return generatorInfo.category();
    }

    public boolean isDeterFunc() {
        return isDeterFunc;
    }
}
