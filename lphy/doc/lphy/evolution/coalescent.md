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

## Classic skyline coalescent model

A highly parameteric version of the coalescent is also possible, where a series of theta values are provided, one for each
group of consecutive coalescent intervals. If the groupSizes are specified then each coalescent interval is given its
own population size. The following code would generate a tree of five taxa, since there are four theta values provided:

```
ψ ~ SkylineCoalescent(theta=[0.1, 0.2, 0.3, 0.4]);
```

The theta values are indexed from the present into the past. So the first coalescent interval (starting from the leaves)
would be generated assuming a population size parameter of 0.1, while the last coalescent interval (culimating at the
root of the tree) would be generated from a population size parameter of 0.4.

## Generalized skyline coalescent model

The following generative distribution call will produce a tree of size n=11 taxa, since 4+3+2+1=10= coalescent intervals.
The first four intervals will all have theta=0.1, the next three will have theta=0.2, the next two will have theta=0.3,
and the last coalescent interval will have theta=0.4:

```
ψ ~ SkylineCoalescent(theta=[0.1, 0.2, 0.3, 0.4], groupSizes=[4,3,2,1]);
```
