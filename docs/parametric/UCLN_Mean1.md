UCLN_Mean1 distribution
=======================
UCLN_Mean1([Number](../types/Number.md) **uclnSigma**, [TimeTree](../types/TimeTree.md) **tree**)
-------------------------------------------------------------------------------------------------

The uncorrelated lognormal (UCLN) relaxed clock model, where the mean of log-normal distr on branch rates in real space must be fixed to 1. Use the clock rate (mu) in PhyloCTMC as the expected mean clock rate.

### Parameters

- [Number](../types/Number.md) **uclnSigma** - The standard deviation of the expected lognormal distribution on branch rates.
- [TimeTree](../types/TimeTree.md) **tree** - The tree.

### Return type

[Double[]](../types/Double[].md)

### Reference

Douglas, J., Zhang, R., & Bouckaert, R. (2021). Adaptive dating and fast proposals: Revisiting the phylogenetic relaxed clock model. PLoS computational biology, 17(2), e1008322.[https://doi.org/10.1371/journal.pcbi.1008322](https://doi.org/10.1371/journal.pcbi.1008322)

