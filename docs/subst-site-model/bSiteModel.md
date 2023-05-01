bSiteModel function
===================
bSiteModel([Double[][]](../types/Double[][].md) **Q**, [Double[]](../types/Double[].md) **siteRates**, [Number](../types/Number.md) **pInv**, [Boolean](../types/Boolean.md) **useSiteRates**, [Boolean](../types/Boolean.md) **usePInv**)
------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

Returns the site model for the given parameters.

### Parameters

- [Double[][]](../types/Double[][].md) **Q** - the instantaneous rate matrix.
- [Double[]](../types/Double[].md) **siteRates** - raw site rates.
- [Number](../types/Number.md) **pInv** - the proportion of invariable sites parameter
- [Boolean](../types/Boolean.md) **useSiteRates** - true if the site rates have heterogeneity.
- [Boolean](../types/Boolean.md) **usePInv** - true if the proportion invariable used.

### Return type

[SiteModel](../types/SiteModel.md)


### Examples

- simpleBModelTest.lphy

### Reference

Bouckaert, R., Drummond, A. bModelTest: Bayesian phylogenetic site model averaging and model comparison. BMC Evol Biol 17, 42 (2017). https://doi.org/10.1186/s12862-017-0890-6[https://doi.org/10.1186/s12862-017-0890-6](https://doi.org/10.1186/s12862-017-0890-6)

