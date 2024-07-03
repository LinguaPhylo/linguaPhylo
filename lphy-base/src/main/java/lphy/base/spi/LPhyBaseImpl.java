package lphy.base.spi;

import lphy.base.bmodeltest.BModelSetFunction;
import lphy.base.bmodeltest.NucleotideModel;
import lphy.base.bmodeltest.bSiteModelFunction;
import lphy.base.bmodeltest.bSiteRates;
import lphy.base.distribution.*;
import lphy.base.evolution.alignment.ErrorModel;
import lphy.base.evolution.alignment.Sequence;
import lphy.base.evolution.birthdeath.*;
import lphy.base.evolution.branchrate.LocalBranchRates;
import lphy.base.evolution.branchrate.LocalClock;
import lphy.base.evolution.coalescent.*;
import lphy.base.evolution.coalescent.populationmodel.*;
import lphy.base.evolution.continuous.PhyloBrownian;
import lphy.base.evolution.continuous.PhyloMultivariateBrownian;
import lphy.base.evolution.continuous.PhyloOU;
import lphy.base.evolution.likelihood.PhyloCTMC;
import lphy.base.evolution.likelihood.PhyloCTMCSiteModel;
import lphy.base.evolution.substitutionmodel.*;
import lphy.base.evolution.tree.*;
import lphy.base.function.*;
import lphy.base.function.alignment.*;
import lphy.base.function.datatype.AminoAcidsFunction;
import lphy.base.function.datatype.BinaryDatatypeFunction;
import lphy.base.function.datatype.NucleotidesFunction;
import lphy.base.function.datatype.StandardDatatypeFunction;
import lphy.base.function.io.*;
import lphy.base.function.taxa.*;
import lphy.base.function.tree.ExtantTree;
import lphy.base.function.tree.MigrationCount;
import lphy.base.function.tree.Newick;
import lphy.base.function.tree.PruneTree;
import lphy.core.model.BasicFunction;
import lphy.core.model.GenerativeDistribution;
import lphy.core.spi.LPhyCoreImpl;

import java.util.Arrays;
import java.util.List;

/**
 * The "Container" provider class that implements SPI
 * which include a list of {@link GenerativeDistribution}, {@link BasicFunction} to extend.
 * It requires a public no-args constructor.
 * @author Walter Xie
 */
public class LPhyBaseImpl extends LPhyCoreImpl { //implements LPhyExtension {

    @Override
    public List<Class<? extends GenerativeDistribution>> declareDistributions() {
        return Arrays.asList(
                // probability distribution
                Bernoulli.class, BernoulliMulti.class, Beta.class, Categorical.class, Cauchy.class, Dirichlet.class,
                DiscretizedGamma.class, Exp.class, Gamma.class, Geometric.class, InverseGamma.class, LogNormal.class,
                NegativeBinomial.class, Normal.class, NormalGamma.class, Poisson.class,
                Uniform.class, UniformDiscrete.class, Weibull.class, WeightedDirichlet.class,
                Multinomial.class,
                // tree distribution
                Yule.class, CalibratedYule.class, BirthDeathTree.class, FullBirthDeathTree.class, BirthDeathTreeDT.class,
                BirthDeathSamplingTree.class, BirthDeathSamplingTreeDT.class, BirthDeathSerialSamplingTree.class,
                RhoSampleTree.class, FossilBirthDeathTree.class,
                SimBDReverse.class, SimFBDAge.class, SimFossilsPoisson.class,
                SerialCoalescent.class, StructuredCoalescent.class, MultispeciesCoalescent.class,
                SubsampledTree.class, PopulationFunctionCoalescent.class,
                SampleBranch.class,
                // skyline
                SkylineCoalescent.class, ExpMarkovChain.class, RandomComposition.class,
                // alignment
                Sequence.class, ErrorModel.class, MissingSites.class,
                // others
                RandomBooleanArray.class, Sample.class,
                // phylogenetic distribution
                PhyloBrownian.class, PhyloMultivariateBrownian.class, PhyloOU.class,
                PhyloCTMC.class, PhyloCTMCSiteModel.class, bSiteRates.class);
    }

    @Override
    public List<Class<? extends BasicFunction>> declareFunctions() {
        return Arrays.asList(ARange.class, ArgI.class,
                // Substitution models
                JukesCantor.class, K80.class, F81.class, HKY.class, GTR.class, WAG.class,
                GeneralTimeReversible.class, LewisMK.class, BinaryCovarion.class,
                BModelSetFunction.class, bSiteModelFunction.class, NucleotideModel.class,

                // Data types
                BinaryDatatypeFunction.class, NucleotidesFunction.class, StandardDatatypeFunction.class,
                AminoAcidsFunction.class,
                // Taxa
                CreateTaxa.class, ExtantTaxa.class, NCharFunction.class, NTaxaFunction.class, TaxaFunction.class,
                // Alignment
                SelectSitesByMissingFraction.class, RemoveTaxa.class,
                VariableSites.class, InvariableSites.class, CopySites.class,
                // Tree
                LocalBranchRates.class, ExtantTree.class, PruneTree.class, LocalClock.class,
                SubstituteClade.class, MRCA.class, LabelClade.class,//NodeCount.class, TreeLength.class,
                // Matrix
                BinaryRateMatrix.class, MigrationMatrix.class, MigrationCount.class,
                // IO
                Newick.class, ReadNexus.class, ReadFasta.class, ReadDelim.class, WriteFasta.class,
                ReadTrees.class, ExtractTrait.class, SpeciesTaxa.class,
                // Math
                SumBoolean.class, SumRows.class, SumCols.class, Sum2dArray.class, Sum.class, Difference.class, Union.class,// Product.class,
                // Set Op
                Intersect.class,
                // cast
                ToDouble.class,
                // Utils
                Length.class, Unique.class, Sort.class, IfElse.class, //ConcatStr.class,
                Get.class, Select.class, Split.class, ParseInt.class, Rep.class, RepArray.class,  //Copy.class,
                ConcatArray.class, Concat2Str.class,
                // Population function
                GompertzPopulationFunction_f0.class, GompertzPopulationFunction_t50.class, ExponentialPopulationFunction.class, LogisticPopulationFunction.class, ConstantPopulationFunction.class

        );

    }

    /**
     * Required by ServiceLoader.
     */
    public LPhyBaseImpl() {
        //TODO do something here, e.g. print package or classes info ?
    }


//    @Override
//    public void register() {
//
//    }
//
//    @Override
//    public Map<String, Set<Class<?>>> getDistributions() {
//        return null;
//    }
//
//    @Override
//    public Map<String, Set<Class<?>>> getFunctions() {
//        return null;
//    }
//
//    @Override
//    public TreeSet<Class<?>> getTypes() {
//        return null;
//    }

    public String getExtensionName() {
        return "LPhy standard library";
    }
}
