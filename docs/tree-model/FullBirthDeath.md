FullBirthDeath distribution
===========================
FullBirthDeath([Number](../types/Number.md) **lambda**, [Number](../types/Number.md) **mu**, [Number](../types/Number.md) **rootAge**, [Number](../types/Number.md) **originAge**)
----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

A birth-death tree with both extant and extinct species.<br>Conditioned on age of root or origin.

### Parameters

- [Number](../types/Number.md) **lambda** - per-lineage birth rate.
- [Number](../types/Number.md) **mu** - per-lineage death rate.
- [Number](../types/Number.md) **rootAge** - (optional) the age of the root of the tree (only one of rootAge and originAge may be specified).
- [Number](../types/Number.md) **originAge** - (optional) the age of the origin of the tree  (only one of rootAge and originAge may be specified).

### Return type

[TimeTree](../types/TimeTree.md)


### Examples

- simpleFullBirthDeath.lphy

### Reference

David G. Kendall. On the Generalized "Birth-and-Death" Process, The Annals of Mathematical Statistics, Ann. Math. Statist. 19(1), 1-15, March, 1948.[https://doi.org/10.1214/aoms/1177730285](https://doi.org/10.1214/aoms/1177730285)

