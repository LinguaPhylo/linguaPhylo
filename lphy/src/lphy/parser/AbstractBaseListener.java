package lphy.parser;


import lphy.core.*;
import lphy.core.distributions.*;
import lphy.core.distributions.Exp;
import lphy.core.functions.*;
import lphy.evolution.alignment.ErrorModel;
import lphy.evolution.birthdeath.*;
import lphy.evolution.coalescent.*;
import lphy.evolution.likelihood.PhyloCTMC;
import lphy.evolution.substitutionmodel.*;
import lphy.toroidalDiffusion.*;
import lphy.graphicalModel.Generator;
import lphy.graphicalModel.Value;

import java.util.*;
import java.util.Map;

public class AbstractBaseListener extends SimulatorBaseListener {

    // CURRENT MODEL STATE

    Map<String, Value<?>> dictionary;

    // PARSER STATE
    static Map<String, Set<Class<?>>> genDistDictionary;
    static Map<String, Set<Class<?>>> functionDictionary;
    static Set<String> bivarOperators, univarfunctions;

    static {
        genDistDictionary = new TreeMap<>();
        functionDictionary = new TreeMap<>();

        Class<?>[] genClasses = {RhoSampleTree.class, BernoulliMulti.class, BirthDeathTree.class, BirthDeathTreeDT.class,
                BirthDeathSamplingTree.class, BirthDeathSamplingTreeDT.class, ExpMarkovChain.class, Normal.class,
                NormalMulti.class,  LogNormal.class, LogNormalMulti.class, Exp.class, Coalescent.class,
                PhyloCTMC.class, PhyloBrownian.class, PhyloCircularBrownian.class, PhyloCircularOU.class, PhyloOU.class,
                PhyloToroidalBrownian.class, PhyloWrappedBivariateDiffusion.class, Dirichlet.class, Gamma.class,
                DiscretizedGamma.class, ErrorModel.class, Yule.class, Beta.class, MultispeciesCoalescent.class,
                Poisson.class, RandomComposition.class, SerialCoalescent.class, SkylineCoalescent.class,
                StructuredCoalescent.class};

        for (Class<?> genClass : genClasses) {
            String name = Generator.getGeneratorName(genClass);

            Set<Class<?>> genDistSet = genDistDictionary.computeIfAbsent(name, k -> new HashSet<>());
            genDistSet.add(genClass);
        }

        Class<?>[] functionClasses = {ARange.class, lphy.core.functions.Exp.class, JukesCantor.class, K80.class, F81.class, HKY.class, GTR.class, lphy.core.functions.Map.class,
                Newick.class, BinaryRateMatrix.class, NodeCount.class, MigrationMatrix.class, MigrationCount.class, Range.class, RootAge.class, DihedralAngleDiffusionMatrix.class};

        for (Class<?> functionClass : functionClasses) {

            String name = Generator.getGeneratorName(functionClass);

            Set<Class<?>> funcSet = functionDictionary.computeIfAbsent(name, k -> new HashSet<>());
            funcSet.add(functionClass);
        }
        System.out.println(Arrays.toString(genDistDictionary.keySet().toArray()));
        System.out.println(Arrays.toString(functionDictionary.keySet().toArray()));
    }
}
