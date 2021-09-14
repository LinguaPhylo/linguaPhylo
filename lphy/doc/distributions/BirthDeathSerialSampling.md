BirthDeathSerialSampling distribution
=====================================
BirthDeathSerialSampling(Number **lambda**, Number **mu**, Number **rho**, Number **psi**, Integer **n**, Taxa **taxa**, Double[] **ages**, Number **rootAge**)
---------------------------------------------------------------------------------------------------------------------------------------------------------------

A tree of extant species and those sampled through time, which is conceptually embedded in a full species tree produced by a speciation-extinction (birth-death) branching process.<br>Conditioned on root age and on number of taxa and their ages (Stadler and Yang, 2013).

### Parameters

- Number **lambda** - per-lineage birth rate.
- Number **mu** - per-lineage death rate.
- Number **rho** - proportion of extant taxa sampled.
- Number **psi** - per-lineage sampling-through-time rate.
- Integer **n** - the number of taxa. optional.
- Taxa **taxa** - Taxa object
- Double[] **ages** - an array of leaf node ages.
- Number **rootAge** - the age of the root.

### Return type

- TimeTree

### Reference

Tanja Stadler, Ziheng Yang (2013) Dating Phylogenies with Sequentially Sampled Tips, Systematic Biology, 62(5):674–688[http://doi.org/10.1093/sysbio/syt030](http://doi.org/10.1093/sysbio/syt030)

