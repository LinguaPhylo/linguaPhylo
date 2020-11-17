BirthDeathSerialSampling distribution
=====================================
BirthDeathSerialSampling(Object **lambda**, Object **mu**, Object **rho**, Object **psi**, Object **n**, Object **taxa**, Object **ages**, Object **rootAge**)
--------------------------------------------------------------------------------------------------------------------------------------------------------------

A tree of extant species and those sampled through time, which is conceptually embedded in a full species tree produced by a speciation-extinction (birth-death) branching process.<br>Conditioned on root age and on number of taxa and their ages (Stadler and Yang, 2013).

### Parameters

- Object **lambda** - per-lineage birth rate.
- Object **mu** - per-lineage death rate.
- Object **rho** - proportion of extant taxa sampled.
- Object **psi** - per-lineage sampling-through-time rate.
- Object **n** - the number of taxa. optional.
- Object **taxa** - Taxa object
- Object **ages** - an array of leaf node ages.
- Object **rootAge** - the age of the root.

### Return type

- Object

### Reference

Tanja Stadler, Ziheng Yang (2013) Dating Phylogenies with Sequentially Sampled Tips, Systematic Biology, 62(5):674–688[http://doi.org/10.1093/sysbio/syt030](http://doi.org/10.1093/sysbio/syt030)

