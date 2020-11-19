package lphy.parser;

import lphy.core.distributions.Exp;
import lphy.core.distributions.*;
import lphy.core.functions.*;
import lphy.core.lightweight.GenerativeDistributionAdapter;
import lphy.core.lightweight.LightweightGenerativeDistribution;
import lphy.core.lightweight.LightweightGenerator;
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
import lphy.evolution.functions.ExtantTaxa;
import lphy.evolution.likelihood.PhyloCTMC;
import lphy.evolution.substitutionmodel.*;
import lphy.evolution.tree.ExtantTree;
import lphy.evolution.tree.PruneTree;
import lphy.graphicalModel.*;
import lphy.toroidalDiffusion.*;
import lphy.utils.LoggerUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.*;
import java.util.stream.Collectors;

public class ParserUtils {

    static Map<String, Set<Class<?>>> genDistDictionary;
    static Map<String, Set<Class<?>>> functionDictionary;
    public static Set<String> bivarOperators;
    static Set<String> univarfunctions;

    public static TreeSet<Class<?>> types = new TreeSet<>(Comparator.comparing(Class::getName));

    static {
        genDistDictionary = new TreeMap<>();
        functionDictionary = new TreeMap<>();

        Class<?>[] lightWeightGenClasses = {
                lphy.core.lightweight.distributions.Beta.class
        };

        Class<?>[] genClasses = {
                RhoSampleTree.class, Bernoulli.class, BernoulliMulti.class, FullBirthDeathTree.class, BirthDeathTreeDT.class,
                BirthDeathSamplingTree.class, BirthDeathSamplingTreeDT.class, BirthDeathSerialSamplingTree.class, ExpMarkovChain.class, BirthDeathTree.class, InverseGamma.class,
                DirichletMulti.class,
                InverseGammaMulti.class,
                Normal.class, NormalMulti.class, LogNormal.class, LogNormalMulti.class, Exp.class, ExpMulti.class,
                PhyloCTMC.class, PhyloBrownian.class, PhyloCircularBrownian.class, PhyloMultivariateBrownian.class,
                PhyloCircularOU.class, PhyloOU.class, PhyloToroidalBrownian.class, PhyloWrappedBivariateDiffusion.class,
                Dirichlet.class, Gamma.class, DiscretizedGamma.class, ErrorModel.class, Yule.class,
                MultispeciesCoalescent.class, Poisson.class, RandomComposition.class, RandomBooleanArray.class,
                WeightedDirichlet.class,
                SerialCoalescent.class,
                SimBDReverse.class, SimFBDAge.class, SimFossilsPoisson.class,
                SkylineCoalescent.class, StructuredCoalescent.class,
                FossilBirthDeathTree.class,
                Uniform.class};

        for (Class<?> genClass : genClasses) {
            String name = Generator.getGeneratorName(genClass);

            Set<Class<?>> genDistSet = genDistDictionary.computeIfAbsent(name, k -> new HashSet<>());
            genDistSet.add(genClass);

            types.add(Generator.getReturnType(genClass));

            for (ParameterInfo parameterInfo : Generator.getAllParameterInfo(genClass)) {
                types.add(parameterInfo.type());
            }
        }

        for (Class<?> genClass : lightWeightGenClasses) {
            String name = Generator.getGeneratorName(genClass);

            Set<Class<?>> genDistSet = genDistDictionary.computeIfAbsent(name, k -> new HashSet<>());
            genDistSet.add(genClass);

            types.add(LightweightGenerator.getReturnType((Class<LightweightGenerator>)genClass));

            for (ParameterInfo parameterInfo : Generator.getAllParameterInfo(genClass)) {
                types.add(parameterInfo.type());
            }
        }

        Class<?>[] functionClasses = {ARange.class, ArgI.class,
                lphy.core.functions.Exp.class, JukesCantor.class, K80.class, F81.class, HKY.class, GTR.class, WAG.class,
                GeneralTimeReversible.class,
                Length.class,
                LewisMK.class,
                LocalBranchRates.class,
                CreateTaxa.class,
                Newick.class, NCharFunction.class, NTaxaFunction.class, BinaryRateMatrix.class, NodeCount.class, MigrationMatrix.class,
                MigrationCount.class, Range.class, TaxaFunction.class, Rep.class,
                TreeLength.class, DihedralAngleDiffusionMatrix.class,
                ReadNexus.class, ReadFasta.class,
                Species.class, ExtantTree.class, PruneTree.class,
                ExtantTaxa.class,
                Select.class,
                SliceDoubleArray.class, Sum.class
        };

        for (Class<?> functionClass : functionClasses) {

            String name = Generator.getGeneratorName(functionClass);

            Set<Class<?>> funcSet = functionDictionary.computeIfAbsent(name, k -> new HashSet<>());
            funcSet.add(functionClass);

            for (ParameterInfo parameterInfo : Generator.getAllParameterInfo(functionClass)) {
                types.add(parameterInfo.type());
            }
        }
        System.out.println(Arrays.toString(genDistDictionary.keySet().toArray()));
        System.out.println(Arrays.toString(functionDictionary.keySet().toArray()));

        TreeSet<String> typeNames = types.stream().map(Class::getSimpleName).collect(Collectors.toCollection(TreeSet::new));

        System.out.println(typeNames);

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

        Set<Class<?>> generators = getGenerativeDistributionClasses(name);

        if (generators != null) {
            for (Class functionClass : getGenerativeDistributionClasses(name)) {
                matches.addAll(getGeneratorByArguments(name, arguments, functionClass));
            }
        } else {
            LoggerUtils.log.severe("No generator with name " + name + " available.");
        }
        return matches;
    }

