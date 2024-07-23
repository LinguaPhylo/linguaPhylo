# LPhy Developer Guide 102 (LPhy in Java)

This tutorial focuses on how to implement LPhy components using Java classes.

## LPhy terms

Please read the LPhy publication before you start to write the code. 
https://doi.org/10.1371/journal.pcbi.1011226

It is essential to have a thorough understanding of the following concepts:

- Probabilistic graphical model
- [Value](https://github.com/LinguaPhylo/linguaPhylo/blob/432a3edea15188c72fa12d42d0f238e9c25c1843/lphy/src/main/java/lphy/core/model/Value.java) 
  - Constant
  - [Random variable](https://github.com/LinguaPhylo/linguaPhylo/blob/432a3edea15188c72fa12d42d0f238e9c25c1843/lphy/src/main/java/lphy/core/model/RandomVariable.java)
- [Generator](https://github.com/LinguaPhylo/linguaPhylo/blob/432a3edea15188c72fa12d42d0f238e9c25c1843/lphy/src/main/java/lphy/core/model/Generator.java)
  - [Generative distribution](https://github.com/LinguaPhylo/linguaPhylo/blob/432a3edea15188c72fa12d42d0f238e9c25c1843/lphy/src/main/java/lphy/core/model/GenerativeDistribution.java)
  - [Deterministic function](https://github.com/LinguaPhylo/linguaPhylo/blob/432a3edea15188c72fa12d42d0f238e9c25c1843/lphy/src/main/java/lphy/core/model/DeterministicFunction.java)
  - [Method call](https://github.com/LinguaPhylo/linguaPhylo/blob/432a3edea15188c72fa12d42d0f238e9c25c1843/lphy-base/src/main/java/lphy/base/evolution/alignment/Alignment.java#L22-L55)

In the Java implementation, Value and Generator classes are defined by 
[GraphicalModelNode](https://github.com/LinguaPhylo/linguaPhylo/blob/432a3edea15188c72fa12d42d0f238e9c25c1843/lphy/src/main/java/lphy/core/model/GraphicalModelNode.java).
Also see https://linguaphylo.github.io/programming/2020/09/22/linguaphylo-for-developers.html


## LPhy data type

LPhy is a [dynamic typing](https://en.wikipedia.org/wiki/Type_system) language.
Therefore, as a developer, you need to understand how the data type is handled.
For example,

- All actual values are wrapped in the [Value](https://github.com/LinguaPhylo/linguaPhylo/blob/432a3edea15188c72fa12d42d0f238e9c25c1843/lphy/src/main/java/lphy/core/model/Value.java) class, there are few classes inherit it,
  such as [RandomVariable](https://github.com/LinguaPhylo/linguaPhylo/blob/432a3edea15188c72fa12d42d0f238e9c25c1843/lphy/src/main/java/lphy/core/model/RandomVariable.java).

You need to use the method `.value()` to retrieve the actual value,
and `.getType()` to get its data type.

- It is also required to define what data type to return in either [Generative distribution](https://github.com/LinguaPhylo/linguaPhylo/blob/432a3edea15188c72fa12d42d0f238e9c25c1843/lphy/src/main/java/lphy/core/model/GenerativeDistribution.java)
  or [Deterministic function](https://github.com/LinguaPhylo/linguaPhylo/blob/432a3edea15188c72fa12d42d0f238e9c25c1843/lphy/src/main/java/lphy/core/model/DeterministicFunction.java).
  The detail is explained in next subsections.

Although we have already implemented some commonly used data types in LPhy,
developers may still need to implement new LPhy data types for certain new generators.

### LPhy data type is not sequence type

You may encounter many different "data types" in LPhy or BEAST.
Please do not confuse these with sequence types. In LPhy, data types are specifically defined for the LPhy language.
For example, they can be Double, Integer, Taxa, Alignment, or TimeTree.

However, any "data type" classes that inherit from JEBL [SequenceType](https://github.com/LinguaPhylo/jebl3/blob/b5421dc622e8fff8a93a28352377e5a4c51b57a4/src/main/java/jebl/evolution/sequences/SequenceType.java)
do not fall under this concept. These classes define the type of sequences.


## Write your LPhy object in Java

### Generative distribution

It is a Java interface to represent all types of generative distributions, 
such as [probability distributions](lphy-base/src/main/java/lphy/base/distribution), tree generative distributions 
(e.g. [Birth-death](docs/lphy/evolution/birthdeath.md), [Coalescent](docs/lphy/evolution/birthdeath.md)), 
and [PhyloCTMC](docs/lphy/evolution/likelihood.md) generative distributions.

To write your own generative distribution, you need to follow these steps:

1. Design your LPhy script first, for example, `Θ ~ LogNormal(meanlog=3.0, sdlog=1.0);`.

2. Create a Java class (e.g. LogNormal.java) to implement [GenerativeDistribution<T>](https://github.com/LinguaPhylo/linguaPhylo/blob/432a3edea15188c72fa12d42d0f238e9c25c1843/lphy/src/main/java/lphy/core/model/GenerativeDistribution.java). 

Look at the example [LogNormal.java](https://github.com/LinguaPhylo/linguaPhylo/blob/eea9a4657669a6e9ce3f0acac494fab803df681c/lphy-base/src/main/java/lphy/base/distribution/LogNormal.java).
A few things are required:

   - Define its LPhy name by the annotation `@GeneratorInfo` for the overwritten method `RandomVariable<Double> sample()`. 

     `name = "LogNormal"` will allow the parser to parse it in a LPhy code into this Java object.


   - Define the arguments for this distribution using the annotation `@ParameterInfo` inside the constructor.

     `name = "meanlog"` declares one of the arguments as "meanlog". This is also referred to as a **named argument**. 
     Following the annotation, you need to declare the Java argument for this constructor, 
     which must be a Value, such as `Value<Number> M`. We use `Number` so that this input can accept integer values. 
     To make an argument optional, simply add `optional = true`.


   - Define the data type, e.g. `LogNormal extends ParametricDistribution<Double> implements GenerativeDistribution1D<Double>`,
     where `Double` replaces `T` and must be consistent with the returned type `RandomVariable<Double> sample()`.


   - Implement the method `RandomVariable<...> sample()` which should sample a value from this distribution 
     and then wrap it into `RandomVariable`.


   - Correctly implement **both** methods `Map<String, Value> getParams()` and `setParam(String paramName, Value value)`, 
     otherwise, it will fail when re-sampling values from the probabilistic graphical model represented by 
     an LPhy script using this distribution.


3. Register the distribution to SPI.

The SPI registration class for generative distributions is located at the Java package named as `*.spi`, 
for example, `lphy.base.spi.LPhyBaseImpl`, or `phylonco.lphy.spi.PhyloncoImpl`.    
You can simply add your class into the list returned by the method `List<Class<? extends GenerativeDistribution>> declareDistributions()`. 
Here is the example in [LPhyBaseImpl](https://github.com/LinguaPhylo/linguaPhylo/blob/27efe2d517ca4de98bfd62f74220168ced4d7b77/lphy-base/src/main/java/lphy/base/spi/LPhyBaseImpl.java#L50-L75).

**Please note** the LPhy code will only function properly after the distribution class is registered. 
Therefore, it is acceptable to commit incomplete LPhy object during development (to avoid painful merges) 
without registering it, provided it compiles and is not included in any published unit tests.  


### Deterministic function

It is an abstract class and extends [BasicFunction](https://github.com/LinguaPhylo/linguaPhylo/blob/27efe2d517ca4de98bfd62f74220168ced4d7b77/lphy/src/main/java/lphy/core/model/BasicFunction.java).

To write your own deterministic function, you need to follow the similar steps:

1. Design your LPhy script first, for example, `Q = hky(kappa=κ, freq=π)`.

2. Create a Java class (e.g. HKY.java) to extend [DeterministicFunction<T>](https://github.com/LinguaPhylo/linguaPhylo/blob/432a3edea15188c72fa12d42d0f238e9c25c1843/lphy/src/main/java/lphy/core/model/DeterministicFunction.java).

Look at the example [HKY.java](https://github.com/LinguaPhylo/linguaPhylo/blob/27efe2d517ca4de98bfd62f74220168ced4d7b77/lphy-base/src/main/java/lphy/base/evolution/substitutionmodel/HKY.java).
A few things are required:

- Define its LPhy name by the annotation `@GeneratorInfo` for the overwritten method `Value<Double[][]> apply()`.

  `name = "hky"` will allow the parser to parse it in a LPhy code into this Java object.


- Define the arguments for this distribution using the annotation `@ParameterInfo` inside the constructor.

  `name = "kappa"` declares one of the arguments as "kappa". This is also referred to as a **named argument**.
  Following the annotation, you need to declare the Java argument for this constructor,
  which must be a Value, such as `Value<Number> kappa`. We use `Number` so that this input can accept integer values.
  To make an argument optional, simply add `optional = true`.


- Define the data type, e.g. `extends DeterministicFunction<Double[][]>`,
  where the 2d matrix `Double[][]` replaces `T` and must be consistent with the returned type `Value<Double[][]> apply()`.


- Implement the method `Value<...> apply()` which should return a value **deterministically** 
  and then wrap it into `Value`.


3. Register the distribution to SPI.

Simply add your class into the list returned by the method 
`List<Class<? extends BasicFunction>> declareFunctions()`.


### Method call

The [method call](https://github.com/LinguaPhylo/linguaPhylo/blob/a04bbc4d2d9f46f4049986ba993d4d6d01cdecbf/lphy/src/main/java/lphy/core/parser/function/MethodCall.java)
is a special case of **deterministic** function, but its implementation in Java is somewhat simpler. 
Here is an example of an [LPhy script](https://github.com/LinguaPhylo/linguaPhylo/blob/a04bbc4d2d9f46f4049986ba993d4d6d01cdecbf/examples/data-clamping/twoPartitionCoalescentNex.lphy):

```lphy
data {
  D = readNexus(file="data/primate.nex");
  taxa = D.taxa();
  ...
}
```

In this script, the first line imports an alignment `D` from "primate.nex", 
and the second line uses the method call `D.taxa()` to extract the [taxa](https://github.com/LinguaPhylo/linguaPhylo/blob/a04bbc4d2d9f46f4049986ba993d4d6d01cdecbf/lphy-base/src/main/java/lphy/base/evolution/Taxa.java) object.

To implement this, simply add a Java method with the same name, `taxa()`, in the Alignment class. 
Then, add the `@MethodInfo` annotation with the necessary information. 
The script line `taxa = D.taxa();` will work as long as `D` is an Alignment object.

It is important to **note** that the method call must be implemented inside an existing Java class 
implementing the LPhy object that calls this method.

### Inheritance

You can use Java inheritance to reuse code. 
For example, the [RateMatrix](https://github.com/LinguaPhylo/linguaPhylo/blob/41c70aaee60032e9a9cdbb77b4a8a47b2b7b8d86/lphy-base/src/main/java/lphy/base/evolution/substitutionmodel/RateMatrix.java),
class is the parent class of most substitution models.

### Overload

LPhy allows overloading. For example, the 1st script is implemented by [Bernoulli](https://github.com/LinguaPhylo/linguaPhylo/blob/bf0cb08d26fed00f8c7ef40b12530bbb6b8578a1/lphy-base/src/main/java/lphy/base/distribution/Bernoulli.java)

```lphy
I_siteRates ~ Bernoulli(p=0.5);
```

The 2nd script is implemented by [BernoulliMulti](https://github.com/LinguaPhylo/linguaPhylo/blob/41c70aaee60032e9a9cdbb77b4a8a47b2b7b8d86/lphy-base/src/main/java/lphy/base/distribution/BernoulliMulti.java)

```lphy
I ~ Bernoulli(p=0.5, replicates=dim, minSuccesses=dim-2);
```


## LPhy extension mechanism

After you complete the Java implementation, you need to register it using SPI (Service Provider Interface) 
so that it can be used in an LPhy script.





