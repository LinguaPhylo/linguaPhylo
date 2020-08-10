package lphybeast.tobeast.generators;

import beast.core.BEASTInterface;
import beast.core.parameter.RealParameter;
import beast.evolution.speciation.BirthDeathGernhard08Model;
import beast.evolution.tree.Tree;
import beast.math.distributions.MRCAPrior;
import beast.math.distributions.Prior;
import lphy.evolution.birthdeath.BirthDeathSamplingTreeDT;
import lphybeast.BEASTContext;
import lphybeast.GeneratorToBEAST;

public class BirthDeathSampleTreeDTToBEAST implements
        GeneratorToBEAST<BirthDeathSamplingTreeDT,BirthDeathGernhard08Model> {

    @Override
    public BirthDeathGernhard08Model generatorToBEAST(BirthDeathSamplingTreeDT generator, BEASTInterface tree, BEASTContext context) {


        BirthDeathGernhard08Model beastBirthDeath = new BirthDeathGernhard08Model();
        beastBirthDeath.setInputValue("birthDiffRate", context.getBEASTObject(generator.getDiversificationRate()));
        beastBirthDeath.setInputValue("relativeDeathRate", context.getBEASTObject(generator.getTurnover()));
        beastBirthDeath.setInputValue("sampleProbability", context.getBEASTObject(generator.getRho()));
        beastBirthDeath.setInputValue("type", "labeled");
        beastBirthDeath.setInputValue("conditionalOnRoot", true);
        beastBirthDeath.setInputValue("tree", tree);
        beastBirthDeath.initAndValidate();

        BEASTInterface beastRootAge = context.getBEASTObject(generator.getRootAge());
        BEASTInterface beastRootAgeGenerator = context.getBEASTObject(generator.getRootAge().getGenerator());

        if (beastRootAge instanceof RealParameter && beastRootAgeGenerator instanceof Prior) {
            RealParameter rootAgeParameter = (RealParameter) beastRootAge;
            Prior rootAgePrior = (Prior) beastRootAgeGenerator;

            MRCAPrior prior = new MRCAPrior();
            prior.setInputValue("distr", rootAgePrior.distInput.get());
            prior.setInputValue("tree", tree);
            prior.setInputValue("taxonset", ((Tree) tree).getTaxonset());
            prior.initAndValidate();
            context.addBEASTObject(prior);
            context.removeBEASTObject(beastRootAge);
            context.removeBEASTObject(beastRootAgeGenerator);
        } else {
            throw new RuntimeException("Can't map BirthDeathSamplingTree.rootAge prior to tree in BEAST conversion.");
        }

        return beastBirthDeath;
    }

    @Override
    public Class<BirthDeathSamplingTreeDT> getGeneratorClass() {
        return BirthDeathSamplingTreeDT.class;
    }

    @Override
    public Class<BirthDeathGernhard08Model> getBEASTClass() {
        return BirthDeathGernhard08Model.class;
    }
}
