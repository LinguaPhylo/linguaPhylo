SubsampledTree distribution
===========================
SubsampledTree([TimeTree](../types/TimeTree.md) **tree**, [String[][]](../types/String[][].md) **taxa**, [Double[]](../types/Double[].md) **sampleFraction**)
-------------------------------------------------------------------------------------------------------------------------------------------------------------

Generate the randomly sampled tree with given sample fractions and clade taxa name arrays within the given tree. The order of sample fractions are respectively matching the name arrays.

### Parameters

- [TimeTree](../types/TimeTree.md) **tree** - the full tree to extract taxa from.
- [String[][]](../types/String[][].md) **taxa** - the taxa name arrays that the function would sample
- [Double[]](../types/Double[].md) **sampleFraction** - the fractions that the function sample in the taxa

### Return type

[TimeTree](../types/TimeTree.md)



