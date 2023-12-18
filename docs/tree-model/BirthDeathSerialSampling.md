BirthDeathSerialSampling distribution
=====================================
BirthDeathSerialSampling([Number](../types/Number.md) **lambda**, [Number](../types/Number.md) **mu**, [Number](../types/Number.md) **rho**, [Number](../types/Number.md) **psi**, [Integer](../types/Integer.md) **n**, [Taxa](../types/Taxa.md) **taxa**, [Double[]](../types/Double[].md) **ages**, [Number](../types/Number.md) **rootAge**)
------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

A tree of extant species and those sampled through time, which is conceptually embedded in a full species tree produced by a speciation-extinction (birth-death) branching process.<br>Conditioned on root age and on number of taxa and their ages (Stadler and Yang, 2013).

### Parameters

- [Number](../types/Number.md) **lambda** - per-lineage birth rate.
- [Number](../types/Number.md) **mu** - per-lineage death rate.
- [Number](../types/Number.md) **rho** - proportion of extant taxa sampled.
- [Number](../types/Number.md) **psi** - per-lineage sampling-through-time rate.
- [Integer](../types/Integer.md) **n** - (optional) the number of taxa. optional.
- [Taxa](../types/Taxa.md) **taxa** - (optional) Taxa object
- [Double[]](../types/Double[].md) **ages** - (optional) an array of leaf node ages.
- [Number](../types/Number.md) **rootAge** - the age of the root.

### Return type

[TimeTree](../types/TimeTree.md)


### Examples

- simpleBirthDeathSerial.lphy

### Reference

Tanja Stadler, Ziheng Yang (2013) Dating Phylogenies with Sequentially Sampled Tips, Systematic Biology, 62(5):674–688[http://doi.org/10.1093/sysbio/syt030](http://doi.org/10.1093/sysbio/syt030)

