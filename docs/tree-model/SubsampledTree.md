SubsampledTree distribution
===========================
SubsampledTree([TimeTree](../types/TimeTree.md) **tree**, [String[][]](../types/String[][].md) **taxa**, [Double[]](../types/Double[].md) **sampleFraction**)
-------------------------------------------------------------------------------------------------------------------------------------------------------------

Generate a randomly sampled tree using the given sample fractions and clade taxa name arrays from the specified tree. The order of sample fractions corresponds to the order of the name arrays.

### Parameters

- [TimeTree](../types/TimeTree.md) **tree** - the full tree to extract taxa from.
- [String[][]](../types/String[][].md) **taxa** - the taxa name arrays that the function would sample
- [Double[]](../types/Double[].md) **sampleFraction** - the fractions that the function sample in the taxa

### Return type

[TimeTree](../types/TimeTree.md)


### Examples

- SubsampledTree.lphy



