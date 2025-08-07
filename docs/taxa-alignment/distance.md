distance function
=================
distance([Alignment](../types/Alignment.md) **alignment**, [String](../types/String.md) **m**)
----------------------------------------------------------------------------------------------

It computes a matrix of pairwise distances from a given alignment using an evolutionary model. If there is a gap or an ambiguous state, it is treated as different from any canonical states.

### Parameters

- [Alignment](../types/Alignment.md) **alignment** - the alignment (no unambiguous states).
- [String](../types/String.md) **m** - (optional) the evolutionary model, such as JC96 or p, default to p (Hamming) distance

### Return type

[Double[][]](../types/Double[][].md)



