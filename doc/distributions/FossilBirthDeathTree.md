FossilBirthDeathTree distribution
=================================
FossilBirthDeathTree(Object **lambda**, Object **mu**, Object **rho**, Object **psi**, Object **n**, Taxa **taxa**)
-------------------------------------------------------------------------------------------------------------------

A tree of extant species and those sampled through time, which is conceptually embedded in a full species tree produced by a speciation-extinction (birth-death) branching process.<br>Conditioned on root age and (optionally) on number of *extant* taxa.

### Parameters

- Object **lambda** - per-lineage birth rate.
- Object **mu** - per-lineage death rate.
- Object **rho** - proportion of extant taxa sampled.
- Object **psi** - per-lineage sampling-through-time rate.
- Object **n** - the number of taxa. optional.
- Taxa **taxa** - Taxa object

### Return type

- Object



