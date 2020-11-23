StructuredCoalescent distribution
=================================
StructuredCoalescent(Object **M**, Integer[] **k**, Taxa **taxa**, Object[] **demes**)
--------------------------------------------------------------------------------------

The structured coalescent distribution over tip-labelled time trees.

### Parameters

- Object **M** - The population process rate matrix which contains the effective population sizes and migration rates. Off-diagonal migration rates are in units of expected migrants per *generation* backwards in time.
- Integer[] **k** - the number of taxa in each population. provide either this or a demes argument.
- Taxa **taxa** - the taxa.
- Object[] **demes** - the deme array, which runs parallel to the taxonArray in the taxa object.

### Return type

- Object



