package lphybeast.tobeast.generators;

import beast.core.BEASTInterface;
import beast.evolution.tree.Tree;
import beast.math.distributions.MRCAPrior;
import beast.math.distributions.Prior;
import lphy.evolution.birthdeath.BirthDeathSerialSamplingTree;
import lphybeast.BEASTContext;
import lphybeast.GeneratorToBEAST;
import treeprior.BirthDeathSequentialSampling;

public class BirthDeathSerialSamplingToBEAST implements
        GeneratorToBEAST<BirthDeathSerialSamplingTree,BirthDeathSequentialSampling> {

    @Override
    public BirthDeathSequentialSampling generatorToBEAST(BirthDeathSerialSamplingTree generator, BEASTInterface tree, BEASTContext context) {

        //           <distribution id="BDSS.TreePrior" spec="treeprior.BirthDeathSequentialSampling" tree="@Tree.t:carnivores" upper="66.0" lower="37.3">
        //          <parameter id="birthRate.t" name="birthRate" spec="parameter.RealParameter" value ="1.0"/>
        //          <parameter id="deathRate.t" name="deathRate" spec="parameter.RealParameter" value="1.0"/>
        //          <parameter id="rho.t" name="rho" spec="parameter.RealParameter" value="0.1"/>
        //          <parameter id="psi.t" name="psi" spec="parameter.RealParameter" value="0.001"/>
        //          </distribution>

        BirthDeathSequentialSampling beastBDSS = new BirthDeathSequentialSampling();
        beastBDSS.setInputValue("birthRate", context.getAsRealParameter(generator.getBirthRate()));
        beastBDSS.setInputValue("deathRate", context.getAsRealParameter(generator.getDeathRate()));
        beastBDSS.setInputValue("rho", context.getAsRealParameter(generator.getRho()));
        beastBDSS.setInputValue("psi", context.getAsRealParameter(generator.getPsi()));
        beastBDSS.setInputValue("tree", tree);
        beastBDSS.initAndValidate();

        BEASTInterface beastRootAge = context.getAsRealParameter(generator.getRootAge());
        BEASTInterface beastRootAgeGenerator = context.getBEASTObject(generator.getRootAge().getGenerator());

        if (beastRootAgeGenerator instanceof Prior) {
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

        return beastBDSS;
    }

    @Override
    public Class<BirthDeathSerialSamplingTree> getGeneratorClass() {
        return BirthDeathSerialSamplingTree.class;
    }

    @Override
    public Class<BirthDeathSequentialSampling> getBEASTClass() {
        return BirthDeathSequentialSampling.class;
    }
}
