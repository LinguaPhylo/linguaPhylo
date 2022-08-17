package lphy.spi;

import jebl.evolution.sequences.SequenceType;
import lphy.bmodeltest.BModelSetFunction;
import lphy.bmodeltest.NucleotideModel;
import lphy.bmodeltest.bSiteModelFunction;
import lphy.bmodeltest.bSiteRates;
import lphy.core.distributions.Exp;
import lphy.core.distributions.*;
import lphy.core.functions.*;
import lphy.core.functions.alignment.NCharFunction;
import lphy.core.functions.alignment.ReadFasta;
import lphy.core.functions.alignment.ReadNexus;
import lphy.core.functions.datatype.AminoAcidsFunction;
import lphy.core.functions.datatype.BinaryDatatypeFunction;
import lphy.core.functions.datatype.NucleotidesFunction;
import lphy.core.functions.datatype.StandardDatatypeFunction;
import lphy.core.functions.taxa.*;
import lphy.core.functions.tree.*;
import lphy.evolution.alignment.ErrorModel;
import lphy.evolution.birthdeath.*;
import lphy.evolution.branchrates.LocalBranchRates;
import lphy.evolution.coalescent.MultispeciesCoalescent;
import lphy.evolution.coalescent.SerialCoalescent;
import lphy.evolution.coalescent.SkylineCoalescent;
import lphy.evolution.coalescent.StructuredCoalescent;
import lphy.evolution.continuous.PhyloBrownian;
import lphy.evolution.continuous.PhyloMultivariateBrownian;
import lphy.evolution.continuous.PhyloOU;
import lphy.evolution.datatype.Binary;
import lphy.evolution.datatype.Continuous;
import lphy.evolution.datatype.SequenceTypeFactory;
import lphy.evolution.likelihood.PhyloCTMC;
import lphy.evolution.likelihood.PhyloCTMCSiteModel;
import lphy.evolution.substitutionmodel.*;
import lphy.graphicalModel.Func;
import lphy.graphicalModel.GenerativeDistribution;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The "Container" provider class that implements SPI
 * which include a list of {@link GenerativeDistribution}, {@link Func},
 * and {@link SequenceType} to extend.
 * It requires a public no-args constructor.
 * @author Walter Xie
 */
public class LPhyExtImpl implements LPhyExtension {

    List<Class<? extends GenerativeDistribution>> genDists = Arrays.asList(
            // probability distribution
            Bernoulli.class, BernoulliMulti.class, Beta.class, Cauchy.class, Dirichlet.class,
            DiscreteUniform.class, DiscretizedGamma.class, Exp.class, Gamma.class, Geometric.class,
            InverseGamma.class, LogNormal.class, Normal.class, NormalGamma.class, Poisson.class,
            NegativeBinomial.class, Uniform.class, WeightedDirichlet.class, Weibull.class,
            // tree distribution
            Yule.class, BirthDeathTree.class, FullBirthDeathTree.class, BirthDeathTreeDT.class,
            BirthDeathSamplingTree.class, BirthDeathSamplingTreeDT.class, BirthDeathSerialSamplingTree.class,
            RhoSampleTree.class, FossilBirthDeathTree.class,
            SimBDReverse.class, SimFBDAge.class, SimFossilsPoisson.class,
            SerialCoalescent.class, StructuredCoalescent.class, MultispeciesCoalescent.class,
            // skyline
            SkylineCoalescent.class, ExpMarkovChain.class, RandomComposition.class,
            // others
            ErrorModel.class, RandomBooleanArray.class,
            // phylogenetic distribution
            PhyloBrownian.class, PhyloMultivariateBrownian.class,
            PhyloOU.class,
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
            // Tree
            LocalBranchRates.class, NodeCount.class, TreeLength.class, ExtantTree.class, PruneTree.class,
            // Matrix
            BinaryRateMatrix.class, MigrationMatrix.class, MigrationCount.class,
            // IO
            Newick.class, ReadNexus.class, ReadFasta.class, ExtractTrait.class, Species.class,
            // Math
            lphy.core.functions.Exp.class, Sum.class, SumBoolean.class,
            Abs.class, Floor.class, Ceil.class, Round.class,
            // Utils
            ParseInt.class, Concat.class,
            Length.class, Unique.class, Range.class, Rep.class,
            Select.class, Split.class, SliceDoubleArray.class);


    /**
     * Required by ServiceLoader.
     */
    public LPhyExtImpl() {
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

    @Override
    public Map<String, ? extends SequenceType> getSequenceTypes() {
        Map<String, SequenceType> dataTypeMap = new ConcurrentHashMap<>();
        dataTypeMap.put("rna", SequenceType.NUCLEOTIDE);
        dataTypeMap.put("dna", SequenceType.NUCLEOTIDE);
        dataTypeMap.put(SequenceTypeFactory.sanitise(SequenceType.NUCLEOTIDE.getName()), SequenceType.NUCLEOTIDE); // nucleotide

        dataTypeMap.put(SequenceTypeFactory.sanitise(SequenceType.AMINO_ACID.getName()), SequenceType.AMINO_ACID); // aminoacid
        dataTypeMap.put("protein", SequenceType.AMINO_ACID);

        dataTypeMap.put(SequenceTypeFactory.sanitise(Binary.NAME), Binary.getInstance());
        dataTypeMap.put(SequenceTypeFactory.sanitise(Continuous.NAME), Continuous.getInstance());
        return dataTypeMap;
    }
}
