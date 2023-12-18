PhyloOU distribution
====================
PhyloOU([TimeTree](../types/TimeTree.md) **tree**, [Double](../types/Double.md) **diffRate**, [Double](../types/Double.md) **theta**, [Double](../types/Double.md) **alpha**, [Double](../types/Double.md) **y0**, [Double[]](../types/Double[].md) **branchThetas**)
---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

The phylogenetic Ornstein-Ulhenbeck distribution. A continous trait is simulated for every leaf node, and every direct ancestor node with an id.

### Parameters

- [TimeTree](../types/TimeTree.md) **tree** - the time tree.
- [Double](../types/Double.md) **diffRate** - the variance of the underlying Brownian process. This is not the equilibrium variance of the OU process.
- [Double](../types/Double.md) **theta** - (optional) the 'optimal' value that the long-term process is centered around.
- [Double](../types/Double.md) **alpha** - the drift term that determines the rate of drift towards the optimal value.
- [Double](../types/Double.md) **y0** - the value of continuous trait at the root.
- [Double[]](../types/Double[].md) **branchThetas** - (optional) the 'optimal' value for each branch in the tree.

### Return type

[ContinuousCharacterData](../types/ContinuousCharacterData.md)


### Examples

- simplePhyloOU.lphy

### Reference

Felsenstein J. (1973). Maximum-likelihood estimation of evolutionary trees from continuous characters. American journal of human genetics, 25(5), 471–492.[https://www.ncbi.nlm.nih.gov/pmc/articles/PMC1762641/](https://www.ncbi.nlm.nih.gov/pmc/articles/PMC1762641/)

