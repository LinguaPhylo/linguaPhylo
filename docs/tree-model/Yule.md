Yule distribution
=================
Yule([Number](../types/Number.md) **lambda**, [Integer](../types/Integer.md) **n**, [Object](../types/Object.md) **taxa**, [Number](../types/Number.md) **rootAge**)
--------------------------------------------------------------------------------------------------------------------------------------------------------------------

The Yule tree distribution over tip-labelled time trees. Will be conditional on the root age if one is provided.

### Parameters

- [Number](../types/Number.md) **lambda** - per-lineage birth rate, possibly scaled to mutations or calendar units.
- [Integer](../types/Integer.md) **n** - the number of taxa.
- [Object](../types/Object.md) **taxa** - a string array of taxa id or a taxa object (e.g. dataframe, alignment or tree)
- [Number](../types/Number.md) **rootAge** - the root age to be conditioned on. optional.

### Return type

[TimeTree](../types/TimeTree.md)


### Examples

- simpleYule.lphy
- yuleRelaxed.lphy

### Reference

Yule, G. U. (1925). II.— A mathematical theory of evolution, based on the conclusions of Dr. JC Willis, FRS. Philosophical transactions of the Royal Society of London. Series B, containing papers of a biological character, 213(402-410), 21-87.[https://doi.org/10.1098/rstb.1925.0002](https://doi.org/10.1098/rstb.1925.0002)

