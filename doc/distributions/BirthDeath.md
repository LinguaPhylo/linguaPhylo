BirthDeath distribution
=======================
BirthDeath(Number **lambda**, Number **mu**, Integer **n**, Object **taxa**, Number **rootAge**)
------------------------------------------------------------------------------------------------

A tree of only extant species, which is conceptually embedded in a full species tree produced by a speciation-extinction (birth-death) branching process.<br>Conditioned on root age and on number of taxa.

### Parameters

- Number **lambda** - per-lineage birth rate.
- Number **mu** - per-lineage death rate.
- Integer **n** - the number of taxa. optional.
- Object **taxa** - a string array of taxa id or a taxa object (e.g. dataframe, alignment or tree), optional.
- Number **rootAge** - the age of the root.

### Return type

- TimeTree



BirthDeath(Number **diversification**, Number **turnover**, Number **rootAge**)
-------------------------------------------------------------------------------

The Birth-death-sampling tree distribution over tip-labelled time trees.<br>Conditioned on root age.

### Parameters

- Number **diversification** - diversification rate.
- Number **turnover** - turnover.
- Number **rootAge** - the number of taxa.

### Return type

- TimeTree



