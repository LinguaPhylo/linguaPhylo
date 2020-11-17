BirthDeath distribution
=======================
BirthDeath(Object **lambda**, Object **mu**, Object **n**, Object **taxa**, Object **rootAge**)
-----------------------------------------------------------------------------------------------

A tree of only extant species, which is conceptually embedded in a full species tree produced by a speciation-extinction (birth-death) branching process.<br>Conditioned on root age and on number of taxa.

### Parameters

- Object **lambda** - per-lineage birth rate.
- Object **mu** - per-lineage death rate.
- Object **n** - the number of taxa. optional.
- Object **taxa** - a string array of taxa id or a taxa object (e.g. dataframe, alignment or tree), optional.
- Object **rootAge** - the age of the root.

### Return type

- Object



BirthDeath(Object **diversification**, Object **turnover**, Object **rootAge**)
-------------------------------------------------------------------------------

The Birth-death-sampling tree distribution over tip-labelled time trees.<br>Conditioned on root age.

### Parameters

- Object **diversification** - diversification rate.
- Object **turnover** - turnover.
- Object **rootAge** - the number of taxa.

### Return type

- Object



