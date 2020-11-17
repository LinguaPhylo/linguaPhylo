migrationMatrix function
========================
migrationMatrix(Object **theta**, Object **m**)
-----------------------------------------------

This function constructs the population process rate matrix. Diagonals are the population sizes, off-diagonals are populated with the migration rate from pop i to pop j (backwards in time in units of expected migrants per generation).

### Parameters

- Object **theta** - the population sizes.
- Object **m** - the migration rates between each pair of demes (row-major order minus diagonals).

### Return type

- Object



