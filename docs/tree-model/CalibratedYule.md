CalibratedYule distribution
===========================
CalibratedYule([Number](../types/Number.md) **lambda**, [Integer](../types/Integer.md) **n**, [Object](../types/Object.md) **cladeTaxa**, [Number[]](../types/Number[].md) **cladeMRCAAge**, [Object](../types/Object.md) **otherTaxa**, [Number](../types/Number.md) **rootAge**)
----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

The CalibratedYule method accepts one or more clade taxa and generates a tip-labelled time tree. If a root age is provided, the method conditions the tree generation on this root age.

### Parameters

- [Number](../types/Number.md) **lambda** - per-lineage birth rate, possibly scaled to mutations or calendar units.
- [Integer](../types/Integer.md) **n** - (optional) the total number of taxa.
- [Object](../types/Object.md) **cladeTaxa** - a string array of taxa id or a taxa object for clade taxa (e.g. dataframe, alignment or tree)
- [Number[]](../types/Number[].md) **cladeMRCAAge** - an array of ages for clade most recent common ancestor, ages should be correspond with clade taxa array.
- [Object](../types/Object.md) **otherTaxa** - (optional) a string array of taxa id or a taxa object for other taxa (e.g. dataframe, alignment or tree)
- [Number](../types/Number.md) **rootAge** - (optional) the root age to be conditioned on optional.

### Return type

[TimeTree](../types/TimeTree.md)


### Examples

- calibratedYule.lphy

### Reference

Heled, J., & Drummond, A. J. (2012). Calibrated tree priors for relaxed phylogenetics and divergence time estimation. Systematic biology, 61(1), 138–149. https://doi.org/10.1093/sysbio/syr087[http://doi.org/10.1093/sysbio/syr087](http://doi.org/10.1093/sysbio/syr087)

