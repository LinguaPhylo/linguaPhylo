SimBDReverse distribution
=========================
SimBDReverse([Number](../types/Number.md) **lambda**, [Number](../types/Number.md) **mu**, [Taxa](../types/Taxa.md) **taxa**, [Number](../types/Number.md) **rho**)
-------------------------------------------------------------------------------------------------------------------------------------------------------------------

A complete birth-death tree with both extant and extinct species.<br>Conditioned on (a fraction of) extant taxa.

### Parameters

- [Number](../types/Number.md) **lambda** - per-lineage birth rate.
- [Number](../types/Number.md) **mu** - per-lineage death rate.
- [Taxa](../types/Taxa.md) **taxa** - The extant taxa that this process are conditioned on
- [Number](../types/Number.md) **rho** - The fraction of total extant species that the conditioned-on taxa represent. The resulting tree will have taxa.ntaxa()/rho total extant taxa.

### Return type

[TimeTree](../types/TimeTree.md)


### Examples

- simFossils.lphy



