package lphy.parser;


import lphy.*;
import lphy.core.*;
import lphy.core.distributions.*;
import lphy.core.distributions.Exp;
import lphy.core.functions.*;
import lphy.graphicalModel.Generator;
import lphy.graphicalModel.Value;
import lphy.parser.SimulatorParser.*;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.util.*;
import java.util.List;

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

        Class<?>[] genClasses = {BirthDeathTree.class, BirthDeathTreeDT.class, Normal.class, LogNormal.class, LogNormalMulti.class, Exp.class, Coalescent.class,
                PhyloCTMC.class, PhyloBrownian.class, PhyloCircularBrownian.class, PhyloToroidalBrownian.class, Dirichlet.class, Gamma.class, DiscretizedGamma.class,
                ErrorModel.class, Yule.class, Beta.class, MultispeciesCoalescent.class, Poisson.class, SerialCoalescent.class, StructuredCoalescent.class};

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
