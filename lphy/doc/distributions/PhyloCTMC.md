PhyloCTMC distribution
======================
PhyloCTMC([TimeTree](../types/TimeTree.md) **tree**, [Number](../types/Number.md) **mu**, [Double[]](../types/Double[].md) **freq**, [Double[][]](../types/Double[][].md) **Q**, [Double[]](../types/Double[].md) **siteRates**, [Double[]](../types/Double[].md) **branchRates**, [Integer](../types/Integer.md) **L**, [SequenceType](../types/SequenceType.md) **dataType**, [SimpleAlignment](../types/SimpleAlignment.md) **root**)
----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

The phylogenetic continuous-time Markov chain distribution. A sequence is simulated for every leaf node, and every direct ancestor node with an id.(The sampling distribution that the phylogenetic likelihood is derived from.)

### Parameters

- [TimeTree](../types/TimeTree.md) **tree** - the time tree.
- [Number](../types/Number.md) **mu** - the clock rate. Default value is 1.0.
- [Double[]](../types/Double[].md) **freq** - the root probabilities. Optional parameter. If not specified then first row of e^{100*Q) is used.
- [Double[][]](../types/Double[][].md) **Q** - the instantaneous rate matrix.
- [Double[]](../types/Double[].md) **siteRates** - a rate for each site in the alignment. Site rates are assumed to be 1.0 otherwise.
- [Double[]](../types/Double[].md) **branchRates** - a rate for each branch in the tree. Branch rates are assumed to be 1.0 otherwise.
- [Integer](../types/Integer.md) **L** - length of the alignment
- [SequenceType](../types/SequenceType.md) **dataType** - the data type used for simulations, default to nucleotide
- [SimpleAlignment](../types/SimpleAlignment.md) **root** - root sequence, defaults to root sequence generated from equilibrium frequencies.

### Return type

[Alignment](../types/Alignment.md)


### Examples

- gtrGammaCoalescent.lphy
- errorModel1.lphy

### Reference

Felsenstein, J. (1981). Evolutionary trees from DNA sequences: a maximum likelihood approach. Journal of molecular evolution, 17(6), 368-376.[https://doi.org/10.1007/BF01734359](https://doi.org/10.1007/BF01734359)

PhyloCTMC([TimeTree](../types/TimeTree.md) **tree**, [Number](../types/Number.md) **mu**, [Double[]](../types/Double[].md) **freq**, [SiteModel](../types/SiteModel.md) **siteModel**, [Double[]](../types/Double[].md) **branchRates**, [Integer](../types/Integer.md) **L**, [SequenceType](../types/SequenceType.md) **dataType**)
-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

The phylogenetic continuous-time Markov chain distribution. A sequence is simulated for every leaf node, and every direct ancestor node with an id.(The sampling distribution that the phylogenetic likelihood is derived from.)

### Parameters

- [TimeTree](../types/TimeTree.md) **tree** - the time tree.
- [Number](../types/Number.md) **mu** - the clock rate. Default value is 1.0.
- [Double[]](../types/Double[].md) **freq** - the root probabilities. Optional parameter. If not specified then first row of e^{100*Q) is used.
- [SiteModel](../types/SiteModel.md) **siteModel** - the site model
- [Double[]](../types/Double[].md) **branchRates** - a rate for each branch in the tree. Branch rates are assumed to be 1.0 otherwise.
- [Integer](../types/Integer.md) **L** - length of the alignment
- [SequenceType](../types/SequenceType.md) **dataType** - the data type used for simulations, default to nucleotide

### Return type

[Alignment](../types/Alignment.md)


### Examples

- simpleBModelTest.lphy

### Reference

Felsenstein, J. (1981). Evolutionary trees from DNA sequences: a maximum likelihood approach. Journal of molecular evolution, 17(6), 368-376.[https://doi.org/10.1007/BF01734359](https://doi.org/10.1007/BF01734359)

