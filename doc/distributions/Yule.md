Yule distribution
=================
Yule(Number **lambda**, Integer **n**, Object **taxa**, Number **rootAge**)
---------------------------------------------------------------------------

The Yule tree distribution over tip-labelled time trees. Will be conditional on the root age if one is provided.

### Parameters

- Number **lambda** - per-lineage birth rate, possibly scaled to mutations or calendar units.
- Integer **n** - the number of taxa.
- Object **taxa** - a string array of taxa id or a taxa object (e.g. dataframe, alignment or tree)
- Number **rootAge** - the root age to be conditioned on. optional.

### Return type

- TimeTree



