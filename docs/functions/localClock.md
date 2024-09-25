localClock function
===================
localClock([TimeTree](../types/TimeTree.md) **tree**, [Object[]](../types/Object[].md) **clades**, [Double[]](../types/Double[].md) **cladeRates**, [Double](../types/Double.md) **rootRate**, [Boolean](../types/Boolean.md) **includeStem**)
----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

Apply local clock in a phylogenetic tree to generate a tree with branch rates. The order of elements in clades and cladeRates array should match. The clades should not be overlapped with each other.

### Parameters

- [TimeTree](../types/TimeTree.md) **tree** - the tree used to calculate branch rates
- [Object[]](../types/Object[].md) **clades** - the array of the node names
- [Double[]](../types/Double[].md) **cladeRates** - the array of clade rates
- [Double](../types/Double.md) **rootRate** - the root rate of the tree
- [Boolean](../types/Boolean.md) **includeStem** - (optional) whether to include stem of clades, defaults to true

### Return type

[Double[]](../types/Double[].md)


### Examples

- substituteClade.lphy



