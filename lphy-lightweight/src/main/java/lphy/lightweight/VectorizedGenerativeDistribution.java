package lphy.base.lightweight;

import lphy.base.lightweight.distributions.*;
import lphy.core.graphicalmodel.components.Argument;
import lphy.core.graphicalmodel.components.Generator;

import java.util.*;
import java.util.stream.Collectors;

public class VectorizedGenerativeDistribution<T> extends VectorizedGenerator<T> implements LGenerativeDistribution<T[]> {

    // PARSER STATE
    static Map<String, Set<Class<?>>> genDistDictionary;

    static {
        genDistDictionary = new TreeMap<>();

        Class<?>[] genClasses = { Normal.class, LogNormal.class, Exp.class,
                Dirichlet.class, Gamma.class, DiscretizedGamma.class, Beta.class, Poisson.class};

        for (Class<?> genClass : genClasses) {
            String name = Generator.getGeneratorName(genClass);

            Set<Class<?>> genDistSet = genDistDictionary.computeIfAbsent(name, k -> new HashSet<>());
            genDistSet.add(genClass);
        }
    }

    public VectorizedGenerativeDistribution(LGenerativeDistribution<T> baseDistribution, SortedMap<Argument, Object> argumentValues) {
        super(baseDistribution, argumentValues);
    }

    public static VectorizedGenerativeDistribution createVectorizedGenerativeDistribution(String name, Map<String, Object> params) {

        LGenerativeDistribution lwg = (LGenerativeDistribution)genDistDictionary.get(name);

        if (lwg == null) throw new RuntimeException("Couldn't find generative distribution named: " + name);

        SortedMap<Argument, Object> arguments =
                params.entrySet().stream()
                        .collect(Collectors.toMap(
                                e -> lwg.getArgumentByName(e.getKey()),
                                e -> e,
                                (u, v) -> {
                                    throw new RuntimeException("should not happen: " + u + " --- map: " + v);
                                },
                                TreeMap::new
                        ));

        return new VectorizedGenerativeDistribution(lwg,arguments);
    }


    public static void main(String[] args) {
        Beta beta = new Beta(1.0, 2.0);

        SortedMap<Argument, Object> arguments = new TreeMap<>();
        arguments.put(beta.getArguments().get(0), new Double[]{200.0, 200.0, 200.0, 3.0, 3.0, 3.0});
        arguments.put(beta.getArguments().get(1), 2.0);

        VectorizedGenerativeDistribution<Double> v = new VectorizedGenerativeDistribution<>(beta, arguments);

        Double[] rbeta = v.generateRaw();

        System.out.println(Arrays.toString(rbeta));

        System.out.println(Arrays.toString(beta.getClass().getConstructors()[0].getParameterTypes()));
    }
}
