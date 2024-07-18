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


   - Correctly implement both methods `Map<String, Value> getParams()` and `setParam(String paramName, Value value)`, 
     otherwise, it will fail when re-sampling values from the probabilistic graphical model represented by 
     an LPhy script using this distribution.


3. Register the distribution to SPI.


### Deterministic function



### Method call



## LPhy extension mechanism

After you complete a LPhy object in Java, you need to register it in SPI, 
so that it can be used in a LPhy script. 

https://linguaphylo.github.io/programming/2021/07/19/lphy-extension.html



