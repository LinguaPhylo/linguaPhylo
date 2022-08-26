MultispeciesCoalescent distribution
===================================
MultispeciesCoalescent([Double[]](../types/Double[].md) **theta**, [Integer[]](../types/Integer[].md) **n**, [Taxa](../types/Taxa.md) **taxa**, [TimeTree](../types/TimeTree.md) **S**)
---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

The Kingman coalescent distribution within each branch of species tree gives rise to a distribution over gene trees conditional on the species tree. The (optional) taxa object provides for non-trivial mappings from individuals to species, and not all species have to have representatives. The (optional) numLoci parameter can be used to produce more than one gene tree from this distribution.

### Parameters

- [Double[]](../types/Double[].md) **theta** - effective population sizes, one for each species (both extant and ancestral).
- [Integer[]](../types/Integer[].md) **n** - the number of sampled taxa in the gene tree for each extant species.
- [Taxa](../types/Taxa.md) **taxa** - the taxa for the gene tree, with species to define the mapping.
- [TimeTree](../types/TimeTree.md) **S** - the species tree. 

### Return type

[TimeTree](../types/TimeTree.md)



