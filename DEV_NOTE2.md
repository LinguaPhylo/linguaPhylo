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

TODO:

### Deterministic function



### Method call



## LPhy extension mechanism

After you complete a LPhy object in Java, you need to register it in SPI, 
so that it can be used in a LPhy script. 

https://linguaphylo.github.io/programming/2021/07/19/lphy-extension.html



