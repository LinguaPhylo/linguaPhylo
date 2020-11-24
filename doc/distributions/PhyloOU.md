PhyloOU distribution
====================
PhyloOU(TimeTree **tree**, Double **diffRate**, Double **theta**, Double **alpha**, Double **y0**, Double[] **branchThetas**)
-----------------------------------------------------------------------------------------------------------------------------

### Parameters

- TimeTree **tree** - the time tree.
- Double **diffRate** - the variance of the underlying Brownian process. This is not the equilibrium variance of the OU process.
- Double **theta** - the 'optimal' value that the long-term process is centered around.
- Double **alpha** - the drift term that determines the rate of drift towards the optimal value.
- Double **y0** - the value of continuous trait at the root.
- Double[] **branchThetas** - the 'optimal' value for each branch in the tree.

### Return type

- ContinuousCharacterData



