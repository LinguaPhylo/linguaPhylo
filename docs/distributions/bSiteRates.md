bSiteRates distribution
=======================
bSiteRates([Number](../types/Number.md) **shape**, [Integer](../types/Integer.md) **ncat**, [Integer](../types/Integer.md) **L**, [Number](../types/Number.md) **proportionInvariable**, [Boolean](../types/Boolean.md) **useShape**, [Boolean](../types/Boolean.md) **useProportionInvariable**)
-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

the site rates for the given bModelTest parameters.

### Parameters

- [Number](../types/Number.md) **shape** - the shape parameter of the discretized Gamma distribution.
- [Integer](../types/Integer.md) **ncat** - the number of categories of the discretized Gamma distribution.
- [Integer](../types/Integer.md) **L** - the number of sites to simulate.
- [Number](../types/Number.md) **proportionInvariable** - the proportion of invariable sites parameter
- [Boolean](../types/Boolean.md) **useShape** - true if the non-zero site rates follow a discretized Gamma distribution.
- [Boolean](../types/Boolean.md) **useProportionInvariable** - true if there is a proportion of invariable sites.

### Return type

[Double[]](../types/Double[].md)


### Examples

- simpleBModelTest2.lphy



