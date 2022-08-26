FossilBirthDeathTree distribution
=================================
FossilBirthDeathTree([Number](../types/Number.md) **lambda**, [Number](../types/Number.md) **mu**, [Number](../types/Number.md) **rho**, [Number](../types/Number.md) **psi**, [Integer](../types/Integer.md) **n**, [Taxa](../types/Taxa.md) **taxa**)
-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

A tree of extant species and those sampled through time, which is conceptually embedded in a full species tree produced by a speciation-extinction (birth-death) branching process.<br>Conditioned on root age and (optionally) on number of *extant* taxa.

### Parameters

- [Number](../types/Number.md) **lambda** - per-lineage birth rate.
- [Number](../types/Number.md) **mu** - per-lineage death rate.
- [Number](../types/Number.md) **rho** - proportion of extant taxa sampled.
- [Number](../types/Number.md) **psi** - per-lineage sampling-through-time rate.
- [Integer](../types/Integer.md) **n** - the number of taxa. optional.
- [Taxa](../types/Taxa.md) **taxa** - Taxa object

### Return type

[TimeTree](../types/TimeTree.md)

### Reference

Tracy A. Heath, John P. Huelsenbeck, and Tanja Stadler, The fossilized birth–death process for coherent calibration of divergence-time estimates, Proceedings of the National Academy of Sciences, 111 (29), 2014.[https://doi.org/10.1073/pnas.1319091111](https://doi.org/10.1073/pnas.1319091111)

