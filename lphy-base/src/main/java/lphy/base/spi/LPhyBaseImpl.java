package lphy.base.spi;

import lphy.base.bmodeltest.BModelSetFunction;
import lphy.base.bmodeltest.NucleotideModel;
import lphy.base.bmodeltest.bSiteModelFunction;
import lphy.base.bmodeltest.bSiteRates;
import lphy.base.distributions.*;
import lphy.base.evolution.alignment.*;
import lphy.base.evolution.birthdeath.*;
import lphy.base.evolution.branchrates.LocalBranchRates;
import lphy.base.evolution.coalescent.MultispeciesCoalescent;
import lphy.base.evolution.coalescent.SerialCoalescent;
import lphy.base.evolution.coalescent.SkylineCoalescent;
import lphy.base.evolution.coalescent.StructuredCoalescent;
import lphy.base.evolution.continuous.PhyloBrownian;
import lphy.base.evolution.continuous.PhyloMultivariateBrownian;
import lphy.base.evolution.continuous.PhyloOU;
import lphy.base.evolution.likelihood.PhyloCTMC;
import lphy.base.evolution.likelihood.PhyloCTMCSiteModel;
import lphy.base.evolution.substitutionmodel.*;
import lphy.base.functions.*;
import lphy.base.functions.alignment.NCharFunction;
import lphy.base.functions.alignment.ReadFasta;
import lphy.base.functions.alignment.ReadNexus;
import lphy.base.functions.datatype.AminoAcidsFunction;
import lphy.base.functions.datatype.BinaryDatatypeFunction;
import lphy.base.functions.datatype.NucleotidesFunction;
import lphy.base.functions.datatype.StandardDatatypeFunction;
import lphy.base.functions.taxa.*;
import lphy.base.functions.tree.ExtantTree;
import lphy.base.functions.tree.MigrationCount;
import lphy.base.functions.tree.Newick;
import lphy.base.functions.tree.PruneTree;
import lphy.core.model.components.Func;
import lphy.core.model.components.GenerativeDistribution;
import lphy.core.spi.LPhyExtension;

import java.util.Arrays;
import java.util.List;

/**
 * The "Container" provider class that implements SPI
 * which include a list of {@link GenerativeDistribution}, {@link Func} to extend.
 * It requires a public no-args constructor.
 * @author Walter Xie
 */
public class LPhyBaseImpl implements LPhyExtension {

    static {
        SequenceTypeLoader.getInstance();
    }

    List<Class<? extends GenerativeDistribution>> genDists = Arrays.asList(
            // probability distribution
            Bernoulli.class, BernoulliMulti.class, Beta.class, Cauchy.class, Dirichlet.class,
            UniformDiscrete.class, DiscretizedGamma.class, Exp.class, Gamma.class, Geometric.class,
            InverseGamma.class, LogNormal.class, Normal.class, NormalGamma.class, Poisson.class,
            NegativeBinomial.class, Uniform.class, WeightedDirichlet.class, Weibull.class, Categorical.class,
            // tree distribution
            Yule.class, BirthDeathTree.class, FullBirthDeathTree.class, BirthDeathTreeDT.class,
            BirthDeathSamplingTree.class, BirthDeathSamplingTreeDT.class, BirthDeathSerialSamplingTree.class,
            RhoSampleTree.class, FossilBirthDeathTree.class,
            SimBDReverse.class, SimFBDAge.class, SimFossilsPoisson.class,
            SerialCoalescent.class, StructuredCoalescent.class, MultispeciesCoalescent.class,
            // skyline
            SkylineCoalescent.class, ExpMarkovChain.class, RandomComposition.class,
            // alignment
            Sequence.class, ErrorModel.class, MissingSites.class,
            // others
            RandomBooleanArray.class, Sample.class,
            // phylogenetic distribution
            PhyloBrownian.class, PhyloMultivariateBrownian.class, PhyloOU.class,
            PhyloCTMC.class, PhyloCTMCSiteModel.class, bSiteRates.class);

    List<Class<? extends Func>> functions = Arrays.asList(ARange.class, ArgI.class,
            // Substitution models
            JukesCantor.class, K80.class, F81.class, HKY.class, GTR.class, WAG.class,
            GeneralTimeReversible.class, LewisMK.class,
            NucleotideModel.class,
            BModelSetFunction.class,
            bSiteModelFunction.class,

            // Data types
            BinaryDatatypeFunction.class, NucleotidesFunction.class, StandardDatatypeFunction.class,
            AminoAcidsFunction.class,
            // Taxa
            CreateTaxa.class, ExtantTaxa.class, NCharFunction.class, NTaxaFunction.class, TaxaFunction.class,
            // Alignment
            SelectSitesByMissingFraction.class,
            VariableSites.class, InvariableSites.class, CopySites.class,
            // Tree
            LocalBranchRates.class, ExtantTree.class, PruneTree.class, //NodeCount.class, TreeLength.class,
            // Matrix
            BinaryRateMatrix.class, MigrationMatrix.class, MigrationCount.class,
            // IO
            Newick.class, ReadNexus.class, ReadFasta.class, ExtractTrait.class, Species.class,
            // Math
            SumBoolean.class, SumRows.class, SumCols.class, Sum2dArray.class, Sum.class,// Product.class,
            // Set Op
            Intersect.class,
            // Utils
            Length.class, Unique.class, Sort.class, IfElse.class, //ConcatStr.class,
            Get.class, Select.class, Split.class, ParseInt.class, Rep.class, RepArray.class,  //Copy.class,
            ConcatArray.class, Concat2Str.class);

    /**
     * Required by ServiceLoader.
     */
    public LPhyBaseImpl() {
        //TODO do something here, e.g. print package or classes info ?
    }

    @Override
    public List<Class<? extends GenerativeDistribution>> getDistributions() {
        return genDists;
    }

    @Override
    public List<Class<? extends Func>> getFunctions() {
        return functions;
    }

}
