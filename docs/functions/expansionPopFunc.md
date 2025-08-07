ExpansionPopFunc function
=========================
ExpansionPopFunc([Double](../types/Double.md) **NA**, [Double](../types/Double.md) **r**, [Double](../types/Double.md) **NC**, [Double](../types/Double.md) **x**, [Integer](../types/Integer.md) **I_na**)
-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

Models population using a piecewise constant-exponential function with optional NA and I_na.

### Parameters

- [Double](../types/Double.md) **NA** - Ancestral population size.
- [Double](../types/Double.md) **r** - Exponential decay rate.
- [Double](../types/Double.md) **NC** - Population size for [0, x].
- [Double](../types/Double.md) **x** - Time boundary x.
- [Integer](../types/Integer.md) **I_na** - Indicator for NA usage (0 or 1).

### Return type

[PopulationFunction](../types/PopulationFunction.md)


### Examples

- expansionCoal.lphy



