taxa function
=============
taxa([Object[]](../types/Object[].md) **names**, [Object[]](../types/Object[].md) **species**, [Double[]](../types/Double[].md) **ages**)
-----------------------------------------------------------------------------------------------------------------------------------------

A set of taxa with species and ages defined in parallel arrays.

### Parameters

- [Object[]](../types/Object[].md) **names** - an array of objects representing taxa names
- [Object[]](../types/Object[].md) **species** - an array of objects representing species names
- [Double[]](../types/Double[].md) **ages** - the ages of the taxa

### Return type

[Taxa](../types/Taxa.md)


### Examples

- jcCoalescent.lphy
- simpleMultispeciesCoalescentTaxa.lphy



taxa([Object](../types/Object.md) **taxa**)
-------------------------------------------

The taxa of the given taxa-dimensioned object (e.g. alignment, tree et cetera).

### Parameters

- [Object](../types/Object.md) **taxa** - the taxa value (i.e. alignment or tree).

### Return type

[Taxa](../types/Taxa.md)



