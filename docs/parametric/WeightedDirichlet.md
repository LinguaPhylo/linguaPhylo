WeightedDirichlet distribution
==============================
WeightedDirichlet([Number[]](../types/Number[].md) **conc**, [Integer[]](../types/Integer[].md) **weights**, [Number](../types/Number.md) **mean**)
---------------------------------------------------------------------------------------------------------------------------------------------------

The scaled dirichlet probability distribution. The weighted mean of values must equal to the expected weighted mean (default to 1).

### Parameters

- [Number[]](../types/Number[].md) **conc** - the concentration parameters of the scaled Dirichlet distribution.
- [Integer[]](../types/Integer[].md) **weights** - the relative weight parameters of the scaled Dirichlet distribution.
- [Number](../types/Number.md) **mean** - (optional) the expected weighted mean of the values, default to 1.

### Return type

[Double[]](../types/Double[].md)


### Examples

- totalEvidence.lphy
- weightedDirichlet.lphy



