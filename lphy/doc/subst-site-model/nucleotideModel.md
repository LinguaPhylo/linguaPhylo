nucleotideModel function
========================
nucleotideModel([BModelSet](../types/BModelSet.md) **modelSet**, [Integer](../types/Integer.md) **modelIndicator**, [Double[]](../types/Double[].md) **rates**, [Double[]](../types/Double[].md) **freq**, [Number](../types/Number.md) **meanRate**)
-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

The instantaneous rate matrix. Takes relative rates and base frequencies and produces an GTR rate matrix.

### Parameters

- [BModelSet](../types/BModelSet.md) **modelSet** - The set of models to choose from. Valid value are: allreversible, transitionTransversionSplit, namedSimple, namedExtended.
- [Integer](../types/Integer.md) **modelIndicator** - the index of the model to be employed
- [Double[]](../types/Double[].md) **rates** - the relative rates of the GTR process.
- [Double[]](../types/Double[].md) **freq** - the base frequencies.
- [Number](../types/Number.md) **meanRate** - the rate of substitution.

### Return type

[Double[][]](../types/Double[][].md)

### Reference

Bouckaert, R., Drummond, A. bModelTest: Bayesian phylogenetic site model averaging and model comparison. BMC Evol Biol 17, 42 (2017). https://doi.org/10.1186/s12862-017-0890-6[https://doi.org/10.1186/s12862-017-0890-6](https://doi.org/10.1186/s12862-017-0890-6)

