Yule distribution
=================
Yule(Object **lambda**, Object **n**, Object **taxa**, Object **rootAge**)
--------------------------------------------------------------------------

The Yule tree distribution over tip-labelled time trees. Will be conditional on the root age if one is provided.

### Parameters

- Object **lambda** - per-lineage birth rate, possibly scaled to mutations or calendar units.
- Object **n** - the number of taxa.
- Object **taxa** - a string array of taxa id or a taxa object (e.g. dataframe, alignment or tree)
- Object **rootAge** - the root age to be conditioned on. optional.

### Return type

- Object



