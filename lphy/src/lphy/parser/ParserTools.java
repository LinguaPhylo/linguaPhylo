package lphy.parser;

import lphy.core.PhyloBrownian;
import lphy.core.PhyloOU;
import lphy.core.distributions.*;
import lphy.core.distributions.Exp;
import lphy.core.functions.*;
import lphy.evolution.alignment.ErrorModel;
import lphy.evolution.birthdeath.*;
import lphy.evolution.branchrates.LocalBranchRates;
import lphy.evolution.coalescent.*;
import lphy.evolution.likelihood.PhyloCTMC;
import lphy.evolution.substitutionmodel.*;
import lphy.graphicalModel.Generator;
import lphy.toroidalDiffusion.*;

import java.util.*;
import java.util.Map;

public class ParserTools {

    static Map<String, Set<Class<?>>> genDistDictionary;
    static Map<String, Set<Class<?>>> functionDictionary;
    static Set<String> bivarOperators;
    static Set<String> univarfunctions;


    static {
        genDistDictionary = new TreeMap<>();
        functionDictionary = new TreeMap<>();

        Class<?>[] genClasses = {RhoSampleTree.class, Bernoulli.class, BernoulliMulti.class, FullBirthDeathTree.class, BirthDeathTreeDT.class,
                BirthDeathSamplingTree.class, BirthDeathSamplingTreeDT.class, ExpMarkovChain.class, BirthDeathTree.class, InverseGamma.class,
                Normal.class, NormalMulti.class,  LogNormal.class, LogNormalMulti.class, Exp.class, ExpMulti.class,
                Coalescent.class, PhyloCTMC.class, PhyloBrownian.class, PhyloCircularBrownian.class,
                PhyloCircularOU.class, PhyloOU.class, PhyloToroidalBrownian.class, PhyloWrappedBivariateDiffusion.class,
                Dirichlet.class, Gamma.class, DiscretizedGamma.class, ErrorModel.class, Yule.class, Beta.class,
                MultispeciesCoalescent.class, Poisson.class, RandomComposition.class, RandomBooleanArray.class, SerialCoalescent.class,
                SkylineCoalescent.class, StructuredCoalescent.class};

        for (Class<?> genClass : genClasses) {
            String name = Generator.getGeneratorName(genClass);

            Set<Class<?>> genDistSet = genDistDictionary.computeIfAbsent(name, k -> new HashSet<>());
            genDistSet.add(genClass);
        }

        Class<?>[] functionClasses = {ARange.class, ArgI.class, DataFrameConstruction.class, DataFramePart.class,
                lphy.core.functions.Exp.class, JukesCantor.class, K80.class, F81.class, HKY.class, GTR.class,
                LocalBranchRates.class, lphy.core.functions.Map.class,
                Newick.class, NCharFunction.class, NTaxaFunction.class, BinaryRateMatrix.class, NodeCount.class, MigrationMatrix.class,
                MigrationCount.class, Range.class, RootAge.class, TaxaAgesFunction.class, TaxaFunction.class, TreeLength.class, DihedralAngleDiffusionMatrix.class,
                Nexus.class
        };

        for (Class<?> functionClass : functionClasses) {

            String name = Generator.getGeneratorName(functionClass);

            Set<Class<?>> funcSet = functionDictionary.computeIfAbsent(name, k -> new HashSet<>());
            funcSet.add(functionClass);
        }
        System.out.println(Arrays.toString(genDistDictionary.keySet().toArray()));
        System.out.println(Arrays.toString(functionDictionary.keySet().toArray()));

        bivarOperators = new HashSet<>();
        for (String s : new String[]{"+", "-", "*", "/", "**", "&&", "||", "<=", "<", ">=", ">", "%", ":", "^", "!=", "==", "&", "|", "<<", ">>", ">>>"}) {
            bivarOperators.add(s);
        }
        univarfunctions = new HashSet<>();
        for (String s : new String[]{"abs", "acos", "acosh", "asin", "asinh", "atan", "atanh", "cLogLog", "cbrt", "ceil", "cos", "cosh", "exp", "expm1", "floor", "log", "log10", "log1p", "logFact", "logGamma", "logit", "phi", "probit", "round", "signum", "sin", "sinh", "sqrt", "step", "tan", "tanh"}) {
            univarfunctions.add(s);
        }
    }

    static Set<Class<?>> getGenerativeDistributionClasses(String name) {
        return genDistDictionary.get(name);
    }

    static Set<Class<?>> getFunctionClasses(String name) {
        return functionDictionary.get(name);
    }
}
