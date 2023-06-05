package lphy.base.lightweight;


import lphy.base.evolution.substitutionmodel.GTR;
import lphy.base.evolution.substitutionmodel.HKY;
import lphy.base.evolution.substitutionmodel.JukesCantor;
import lphy.base.evolution.substitutionmodel.K80;
import lphy.base.function.BinaryRateMatrix;
import lphy.base.function.MigrationMatrix;
import lphy.base.function.tree.MigrationCount;
import lphy.base.function.tree.Newick;
import lphy.base.function.tree.NodeCount;
import lphy.base.lightweight.distributions.*;
import lphy.core.model.component.Generator;
import lphy.core.model.component.Value;
import lphy.core.parser.antlr.LPhyBaseListener;
import lphy.core.parser.function.Range;

import java.util.*;

public class AbstractLightweightBaseListener extends LPhyBaseListener {

    // CURRENT MODEL STATE

    Map<String, Value<?>> dictionary;

    // PARSER STATE
    static Map<String, Set<Class<?>>> genDistDictionary;
    static Map<String, Set<Class<?>>> functionDictionary;
    static Set<String> bivarOperators, univarfunctions;

    static {
        genDistDictionary = new TreeMap<>();
        functionDictionary = new TreeMap<>();

        Class<?>[] genClasses = { Normal.class, LogNormal.class, Exp.class,
                Dirichlet.class, Gamma.class, DiscretizedGamma.class, Beta.class, Poisson.class};

        for (Class<?> genClass : genClasses) {
            String name = Generator.getGeneratorName(genClass);

            Set<Class<?>> genDistSet = genDistDictionary.computeIfAbsent(name, k -> new HashSet<>());
            genDistSet.add(genClass);
        }

        Class<?>[] functionClasses = {JukesCantor.class, K80.class, HKY.class, GTR.class,
                Newick.class, BinaryRateMatrix.class, NodeCount.class, MigrationMatrix.class,
                MigrationCount.class, Range.class};

        for (Class<?> functionClass : functionClasses) {

            String name = Generator.getGeneratorName(functionClass);

            Set<Class<?>> funcSet = functionDictionary.computeIfAbsent(name, k -> new HashSet<>());
            funcSet.add(functionClass);
        }
        System.out.println(Arrays.toString(genDistDictionary.keySet().toArray()));
        System.out.println(Arrays.toString(functionDictionary.keySet().toArray()));
    }
}
