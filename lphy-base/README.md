# LPhy Standard Library

The "lphy-base" [subproject](https://docs.gradle.org/current/userguide/multi_project_builds.html)
is the standard library containing relevant distributions to specify standard Bayesian phylogenetic analyses and allow simulation from those models.
It should only contain the following implementations in Java:

## Java packages

1. The extended GenerativeDistribution:


2. The extended DeterministicFunction:


3. The "evolution" package: It comprises Java objects that facilitate the implementation of fundamental concepts in
   Bayesian phylogenetics. For example, these objects include the Alignment and Tree classes, which represent essential
   elements of phylogenetic analysis. Additionally, the package also encompasses models such as Coalescent and
   Birth-death, which are integral to probabilistic modeling in phylogenetics.

4. The common sequence types, such as nucleotides, amino acids, binary, etc.

5. This includes two SPIs: one is the LPhy extension to allow the extended GenerativeDistribution and
   DeterministicFunction registering to ServiceLoader. The 2nd is the sequence type extension to allow the third party
   developers create new sequence types. 
