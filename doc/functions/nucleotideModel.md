nucleotideModel function
========================
nucleotideModel(BModelSet **modelSet**, Integer **modelIndicator**, Double[] **rates**, Double[] **freq**, Number **meanRate**)
-------------------------------------------------------------------------------------------------------------------------------

The instantaneous rate matrix. Takes relative rates and base frequencies and produces an GTR rate matrix.

### Parameters

- BModelSet **modelSet** - The set of models to choose from. Valid value are: allreversible, transitionTransversionSplit, namedSimple, namedExtended.
- Integer **modelIndicator** - the index of the model to be employed
- Double[] **rates** - the relative rates of the GTR process.
- Double[] **freq** - the base frequencies.
- Number **meanRate** - the rate of substitution.

### Return type

- Double[][]

### Reference

Bouckaert, R., Drummond, A. bModelTest: Bayesian phylogenetic site model averaging and model comparison. BMC Evol Biol 17, 42 (2017). https://doi.org/10.1186/s12862-017-0890-6[https://doi.org/10.1186/s12862-017-0890-6](https://doi.org/10.1186/s12862-017-0890-6)

