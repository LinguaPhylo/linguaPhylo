gompertzPopFunc_f0 function
===========================
gompertzPopFunc_f0([Double](../types/Double.md) **b**, [Double](../types/Double.md) **N0**, [Double](../types/Double.md) **f0**, [Double](../types/Double.md) **NA**, [Integer](../types/Integer.md) **I_na**)
--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

Constructs a Gompertz population model (f0-parameterized) with optional NA and indicator I_na.

### Parameters

- [Double](../types/Double.md) **b** - Gompertz growth rate (b).
- [Double](../types/Double.md) **N0** - Initial population size (N0).
- [Double](../types/Double.md) **f0** - Initial proportion, f0 = N0 / NInfinity.
- [Double](../types/Double.md) **NA** - Ancestral population size (NA).
- [Integer](../types/Integer.md) **I_na** - Indicator (0 or 1) controlling usage of NA.

### Return type

[PopulationFunction](../types/PopulationFunction.md)


### Examples

- gompertzF0Coal.lphy



