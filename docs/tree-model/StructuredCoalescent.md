StructuredCoalescent distribution
=================================
StructuredCoalescent([Double[][]](../types/Double[][].md) **M**, [Integer[]](../types/Integer[].md) **k**, [Taxa](../types/Taxa.md) **taxa**, [Object[]](../types/Object[].md) **demes**, [Boolean](../types/Boolean.md) **sort**)
----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

The structured coalescent distribution over tip-labelled time trees.

### Parameters

- [Double[][]](../types/Double[][].md) **M** - The population process rate matrix which contains the effective population sizes and migration rates. Off-diagonal migration rates are in units of expected migrants per *generation* backwards in time.
- [Integer[]](../types/Integer[].md) **k** - (optional) the number of taxa in each population. provide either this or a demes argument.
- [Taxa](../types/Taxa.md) **taxa** - (optional) the taxa.
- [Object[]](../types/Object[].md) **demes** - (optional) the deme array, which runs parallel to the taxonArray in the taxa object.
- [Boolean](../types/Boolean.md) **sort** - (optional) whether to sort the deme array, before mapping them to the indices of the effective population sizes and migration rates. If not, as default, the pop size indices are determined by the natural order of the deme array, if true, then the indices are the order of sorted deme array.

### Return type

[TimeTree](../types/TimeTree.md)


### Examples

- https://linguaphylo.github.io/tutorials/structured-coalescent/

### Reference

Müller, N. F., Rasmussen, D. A., & Stadler, T. (2017). The structured coalescent and its approximations. Molecular biology and evolution, 34(11), 2970-2981.[https://doi.org/10.1093/molbev/msx186](https://doi.org/10.1093/molbev/msx186)

