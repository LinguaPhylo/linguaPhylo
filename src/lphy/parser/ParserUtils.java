package lphy.parser;

import lphy.bmodeltest.BModelSetFunction;
import lphy.bmodeltest.NucleotideModel;
import lphy.core.distributions.Exp;
import lphy.core.distributions.*;
import lphy.core.functions.*;
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

//        Class<?>[] lightWeightGenClasses = {
//                lphy.core.lightweight.distributions.Beta.class
//        };

        Class<?>[] genClasses = {
                // probability distribution
                Normal.class, LogNormal.class, Exp.class, Bernoulli.class, Poisson.class, Beta.class, Uniform.class,
                Dirichlet.class, Gamma.class, InverseGamma.class, DiscretizedGamma.class, WeightedDirichlet.class,
                UniformInteger.class,
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
                PhyloBrownian.class, PhyloCircularBrownian.class, PhyloMultivariateBrownian.class,
                PhyloCircularOU.class, PhyloOU.class, PhyloToroidalBrownian.class, PhyloWrappedBivariateDiffusion.class,
                PhyloCTMC.class};

        for (Class<?> genClass : genClasses) {
            String name = Generator.getGeneratorName(genClass);

            Set<Class<?>> genDistSet = genDistDictionary.computeIfAbsent(name, k -> new HashSet<>());
            genDistSet.add(genClass);

            types.add(GenerativeDistribution.getReturnType((Class<GenerativeDistribution>)genClass));

            Collections.addAll(types, Generator.getParameterTypes((Class<Generator>) genClass, 0));

            Collections.addAll(types, Generator.getReturnType(genClass));
        }

