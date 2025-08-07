AutoCorrelatedClock function
============================
AutoCorrelatedClock([TimeTree](../types/TimeTree.md) **tree**, [Double[]](../types/Double[].md) **nodeLogRates**, [Double](../types/Double.md) **rootLogRate**, [Double](../types/Double.md) **sigma2**, [Double](../types/Double.md) **meanRate**, [Boolean](../types/Boolean.md) **normalize**, [Integer](../types/Integer.md) **taylorOrder**)
-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

This deterministic function calculates the mean substitution rate on each branch
of a phylogeny under an autocorrelated Brownian motion (log-rate) model.
For each parent-child node pair, the average rate is computed via a bridging integral
(the so-called MeanZ approach). Optionally, the average rates can be normalized so that
the time-weighted mean rate over the entire tree is 1.0.


### Parameters

- [TimeTree](../types/TimeTree.md) **tree** - 
- [Double[]](../types/Double[].md) **nodeLogRates** - 
- [Double](../types/Double.md) **rootLogRate** - 
- [Double](../types/Double.md) **sigma2** - 
- [Double](../types/Double.md) **meanRate** - (optional) 
- [Boolean](../types/Boolean.md) **normalize** - (optional) 
- [Integer](../types/Integer.md) **taylorOrder** - (optional) 

### Return type

[Object](../types/Object.md)


### Examples

- autoCorrelatedClock.lphy



