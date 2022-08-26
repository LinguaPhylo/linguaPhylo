RhoSampleTree distribution
==========================
RhoSampleTree([TimeTree](../types/TimeTree.md) **tree**, [Number](../types/Number.md) **rho**)
----------------------------------------------------------------------------------------------

A tree sampled from a larger tree by selecting tips at time zero with probability rho.<br>Conditioned on root age.

### Parameters

- [TimeTree](../types/TimeTree.md) **tree** - the full tree to sample
- [Number](../types/Number.md) **rho** - the probability that each tip at time zero is sampled

### Return type

[TimeTree](../types/TimeTree.md)



