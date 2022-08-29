SimFBDAge distribution
======================
SimFBDAge([Number](../types/Number.md) **lambda**, [Number](../types/Number.md) **mu**, [Double](../types/Double.md) **frac**, [Number](../types/Number.md) **psi**, [Number](../types/Number.md) **originAge**)
----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

A tree of extant species and those sampled through time, which is conceptually embedded in a full species tree produced by a speciation-extinction (birth-death) branching process.<br>Conditioned on origin age.

### Parameters

- [Number](../types/Number.md) **lambda** - per-lineage birth rate.
- [Number](../types/Number.md) **mu** - per-lineage death rate.
- [Double](../types/Double.md) **frac** - fraction of extant taxa sampled.
- [Number](../types/Number.md) **psi** - per-lineage sampling-through-time rate.
- [Number](../types/Number.md) **originAge** - the age of the origin.

### Return type

[TimeTree](../types/TimeTree.md)


### Examples

- simFBDAge.lphy



