Coalescent distribution
=======================
Coalescent([Number](../types/Number.md) **theta**, [Integer](../types/Integer.md) **n**, [Taxa](../types/Taxa.md) **taxa**, [Double[]](../types/Double[].md) **ages**)
----------------------------------------------------------------------------------------------------------------------------------------------------------------------

The Kingman coalescent with serially sampled data. (Rodrigo and Felsenstein, 1999)

### Parameters

- [Number](../types/Number.md) **theta** - effective population size, possibly scaled to mutations or calendar units.
- [Integer](../types/Integer.md) **n** - (optional) number of taxa.
- [Taxa](../types/Taxa.md) **taxa** - (optional) Taxa object, (e.g. Taxa or TimeTree or Object[])
- [Double[]](../types/Double[].md) **ages** - (optional) an array of leaf node ages.

### Return type

[TimeTree](../types/TimeTree.md)


### Examples

- https://linguaphylo.github.io/tutorials/time-stamped-data/

### Reference

Rodrigo AG, Felsenstein J. (1999). Coalescent Approaches to HIV Population Genetics, The Evolution of HIV, Chapter 8, edited by Crandall K., Johns Hopkins Univ. Press, Baltimore.

