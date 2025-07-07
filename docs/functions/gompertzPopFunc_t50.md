gompertzPopFunc_t50 function
============================
gompertzPopFunc_t50([Double](../types/Double.md) **t50**, [Number](../types/Number.md) **b**, [Number](../types/Number.md) **NInfinity**, [Number](../types/Number.md) **NA**, [Integer](../types/Integer.md) **I_na**)
-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

Constructs a GompertzPopulation_t50 model with optional NA and an I_na indicator.

### Parameters

- [Double](../types/Double.md) **t50** - Time when population is half of carrying capacity.
- [Number](../types/Number.md) **b** - Growth rate parameter (>0).
- [Number](../types/Number.md) **NInfinity** - Carrying capacity NInfinity (>0).
- [Number](../types/Number.md) **NA** - (optional) Ancestral population size (>=0).
- [Integer](../types/Integer.md) **I_na** - (optional) Indicator (0 or 1). If 1 and NA>0 => use NA.

### Return type

[PopulationFunction](../types/PopulationFunction.md)


### Examples

- gomp_t50_jc69.lphy
- gomp_t50_gt16.lphy



