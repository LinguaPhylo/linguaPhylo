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



