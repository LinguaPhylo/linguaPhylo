migrationMatrix function
========================
migrationMatrix([Double[]](../types/Double[].md) **theta**, [Double[]](../types/Double[].md) **m**)
---------------------------------------------------------------------------------------------------

This function constructs the population process rate matrix. Diagonals are the population sizes, off-diagonals are populated with the migration rate from pop i to pop j (backwards in time in units of expected migrants per generation).

### Parameters

- [Double[]](../types/Double[].md) **theta** - the population sizes.
- [Double[]](../types/Double[].md) **m** - the migration rates between each pair of demes (row-major order minus diagonals).

### Return type

[Double[][]](../types/Double[][].md)


### Examples

- simpleStructuredCoalescent.lphy
- https://linguaphylo.github.io/tutorials/structured-coalescent/



