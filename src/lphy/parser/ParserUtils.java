package lphy.parser;

import lphy.evolution.continuous.PhyloBrownian;
import lphy.evolution.continuous.PhyloMultivariateBrownian;
import lphy.evolution.continuous.PhyloOU;
import lphy.core.distributions.*;
import lphy.core.distributions.Exp;
import lphy.core.functions.*;
import lphy.evolution.alignment.ErrorModel;
import lphy.evolution.birthdeath.*;
import lphy.evolution.branchrates.LocalBranchRates;
import lphy.evolution.coalescent.*;
import lphy.evolution.likelihood.PhyloCTMC;
import lphy.evolution.substitutionmodel.*;
import lphy.graphicalModel.*;
import lphy.toroidalDiffusion.*;
import lphy.utils.LoggerUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.Map;

public class ParserUtils {

    static Map<String, Set<Class<?>>> genDistDictionary;
    static Map<String, Set<Class<?>>> functionDictionary;
    public static Set<String> bivarOperators;
    static Set<String> univarfunctions;


    static {
        genDistDictionary = new TreeMap<>();
        functionDictionary = new TreeMap<>();

        Class<?>[] genClasses = {
                RhoSampleTree.class, Bernoulli.class, BernoulliMulti.class, FullBirthDeathTree.class, BirthDeathTreeDT.class,
                BirthDeathSamplingTree.class, BirthDeathSamplingTreeDT.class, BirthDeathSerialSamplingTree.class, ExpMarkovChain.class, BirthDeathTree.class, InverseGamma.class,
                Normal.class, NormalMulti.class, LogNormal.class, LogNormalMulti.class, Exp.class, ExpMulti.class,
                PhyloCTMC.class, PhyloBrownian.class, PhyloCircularBrownian.class, PhyloMultivariateBrownian.class,
                PhyloCircularOU.class, PhyloOU.class, PhyloToroidalBrownian.class, PhyloWrappedBivariateDiffusion.class,
                Dirichlet.class, Gamma.class, DiscretizedGamma.class, ErrorModel.class, Yule.class, Beta.class,
                MultispeciesCoalescent.class, Poisson.class, RandomComposition.class, RandomBooleanArray.class, SerialCoalescent.class,
                SkylineCoalescent.class, StructuredCoalescent.class};

        for (Class<?> genClass : genClasses) {
            String name = Generator.getGeneratorName(genClass);

            Set<Class<?>> genDistSet = genDistDictionary.computeIfAbsent(name, k -> new HashSet<>());
            genDistSet.add(genClass);
        }

        Class<?>[] functionClasses = {ARange.class, ArgI.class,
                lphy.core.functions.Exp.class, JukesCantor.class, K80.class, F81.class, HKY.class, GTR.class, WAG.class, LewisMK.class,
                LocalBranchRates.class,
                CreateTaxa.class,
                Newick.class, NCharFunction.class, NTaxaFunction.class, BinaryRateMatrix.class, NodeCount.class, MigrationMatrix.class,
                MigrationCount.class, Range.class, RootAge.class, TaxaFunction.class, Rep.class,
                TreeLength.class, DihedralAngleDiffusionMatrix.class,
                Nexus.class, Partition.class,
                Species.class
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

    public static List<Generator> getMatchingFunctions(String name, Value[] values) {
        List<Generator> matches = new ArrayList<>();
        for (Class functionClass : getFunctionClasses(name)) {
            matches.addAll(getFunctionByArguments(name, values, functionClass));
        }
        return matches;
    }

    public static List<Generator> getMatchingFunctions(String name, Map<String, Value> arguments) {
        List<Generator> matches = new ArrayList<>();
        for (Class functionClass : getFunctionClasses(name)) {
            matches.addAll(getGeneratorByArguments(name, arguments, functionClass));
        }
        return matches;
    }

    public static List<Generator> getMatchingGenerativeDistributions(String name, Map<String, Value> arguments) {
        List<Generator> matches = new ArrayList<>();
        for (Class functionClass : getGenerativeDistributionClasses(name)) {
            System.out.println("Found potential matching class: " + functionClass);
            matches.addAll(getGeneratorByArguments(name, arguments, functionClass));
        }
        return matches;
    }

    public static List<Generator> getGeneratorByArguments(String name, Map<String, Value> arguments, Class generatorClass) {

        List<Generator> matches = new ArrayList<>();

        for (Constructor constructor : generatorClass.getConstructors()) {
            List<ParameterInfo> pInfo = Generator.getParameterInfo(constructor);
            List<Object> initargs = new ArrayList<>();
//            if (arguments.size() == 1 && pInfo.size() == 1 && arguments.keySet().iterator().next().equals(pInfo.get(0).name())) {
//                matches.add(constructGenerator(name, constructor, new Object[] {arguments.values().iterator().next()}));
//            }

            if (match(arguments, pInfo)) {
                for (int i = 0; i < pInfo.size(); i++) {
                    Value arg = arguments.get(pInfo.get(i).name());
                    if (arg != null) {
                        initargs.add(arg);
                    } else if (!pInfo.get(i).optional()) {
                        throw new RuntimeException("Required argument " + pInfo.get(i).name() + " not found!");
                    } else {
                        initargs.add(null);
                    }
                }
                matches.add(constructGenerator(name, constructor, initargs.toArray()));
            }
        }
        return matches;
    }


    /**
     * A match occurs if the required parameters are in the argument map and the remaining arguments in the map match names of optional arguments.
     *
     * @param arguments
     * @param pInfo
     * @return
     */
    private static boolean match(Map<String, Value> arguments, List<ParameterInfo> pInfo) {

        Set<String> requiredArguments = new TreeSet<>();
        Set<String> optionalArguments = new TreeSet<>();
        for (ParameterInfo pinfo : pInfo) {
            if (pinfo.optional()) {
                optionalArguments.add(pinfo.name());
            } else {
                requiredArguments.add(pinfo.name());
            }
        }

        if (!arguments.keySet().containsAll(requiredArguments)) {
            return false;
        }
        Set<String> allArguments = optionalArguments;
        allArguments.addAll(requiredArguments);
        return allArguments.containsAll(arguments.keySet());
    }

    private static List<DeterministicFunction> getFunctionByArguments(String name, Value[] values, Class generatorClass) {

        List<DeterministicFunction> matches = new ArrayList<>();
        for (Constructor constructor : generatorClass.getConstructors()) {
            List<ParameterInfo> pInfo = Generator.getParameterInfo(constructor);

            if (values.length == pInfo.size() && (values.length == 1 || values.length == 2)) {
                DeterministicFunction f = (DeterministicFunction)constructGenerator(name, constructor, values);
                if (f != null) {
                    matches.add(f);
                }
            } else if (values.length == 0 && pInfo.size() == 1 && pInfo.get(0).optional()) {
                DeterministicFunction f = (DeterministicFunction)constructGenerator(name, constructor, new Object[] {null});
                if (f != null) {
                    matches.add(f);
                }
            }
        }
        return matches;
    }

    private static Generator constructGenerator(String name, Constructor constructor, Object[] initargs) {
        try {
            return (Generator) constructor.newInstance(initargs);
        } catch (InstantiationException e) {
            e.printStackTrace();
            LoggerUtils.log.severe("Parsing generator " + name + " failed.");
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            LoggerUtils.log.severe("Parsing generator " + name + " failed.");
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            LoggerUtils.log.severe("Parsing generator " + name + " failed.");
        }
        return null;
    }

    static Set<Class<?>> getGenerativeDistributionClasses(String name) {
        return genDistDictionary.get(name);
    }

    static Set<Class<?>> getFunctionClasses(String name) {
        return functionDictionary.get(name);
    }
}
