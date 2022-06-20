FullBirthDeath distribution
===========================
FullBirthDeath(Number **lambda**, Number **mu**, Number **rootAge**, Number **originAge**)
------------------------------------------------------------------------------------------

A birth-death tree with both extant and extinct species.<br>Conditioned on age of root or origin.

### Parameters

- Number **lambda** - per-lineage birth rate.
- Number **mu** - per-lineage death rate.
- Number **rootAge** - the age of the root of the tree (only one of rootAge and originAge may be specified).
- Number **originAge** - the age of the origin of the tree  (only one of rootAge and originAge may be specified).

### Return type

- TimeTree

### Reference

David G. Kendall. On the Generalized "Birth-and-Death" Process, The Annals of Mathematical Statistics, Ann. Math. Statist. 19(1), 1-15, March, 1948.[https://doi.org/10.1214/aoms/1177730285](https://doi.org/10.1214/aoms/1177730285)

