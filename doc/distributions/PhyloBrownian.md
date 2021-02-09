PhyloBrownian distribution
==========================
PhyloBrownian(TimeTree **tree**, Double **diffRate**, Double **y0**)
--------------------------------------------------------------------

The phylogenetic Brownian motion distribution. A continous trait is simulated for every leaf node, and every direct ancestor node with an id.(The sampling distribution that the phylogenetic continuous trait likelihood is derived from.)

### Parameters

- TimeTree **tree** - the time tree.
- Double **diffRate** - the diffusion rate.
- Double **y0** - the value of continuous trait at the root.

### Return type

- ContinuousCharacterData

### Reference

Felsenstein J. (1973). Maximum-likelihood estimation of evolutionary trees from continuous characters. American journal of human genetics, 25(5), 471–492.[https://www.ncbi.nlm.nih.gov/pmc/articles/PMC1762641/](https://www.ncbi.nlm.nih.gov/pmc/articles/PMC1762641/)

PhyloBrownian(TimeTree **tree**, Double **diffRate**, Double **y0**)
--------------------------------------------------------------------

The phylogenetic Brownian motion distribution. A continous trait is simulated for every leaf node, and every direct ancestor node with an id.(The sampling distribution that the phylogenetic continuous trait likelihood is derived from.)

### Parameters

- TimeTree **tree** - the time tree.
- Double **diffRate** - the diffusion rate.
- Double **y0** - the value of continuous trait at the root.

### Return type

- ContinuousCharacterData

### Reference

Felsenstein J. (1973). Maximum-likelihood estimation of evolutionary trees from continuous characters. American journal of human genetics, 25(5), 471–492.[https://www.ncbi.nlm.nih.gov/pmc/articles/PMC1762641/](https://www.ncbi.nlm.nih.gov/pmc/articles/PMC1762641/)

PhyloBrownian(TimeTree **tree**, Double **variance**, Double **theta**, Double **alpha**, Double **y0**)
--------------------------------------------------------------------------------------------------------

The phylogenetic Brownian motion distribution. A continous trait is simulated for every leaf node, and every direct ancestor node with an id.(The sampling distribution that the phylogenetic continuous trait likelihood is derived from.)

### Parameters

- TimeTree **tree** - the time tree.
- Double **variance** - the variance of the underlying Brownian process.
- Double **theta** - the 'optimal' value that the long-term process is centered around.
- Double **alpha** - the drift term that determines the rate of drift towards the optimal value.
- Double **y0** - the value of continuous trait at the root.

### Return type

- ContinuousCharacterData

### Reference

Felsenstein J. (1973). Maximum-likelihood estimation of evolutionary trees from continuous characters. American journal of human genetics, 25(5), 471–492.[https://www.ncbi.nlm.nih.gov/pmc/articles/PMC1762641/](https://www.ncbi.nlm.nih.gov/pmc/articles/PMC1762641/)

PhyloBrownian(TimeTree **tree**, Double **diffRate**, Double **theta**, Double **alpha**, Double **y0**, Double[] **branchThetas**)
-----------------------------------------------------------------------------------------------------------------------------------

The phylogenetic Brownian motion distribution. A continous trait is simulated for every leaf node, and every direct ancestor node with an id.(The sampling distribution that the phylogenetic continuous trait likelihood is derived from.)

### Parameters

- TimeTree **tree** - the time tree.
- Double **diffRate** - the variance of the underlying Brownian process. This is not the equilibrium variance of the OU process.
- Double **theta** - the 'optimal' value that the long-term process is centered around.
- Double **alpha** - the drift term that determines the rate of drift towards the optimal value.
- Double **y0** - the value of continuous trait at the root.
- Double[] **branchThetas** - the 'optimal' value for each branch in the tree.

### Return type

- ContinuousCharacterData

### Reference

Felsenstein J. (1973). Maximum-likelihood estimation of evolutionary trees from continuous characters. American journal of human genetics, 25(5), 471–492.[https://www.ncbi.nlm.nih.gov/pmc/articles/PMC1762641/](https://www.ncbi.nlm.nih.gov/pmc/articles/PMC1762641/)

