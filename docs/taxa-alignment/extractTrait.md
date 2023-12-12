extractTrait function
=====================
extractTrait([Taxa](../types/Taxa.md) **taxa**, [String](../types/String.md) **sep**, [Integer](../types/Integer.md) **i**, [String](../types/String.md) **name**)
------------------------------------------------------------------------------------------------------------------------------------------------------------------

return a trait alignment, which contains the set of traits extracted from taxa names.

### Parameters

- [Taxa](../types/Taxa.md) **taxa** - the set of taxa whose names contain the traits.
- [String](../types/String.md) **sep** - the substring to split the taxa names, where Java regular expression escape characters will be given no special meaning.
- [Integer](../types/Integer.md) **i** - i (>=0) is the index to extract the trait value.
- [String](../types/String.md) **name** - (optional) the map containing optional arguments and their values for reuse.

### Return type

[Alignment](../types/Alignment.md)


### Examples

- covidDPG.lphy
- https://linguaphylo.github.io/tutorials/discrete-phylogeography/



