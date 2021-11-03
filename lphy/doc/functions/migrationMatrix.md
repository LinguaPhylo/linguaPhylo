migrationMatrix function
========================
migrationMatrix(Double[] **theta**, Double[] **m**)
---------------------------------------------------

This function constructs the population process rate matrix. Diagonals are the population sizes, off-diagonals are populated with the migration rate from pop i to pop j (backwards in time in units of expected migrants per generation).

### Parameters

- Double[] **theta** - the population sizes.
- Double[] **m** - the migration rates between each pair of demes (row-major order minus diagonals).

### Return type

- Double[][]



