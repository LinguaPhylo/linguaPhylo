PhyloCTMC distribution
======================
PhyloCTMC(TimeTree **tree**, Object **mu**, Double[] **freq**, Double[][] **Q**, Double[] **siteRates**, Double[] **branchRates**, Integer **L**)
-------------------------------------------------------------------------------------------------------------------------------------------------

The phylogenetic continuous-time Markov chain distribution. A sequence is simulated for every leaf node, and every direct ancestor node with an id.(The sampling distribution that the phylogenetic likelihood is derived from.)

### Parameters

- TimeTree **tree** - the time tree.
- Object **mu** - the clock rate. Default value is 1.0.
- Double[] **freq** - the root probabilities. Optional parameter. If not specified then first row of e^{100*Q) is used.
- Double[][] **Q** - the instantaneous rate matrix.
- Double[] **siteRates** - a rate for each site in the alignment. Site rates are assumed to be 1.0 otherwise.
- Double[] **branchRates** - a rate for each branch in the tree. Branch rates are assumed to be 1.0 otherwise.
- Integer **L** - length of the alignment

### Return type

- Object



