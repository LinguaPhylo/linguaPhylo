PhyloBrownian distribution
==========================
PhyloBrownian([TimeTree](../types/TimeTree.md) **tree**, [Double](../types/Double.md) **diffRate**, [Double](../types/Double.md) **y0**)
----------------------------------------------------------------------------------------------------------------------------------------

The phylogenetic Brownian motion distribution. A continous trait is simulated for every leaf node, and every direct ancestor node with an id.(The sampling distribution that the phylogenetic continuous trait likelihood is derived from.)

### Parameters

- [TimeTree](../types/TimeTree.md) **tree** - the time tree.
- [Double](../types/Double.md) **diffRate** - the diffusion rate.
- [Double](../types/Double.md) **y0** - the value of continuous trait at the root.

### Return type

[ContinuousCharacterData](../types/ContinuousCharacterData.md)


### Examples

- simplePhyloBrownian.lphy

### Reference

Felsenstein J. (1973). Maximum-likelihood estimation of evolutionary trees from continuous characters. American journal of human genetics, 25(5), 471–492.[https://www.ncbi.nlm.nih.gov/pmc/articles/PMC1762641/](https://www.ncbi.nlm.nih.gov/pmc/articles/PMC1762641/)