//        for (Class<?> genClass : lightWeightGenClasses) {
//            String name = Generator.getGeneratorName(genClass);
//
//            Set<Class<?>> genDistSet = genDistDictionary.computeIfAbsent(name, k -> new HashSet<>());
//            genDistSet.add(genClass);
//
//            types.add(LGenerator.getReturnType((Class<LGenerator>)genClass));
//        }

        Class<?>[] functionClasses = {ARange.class, ArgI.class,
                // Substitution models
                JukesCantor.class, K80.class, F81.class, HKY.class, GTR.class, WAG.class,
                GeneralTimeReversible.class, LewisMK.class,
                NucleotideModel.class,
                BModelSetFunction.class,
                // Taxa
                CreateTaxa.class, ExtantTaxa.class, NCharFunction.class, NTaxaFunction.class, TaxaFunction.class,
                // Tree
                LocalBranchRates.class, NodeCount.class, TreeLength.class, ExtantTree.class, PruneTree.class,
                // Matrix
                BinaryRateMatrix.class, MigrationMatrix.class, MigrationCount.class, DihedralAngleDiffusionMatrix.class,
                // IO
                Newick.class, ReadNexus.class, ReadFasta.class, ExtractTrait.class, Species.class,
                // Math
                lphy.core.functions.Exp.class, Sum.class, SumBoolean.class,
                // Utils
                ParseInt.class,
                Length.class, Unique.class, Range.class, Rep.class,
                Select.class, Split.class, SliceDoubleArray.class
        };

        for (Class<?> functionClass : functionClasses) {

            String name = Generator.getGeneratorName(functionClass);

            Set<Class<?>> funcSet = functionDictionary.computeIfAbsent(name, k -> new HashSet<>());
            funcSet.add(functionClass);

            Collections.addAll(types, Generator.getParameterTypes((Class<Generator>) functionClass, 0));

            Collections.addAll(types, Generator.getReturnType(functionClass));
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
            for (Class genClass : getGenerativeDistributionClasses(name)) {
                matches.addAll(getGeneratorByArguments(name, arguments, genClass));
            }
        } else {
            LoggerUtils.log.severe("No generator with name " + name + " available.");
        }
        return matches;
    }

    public static List<Generator> getGeneratorByArguments(String name, Map<String, Value> arguments, Class generatorClass) {

        List<Generator> matches = new ArrayList<>();

        for (Constructor constructor : generatorClass.getConstructors()) {
            List<Argument> argumentInfo = Generator.getArguments(constructor);
            List<Object> initargs = new ArrayList<>();

            if (match(arguments, argumentInfo)) {

                for (int i = 0; i < argumentInfo.size(); i++) {
                    Value arg = arguments.get(argumentInfo.get(i).name);
                    if (arg != null) {
                        initargs.add(arg);
                    } else if (!argumentInfo.get(i).optional) {
                        throw new RuntimeException("Required argument " + argumentInfo.get(i).name + " not found!");
                    } else {
                        initargs.add(null);
                    }
                }

                matches.add(constructGenerator(name, constructor, argumentInfo, initargs.toArray(), arguments,false));
            }
        }
        return matches;
    }


    /**
     * A match occurs if the required parameters are in the argument map and the remaining arguments in the map match names of optional arguments.
     *
     * an iid match occurs if the required parameters are in the argument map and the remaining arguments include "replicates" and are otherwise named matchs for the optional arguments
     *
     * @param arguments the arguments that are attempting to be passed.
     * @param argumentInfo the arguments of the constructor
     * @return
     */
    private static boolean match(Map<String, Value> arguments, List<Argument> argumentInfo) {

        Set<String> requiredArguments = new TreeSet<>();
        Set<String> optionalArguments = new TreeSet<>();
        Set<String> keys = new TreeSet<>();
        keys.addAll(arguments.keySet());

        for (Argument argumentInf : argumentInfo) {
            if (argumentInf.optional) {
                optionalArguments.add(argumentInf.name);
            } else {
                requiredArguments.add(argumentInf.name);
            }
        }

        // return false if not all required arguments are present
        if (!keys.containsAll(requiredArguments)) {
            return false;
        }

        keys.removeAll(requiredArguments);
        keys.removeAll(optionalArguments);
        return keys.size() == 0 || (keys.size() == 1 && keys.contains(IID.replicatesParamName));
    }

    private static List<DeterministicFunction> getFunctionByArguments(String name, Value[] values, Class generatorClass) {

        List<DeterministicFunction> matches = new ArrayList<>();
        for (Constructor constructor : generatorClass.getConstructors()) {
            List<Argument> arguments = Generator.getArguments(constructor);

            if (values.length == arguments.size() && (values.length == 1 || values.length == 2)) {
                DeterministicFunction f = (DeterministicFunction) constructGenerator(name, constructor, arguments, values, null, false);
                if (f != null) {
                    matches.add(f);
                }
            } else if (values.length == 0 && arguments.size() == 1 && arguments.get(0).optional) {
                DeterministicFunction f = (DeterministicFunction) constructGenerator(name, constructor, arguments, new Object[]{null}, null, false);
                if (f != null) {
                    matches.add(f);
                }
            }
        }
        return matches;
    }

    /**
     * @param name the name of the generator
     * @param constructor the constructor
     * @param arguments
     * @param initargs
     * @param params
     * @param lightweight
     * @return
     */
    private static Generator constructGenerator(String name, Constructor constructor, List<Argument> arguments, Object[] initargs, Map<String, Value> params, boolean lightweight) {
        try {
            if (Generator.matchingParameterTypes(arguments, initargs, params, lightweight)) {
                return (Generator) constructor.newInstance(initargs);
            } else if (IID.match(constructor, arguments, initargs, params)) {
                return new IID(constructor, initargs, params);
            } else if (vectorMatch(arguments, initargs) > 0) {
                // do vector match
                return vectorGenerator(constructor, arguments, initargs);
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

    public static int vectorMatch(List<Argument> arguments, Object[] initargs) {
        int vectorMatches = 0;
        for (int i = 0; i < arguments.size(); i++) {
            Argument argument = arguments.get(i);
            Value argValue = (Value) initargs[i];

            if (argValue == null) {
                if (!argument.optional) return 0;
            } else {
                if (argument.type.isAssignableFrom(argValue.value().getClass())) {
                    // direct type match
                } else if (argValue.value().getClass().isArray() && argument.type.isAssignableFrom(argValue.value().getClass().getComponentType())) {
                    // vector match
                    vectorMatches += 1;
                }
            }
        }
        return vectorMatches;
    }

    public static Generator vectorGenerator(Constructor constructor, List<Argument> arguments, Object[] vectorArgs) throws IllegalAccessException, InvocationTargetException, InstantiationException {

        if (GenerativeDistribution.class.isAssignableFrom(constructor.getDeclaringClass())) {
            return new VectorizedDistribution(constructor, arguments, vectorArgs);
        } else if (DeterministicFunction.class.isAssignableFrom(constructor.getDeclaringClass())) {
            return new VectorizedFunction(constructor, arguments, vectorArgs);
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
