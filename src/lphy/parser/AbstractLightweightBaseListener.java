package lphy.parser;


import lphy.core.functions.*;
import lphy.core.lightweight.distributions.*;
import lphy.core.lightweight.distributions.Exp;
import lphy.graphicalModel.Generator;
import lphy.graphicalModel.Value;

import java.util.*;

public class AbstractLightweightBaseListener extends SimulatorBaseListener {

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

        Class<?>[] functionClasses = {lphy.core.functions.Exp.class, JukesCantor.class, K80.class, HKY.class, GTR.class,
                Newick.class, BinaryRateMatrix.class, NodeCount.class, MigrationMatrix.class, MigrationCount.class, Range.class, RootAge.class};

        for (Class<?> functionClass : functionClasses) {

            String name = Generator.getGeneratorName(functionClass);

            Set<Class<?>> funcSet = functionDictionary.computeIfAbsent(name, k -> new HashSet<>());
            funcSet.add(functionClass);
        }
        System.out.println(Arrays.toString(genDistDictionary.keySet().toArray()));
        System.out.println(Arrays.toString(functionDictionary.keySet().toArray()));
    }
}
