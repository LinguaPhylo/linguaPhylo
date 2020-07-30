package lphy2beast.tobeast.generators;

import beast.core.BEASTInterface;
import beast.evolution.speciation.YuleModel;
import lphy.evolution.birthdeath.Yule;
import lphy2beast.BEASTContext;
import lphy2beast.GeneratorToBEAST;

public class YuleToBEAST implements GeneratorToBEAST<Yule> {

    @Override
    public BEASTInterface generatorToBEAST(Yule generator, BEASTInterface value, BEASTContext context) {
        YuleModel yuleModel = new YuleModel();

        yuleModel.setInputValue("tree", value);
        yuleModel.setInputValue("birthDiffRate", context.getBEASTObject(generator.getBirthRate()));
        yuleModel.initAndValidate();

        return yuleModel;
    }

    @Override
    public Class<Yule> getGeneratorClass() {
        return Yule.class;
    }
}
