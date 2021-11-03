bSiteModel function
===================
bSiteModel(Double[][] **Q**, Double[] **siteRates**, Number **pInv**, Boolean **useSiteRates**, Boolean **usePInv**)
--------------------------------------------------------------------------------------------------------------------

Returns the site model for the given parameters.

### Parameters

- Double[][] **Q** - the instantaneous rate matrix.
- Double[] **siteRates** - raw site rates.
- Number **pInv** - the proportion of invariable sites parameter
- Boolean **useSiteRates** - true if the site rates have heterogeneity.
- Boolean **usePInv** - true if the proportion invariable used.

### Return type

- SiteModel

### Reference

Bouckaert, R., Drummond, A. bModelTest: Bayesian phylogenetic site model averaging and model comparison. BMC Evol Biol 17, 42 (2017). https://doi.org/10.1186/s12862-017-0890-6[https://doi.org/10.1186/s12862-017-0890-6](https://doi.org/10.1186/s12862-017-0890-6)

