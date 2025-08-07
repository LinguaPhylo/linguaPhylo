logisticPopFunc function
========================
logisticPopFunc([Double](../types/Double.md) **t50**, [Double](../types/Double.md) **nCarryingCapacity**, [Double](../types/Double.md) **b**, [Double](../types/Double.md) **NA**, [Integer](../types/Integer.md) **I_na**)
---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

Models population growth using a logistic growth function, with optional ancestral population size (NA) and indicator (I_na).

### Parameters

- [Double](../types/Double.md) **t50** - The logistic midpoint (inflection point).
- [Double](../types/Double.md) **nCarryingCapacity** - Carrying capacity (K).
- [Double](../types/Double.md) **b** - Logistic growth rate.
- [Double](../types/Double.md) **NA** - Ancestral population size (optional).
- [Integer](../types/Integer.md) **I_na** - Indicator for using NA (0 or 1).

### Return type

[PopulationFunction](../types/PopulationFunction.md)


### Examples

- logisticCoalescent.lphy
- logisticCoalJC.lphy



