AutoCorrelatedLogRates distribution
===================================
AutoCorrelatedLogRates([TimeTree](../types/TimeTree.md) **tree**, [Double](../types/Double.md) **sigma2**, [Double](../types/Double.md) **rootLogRate**, [Double[]](../types/Double[].md) **nodeLogRates**)
-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

This parametric distribution generates node-specific log-rates by a Brownian increment process
along the given time tree. The root node has a specified log-rate, and each child node's log-rate
is drawn from Normal( parentLogRate, sigma^2 * dt ), where dt is the time between parent and child.
This leads to an auto-correlated relaxation of the molecular clock across lineages.


### Parameters

- [TimeTree](../types/TimeTree.md) **tree** - 
- [Double](../types/Double.md) **sigma2** - 
- [Double](../types/Double.md) **rootLogRate** - 
- [Double[]](../types/Double[].md) **nodeLogRates** - (optional) 

### Return type

[Double[]](../types/Double[].md)


### Examples

- autoCorrelatedClock.lphy



