BirthDeathSampling distribution
===============================
BirthDeathSampling([Number](../types/Number.md) **lambda**, [Number](../types/Number.md) **mu**, [Number](../types/Number.md) **rho**, [Number](../types/Number.md) **rootAge**)
--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

The Birth-death-sampling tree distribution over tip-labelled time trees.<br>Conditioned on root age.

### Parameters

- [Number](../types/Number.md) **lambda** - per-lineage birth rate.
- [Number](../types/Number.md) **mu** - per-lineage death rate.
- [Number](../types/Number.md) **rho** - the sampling proportion.
- [Number](../types/Number.md) **rootAge** - the age of the root of the tree.

### Return type

[TimeTree](../types/TimeTree.md)


### Examples

- simpleBirthDeath.lphy

### Reference

Tanja Stadler, Roger Kouyos, ..., Sebastian Bonhoeffer, Estimating the Basic Reproductive Number from Viral Sequence Data, Molecular Biology and Evolution, Volume 29, Issue 1, January 2012.[https://doi.org/10.1093/molbev/msr217](https://doi.org/10.1093/molbev/msr217)

BirthDeathSampling([Number](../types/Number.md) **diversification**, [Number](../types/Number.md) **turnover**, [Number](../types/Number.md) **rho**, [Number](../types/Number.md) **rootAge**)
-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

The Birth-death-sampling tree distribution over tip-labelled time trees.<br>Conditioned on root age.

### Parameters

- [Number](../types/Number.md) **diversification** - diversification rate.
- [Number](../types/Number.md) **turnover** - turnover.
- [Number](../types/Number.md) **rho** - the sampling proportion.
- [Number](../types/Number.md) **rootAge** - the age of the root node.

### Return type

[TimeTree](../types/TimeTree.md)


### Examples

- birthDeathRhoSampling.lphy

### Reference

Tanja Stadler, Mammalian phylogeny reveals recent diversification rate shifts, Proceedings of the National Academy of Sciences, 108 (15), 2011.[https://doi.org/10.1073/pnas.1016876108](https://doi.org/10.1073/pnas.1016876108)

