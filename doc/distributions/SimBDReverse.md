SimBDReverse distribution
=========================
SimBDReverse(Object **lambda**, Object **mu**, Object **taxa**, Object **rho**)
-------------------------------------------------------------------------------

A complete birth-death tree with both extant and extinct species.<br>Conditioned on (a fraction of) extant taxa.

### Parameters

- Object **lambda** - per-lineage birth rate.
- Object **mu** - per-lineage death rate.
- Object **taxa** - The extant taxa that this process are conditioned on
- Object **rho** - The fraction of total extant species that the conditioned-on taxa represent. The resulting tree will have taxa.ntaxa()/rho total extant taxa.

### Return type

- Object