    public static List<Generator> getGeneratorByArguments(String name, Map<String, Value> arguments, Class generatorClass) {

        List<Generator> matches = new ArrayList<>();

        for (Constructor constructor : generatorClass.getConstructors()) {
            List<ParameterInfo> pInfo = Generator.getParameterInfo(constructor);
            List<Object> initargs = new ArrayList<>();

            if (match(arguments, pInfo)) {

                boolean lightweight = LightweightGenerativeDistribution.class.isAssignableFrom(generatorClass);

                for (int i = 0; i < pInfo.size(); i++) {
                    Value arg = arguments.get(pInfo.get(i).name());
                    if (arg != null) {
                        initargs.add(lightweight ? arg.value() : arg);
                    } else if (!pInfo.get(i).optional()) {
                        throw new RuntimeException("Required argument " + pInfo.get(i).name() + " not found!");
                    } else {
                        initargs.add(null);
                    }
                }

                matches.add(constructGenerator(name, constructor, pInfo, initargs.toArray(),arguments,lightweight));
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
                DeterministicFunction f = (DeterministicFunction) constructGenerator(name, constructor, pInfo, values, null, false);
                if (f != null) {
                    matches.add(f);
                }
            } else if (values.length == 0 && pInfo.size() == 1 && pInfo.get(0).optional()) {
                DeterministicFunction f = (DeterministicFunction) constructGenerator(name, constructor, pInfo, new Object[]{null}, null, false);
                if (f != null) {
                    matches.add(f);
                }
            }
        }
        return matches;
    }

    private static Generator constructGenerator(String name, Constructor constructor, List<ParameterInfo> pInfo, Object[] initargs, Map<String, Value> params, boolean lightweight) {
        try {
            if (Generator.matchingParameterTypes(pInfo, initargs, lightweight)) {
                if (lightweight) {
                    boolean generativeDistribution = LightweightGenerativeDistribution.class.isAssignableFrom(constructor.getDeclaringClass());
                    if (generativeDistribution) {
                        return new GenerativeDistributionAdapter((LightweightGenerativeDistribution)constructor.newInstance(initargs), params);
                    } else throw new RuntimeException("Only lightweight generative distributions are currently supported!");
                }

                return (Generator) constructor.newInstance(initargs);
            } else if (vectorMatch(pInfo, initargs) > 0) {
                // do vector match
                return vectorGenerator(constructor, pInfo, initargs);
            } else {
                throw new RuntimeException("ERROR! No match, including vector match!");
            }

        } catch (InstantiationException e) {
            e.printStackTrace();
            LoggerUtils.log.severe("Parsing generator " + name + " failed.");
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            LoggerUtils.log.severe("Parsing generator " + name + " failed.");
        } catch (InvocationTargetException e) {
            LoggerUtils.log.severe("Parsing generator " + name + " failed.");
        }
        return null;
    }

    public static int vectorMatch(List<ParameterInfo> pInfo, Object[] initargs) {
        int vectorMatches = 0;
        for (int i = 0; i < pInfo.size(); i++) {
            ParameterInfo parameterInfo = pInfo.get(i);
            Value argValue = (Value) initargs[i];

            if (argValue == null) {
                if (!parameterInfo.optional()) return 0;
            } else {
                if (parameterInfo.type().isAssignableFrom(argValue.value().getClass())) {
                    // direct type match
                } else if (argValue.value().getClass().isArray() && parameterInfo.type().isAssignableFrom(argValue.value().getClass().getComponentType())) {
                    // vector match
                    vectorMatches += 1;
                }
            }
        }
        return vectorMatches;
    }

    public static Generator vectorGenerator(Constructor constructor, List<ParameterInfo> pInfo, Object[] vectorArgs) throws IllegalAccessException, InvocationTargetException, InstantiationException {

        if (GenerativeDistribution.class.isAssignableFrom(constructor.getDeclaringClass())) {
            return new VectorizedDistribution(constructor, pInfo, vectorArgs);
        } else if (DeterministicFunction.class.isAssignableFrom(constructor.getDeclaringClass())) {
            return new VectorizedFunction(constructor, pInfo, vectorArgs);
        } else
            throw new IllegalArgumentException("Unexpected Generator class! Expecting a GenerativeDistribution or a DeterministicFunction");

    }

    private static Set<Class<?>> getGenerativeDistributionClasses(String name) {
        return genDistDictionary.get(name);
    }

    static Set<Class<?>> getFunctionClasses(String name) {
        return functionDictionary.get(name);
    }

    public static List<Class<GenerativeDistribution>> getGenerativeDistributions() {
        List<Class<GenerativeDistribution>> genDists = new ArrayList<>();

        for (Set<Class<?>> classes : genDistDictionary.values()) {
            for (Class<?> c : classes) {
                genDists.add((Class<GenerativeDistribution>) c);
            }
        }
        return genDists;
    }

    public static List<Class<DeterministicFunction>> getDeterministicFunctions() {
        List<Class<DeterministicFunction>> functions = new ArrayList<>();

        for (Set<Class<?>> classes : functionDictionary.values()) {
            for (Class<?> c : classes) {
                functions.add((Class<DeterministicFunction>) c);
            }
        }
        return functions;
    }
}
