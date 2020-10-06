# Coalescent generative distributions for genealogical time trees

This LPHY package describes a family of coalescent generative distributions that produce TimeTrees.

The simplest model in this package is the one parameter model constant-population size coalescent. 
The generation-time-scaled population size parameter (theta) parameter determines at 
what rate, per unit time, a pair of lineages coalesce, backwards in time. 

## Constant population size coalescent model

In its simplest form (Kingman; 1981) the coalescent model produces a tree on a fixed number of leaves based on a population size 
parameter (theta):

```
ψ ~ Coalescent(theta=0.1, n=16);
```

It is also possible to give explicit taxa labels to the generative distribution:

```
ψ ~ Coalescent(theta=0.1, taxa=["a", "b", "c", "d"]);
```

It is also possible to handle serially-sampled (time-stamped) data by adding ages. There are two ways to do that:

Ages without taxa names:

```
g ~ Coalescent(theta=0.1, ages=[0.0, 0.1, 0.2, 0.3]);
```

Ages and taxa names:

```
taxaAges = taxaAges(taxa=["a", "b", "c", "d"], ages=[0.0, 0.1, 0.2, 0.3]); 
g ~ Coalescent(theta=0.1, taxaAges=taxaAges);
```



## Classic skyline coalescent model

A highly parametric version of the coalescent is also possible, where a series of theta values are provided, one for each
group of consecutive coalescent intervals. If the groupSizes are specified then each coalescent interval is given its
own population size. The following code would generate a tree of five taxa, since there are four theta values provided:

```
ψ ~ SkylineCoalescent(theta=[0.1, 0.2, 0.3, 0.4]);
```

The theta values are indexed from the present into the past. So the first coalescent interval (starting from the leaves)
would be generated assuming a population size parameter of 0.1, while the last coalescent interval (culimating at the
root of the tree) would be generated from a population size parameter of 0.4.

It is also possible to add taxa and/or taxa age information:

```
taxaAges = taxaAges(taxa=["a", "b", "c", "d"], ages=[0.0, 0.1, 0.2, 0.3]); 
T ~ SkylineCoalescent(theta=[0.1, 0.2, 0.3], taxaAges=taxaAges);
```

This will produce a serial coalescent tree with three distinct epochs of population size on four taxa with distinct ages.


## Generalized skyline coalescent model

The following generative distribution call will produce a tree of size n=11 taxa, since 4+3+2+1=10= coalescent intervals.
The first four intervals will all have theta=0.1, the next three will have theta=0.2, the next two will have theta=0.3,
and the last coalescent interval will have theta=0.4:

```
ψ ~ SkylineCoalescent(theta=[0.1, 0.2, 0.3, 0.4], groupSizes=[4,3,2,1]);
```

## Structured coalescent

A structured coalescent process takes a migration matrix (M) with population sizes of each deme on the diagonal:
For K demes, theta is an K-tuple and the dimension of m is $K^2 - K$. $n$ is a tuple of sample sizes, one
dimension for each deme:

```
M = migrationMatrix(theta=[0.1, 0.1], m=[1.0, 1.0]);
ψ ~ StructuredCoalescent(M=M, n=[15, 15]);
```

## Multispecies coalescent

This model allows for gene tree-species tree discordance, and is a hierarchical model of phylogeny. A simple
multispecies coalescent model has one distribution define a species tree, and a second distribution define a gene tree
based on the species tree:

```
S ~ Yule(lambda=5, n=4);
g ~ MultispeciesCoalescent(theta=[0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1], n=[2, 2, 2, 2], S=S);
```

Each branch in the species tree has its own theta value. The n value describes how many individuals are represented in
the gene tree for each species. It is a tuple of integers with length equal to the number of species in the species 
tree.