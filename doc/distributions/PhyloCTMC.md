PhyloCTMC distribution
======================
PhyloCTMC(TimeTree **tree**, Number **mu**, Double[] **freq**, Double[][] **Q**, Double[] **siteRates**, Double[] **branchRates**, Integer **L**, Object **dataType**)
----------------------------------------------------------------------------------------------------------------------------------------------------------------------

The phylogenetic continuous-time Markov chain distribution. A sequence is simulated for every leaf node, and every direct ancestor node with an id.(The sampling distribution that the phylogenetic likelihood is derived from.)

### Parameters

- TimeTree **tree** - the time tree.
- Number **mu** - the clock rate. Default value is 1.0.
- Double[] **freq** - the root probabilities. Optional parameter. If not specified then first row of e^{100*Q) is used.
- Double[][] **Q** - the instantaneous rate matrix.
- Double[] **siteRates** - a rate for each site in the alignment. Site rates are assumed to be 1.0 otherwise.
- Double[] **branchRates** - a rate for each branch in the tree. Branch rates are assumed to be 1.0 otherwise.
- Integer **L** - length of the alignment
- Object **dataType** - the data type used for simulations

### Return type

- Alignment

### Reference

Felsenstein, J. (1981). Evolutionary trees from DNA sequences: a maximum likelihood approach. Journal of molecular evolution, 17(6), 368-376.[https://doi.org/10.1007/BF01734359](https://doi.org/10.1007/BF01734359)

PhyloCTMC(TimeTree **tree**, Number **mu**, Double[] **freq**, SiteModel **siteModel**, Double[] **branchRates**, Integer **L**, String **dataType**)
-----------------------------------------------------------------------------------------------------------------------------------------------------

The phylogenetic continuous-time Markov chain distribution. A sequence is simulated for every leaf node, and every direct ancestor node with an id.(The sampling distribution that the phylogenetic likelihood is derived from.)

### Parameters

- TimeTree **tree** - the time tree.
- Number **mu** - the clock rate. Default value is 1.0.
- Double[] **freq** - the root probabilities. Optional parameter. If not specified then first row of e^{100*Q) is used.
- SiteModel **siteModel** - the site model
- Double[] **branchRates** - a rate for each branch in the tree. Branch rates are assumed to be 1.0 otherwise.
- Integer **L** - length of the alignment
- String **dataType** - the data type used for simulations

### Return type

- Alignment

### Reference

Felsenstein, J. (1981). Evolutionary trees from DNA sequences: a maximum likelihood approach. Journal of molecular evolution, 17(6), 368-376.[https://doi.org/10.1007/BF01734359](https://doi.org/10.1007/BF01734359)

