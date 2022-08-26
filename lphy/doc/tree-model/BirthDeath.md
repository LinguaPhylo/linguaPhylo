BirthDeath distribution
=======================
BirthDeath([Number](../types/Number.md) **lambda**, [Number](../types/Number.md) **mu**, [Integer](../types/Integer.md) **n**, [Object](../types/Object.md) **taxa**, [Number](../types/Number.md) **rootAge**)
---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

A tree of only extant species, which is conceptually embedded<br>in a full species tree produced by a speciation-extinction (birth-death) branching process.<br>Conditioned on root age and on number of taxa.

### Parameters

- [Number](../types/Number.md) **lambda** - per-lineage birth rate.
- [Number](../types/Number.md) **mu** - per-lineage death rate.
- [Integer](../types/Integer.md) **n** - the number of taxa. optional.
- [Object](../types/Object.md) **taxa** - a string array of taxa id or a taxa object (e.g. dataframe, alignment or tree), optional.
- [Number](../types/Number.md) **rootAge** - the age of the root.

### Return type

[TimeTree](../types/TimeTree.md)

### Reference

Joseph Heled, Alexei J. Drummond, Calibrated Birth–Death Phylogenetic Time-Tree Priors for Bayesian Inference, Systematic Biology, Volume 64, Issue 3, May 2015.[https://doi.org/10.1093/sysbio/syu089](https://doi.org/10.1093/sysbio/syu089)

BirthDeath([Number](../types/Number.md) **diversification**, [Number](../types/Number.md) **turnover**, [Number](../types/Number.md) **rootAge**)
-------------------------------------------------------------------------------------------------------------------------------------------------

The Birth-death-sampling tree distribution over tip-labelled time trees.<br>Conditioned on root age.

### Parameters

- [Number](../types/Number.md) **diversification** - diversification rate.
- [Number](../types/Number.md) **turnover** - turnover.
- [Number](../types/Number.md) **rootAge** - the number of taxa.

### Return type

[TimeTree](../types/TimeTree.md)



