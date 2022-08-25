MultispeciesCoalescent distribution
===================================
MultispeciesCoalescent(Double[] **theta**, Integer[] **n**, Taxa **taxa**, TimeTree **S**)
------------------------------------------------------------------------------------------

The Kingman coalescent distribution within each branch of species tree gives rise to a distribution over gene trees conditional on the species tree. The (optional) taxa object provides for non-trivial mappings from individuals to species, and not all species have to have representatives. The (optional) numLoci parameter can be used to produce more than one gene tree from this distribution.

### Parameters

- Double[] **theta** - effective population sizes, one for each species (both extant and ancestral).
- Integer[] **n** - the number of sampled taxa in the gene tree for each extant species.
- Taxa **taxa** - the taxa for the gene tree, with species to define the mapping.
- TimeTree **S** - the species tree. 

### Return type

- TimeTree



