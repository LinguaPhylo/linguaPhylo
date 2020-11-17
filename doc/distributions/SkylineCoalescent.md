SkylineCoalescent distribution
==============================
SkylineCoalescent(Object **theta**, Object **groupSizes**, Object **n**, Object **taxa**, Object **ages**)
----------------------------------------------------------------------------------------------------------

The skyline coalescent distribution over tip-labelled time trees. If no group sizes are specified, then there is one population parameter per coalescent event (as per classic skyline coalescent of Pybus, Rambaut and Harvey 2000)

### Parameters

- Object **theta** - effective population size, one value for each group of coalescent intervals, ordered from present to past. Possibly scaled to mutations or calendar units. If no groupSizes are specified, then the number of coalescent intervals will be equal to the number of population size parameters.
- Object **groupSizes** - A tuple of group sizes. The sum of this tuple determines the number of coalescent events in the tree and thus the number of taxa. By default all group sizes are 1 which is equivalent to the classic skyline coalescent.
- Object **n** - number of taxa.
- Object **taxa** - Taxa object, (e.g. Taxa or Object[])
- Object **ages** - an array of leaf node ages.

### Return type

- Object

### Reference

Drummond, A. J., Rambaut, A., Shapiro, B, & Pybus, O. G. (2005).
Bayesian coalescent inference of past population dynamics from molecular sequences.
Molecular biology and evolution, 22(5), 1185-1192.[http://doi.org/10.1093/molbev/msi103](http://doi.org/10.1093/molbev/msi103)

