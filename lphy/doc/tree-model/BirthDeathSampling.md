BirthDeathSampling distribution
===============================
BirthDeathSampling(Number **lambda**, Number **mu**, Number **rho**, Number **rootAge**)
----------------------------------------------------------------------------------------

The Birth-death-sampling tree distribution over tip-labelled time trees.<br>Conditioned on root age.

### Parameters

- Number **lambda** - per-lineage birth rate.
- Number **mu** - per-lineage death rate.
- Number **rho** - the sampling proportion.
- Number **rootAge** - the age of the root of the tree.

### Return type

- TimeTree

### Reference

Tanja Stadler, Roger Kouyos, ..., Sebastian Bonhoeffer, Estimating the Basic Reproductive Number from Viral Sequence Data, Molecular Biology and Evolution, Volume 29, Issue 1, January 2012.[https://doi.org/10.1093/molbev/msr217](https://doi.org/10.1093/molbev/msr217)

BirthDeathSampling(Number **diversification**, Number **turnover**, Number **rho**, Number **rootAge**)
-------------------------------------------------------------------------------------------------------

The Birth-death-sampling tree distribution over tip-labelled time trees.<br>Conditioned on root age.

### Parameters

- Number **diversification** - diversification rate.
- Number **turnover** - turnover.
- Number **rho** - the sampling proportion.
- Number **rootAge** - the age of the root node.

### Return type

- TimeTree

### Reference

Tanja Stadler, Mammalian phylogeny reveals recent diversification rate shifts, Proceedings of the National Academy of Sciences, 108 (15), 2011.[https://doi.org/10.1073/pnas.1016876108](https://doi.org/10.1073/pnas.1016876108)

