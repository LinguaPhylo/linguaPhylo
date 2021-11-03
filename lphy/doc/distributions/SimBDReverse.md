SimBDReverse distribution
=========================
SimBDReverse(Number **lambda**, Number **mu**, Taxa **taxa**, Number **rho**)
-----------------------------------------------------------------------------

A complete birth-death tree with both extant and extinct species.<br>Conditioned on (a fraction of) extant taxa.

### Parameters

- Number **lambda** - per-lineage birth rate.
- Number **mu** - per-lineage death rate.
- Taxa **taxa** - The extant taxa that this process are conditioned on
- Number **rho** - The fraction of total extant species that the conditioned-on taxa represent. The resulting tree will have taxa.ntaxa()/rho total extant taxa.

### Return type

- TimeTree



